/*
 * Copyright (c) 2015 SONATA-NFV, 2017 5GTANGO [, ANY ADDITIONAL AFFILIATION]
 * ALL RIGHTS RESERVED.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Neither the name of the SONATA-NFV, 5GTANGO [, ANY ADDITIONAL AFFILIATION]
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * This work has been performed in the framework of the SONATA project,
 * funded by the European Commission under Grant number 671517 through
 * the Horizon 2020 and 5G-PPP programmes. The authors would like to
 * acknowledge the contributions of their colleagues of the SONATA
 * partner consortium (www.sonata-nfv.eu).
 *
 * This work has been performed in the framework of the 5GTANGO project,
 * funded by the European Commission under Grant number 761493 through
 * the Horizon 2020 and 5G-PPP programmes. The authors would like to
 * acknowledge the contributions of their colleagues of the 5GTANGO
 * partner consortium (www.5gtango.eu).
 */

package com.github.tng.vnv.planner

import com.github.tng.vnv.planner.model.NetworkService
import com.github.tng.vnv.planner.model.Test
import com.github.tng.vnv.planner.model.TestSuite
import com.github.tng.vnv.planner.service.CatalogueService
import com.github.tng.vnv.planner.service.NetworkServiceService
import com.github.tng.vnv.planner.service.TestPlanService
import com.github.tng.vnv.planner.model.Package
import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.service.TestService
import com.github.tng.vnv.planner.service.TestSuiteService
import com.github.tng.vnv.planner.utils.TEST_PLAN_STATUS
import groovy.util.logging.Log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils

import java.security.AllPermission
import java.util.concurrent.CompletableFuture

import static org.springframework.util.StringUtils.isEmpty

@Log
@Component
class ScheduleManager {

    @Autowired
    TestService testService

    @Autowired
    NetworkServiceService networkServiceService

    @Autowired
    TestPlanService testPlanService

    @Autowired
    TestSuiteService testSuiteService

    @Autowired
    WorkflowManager workflowManager

    @Value('${app.NOT_AVAILABLE_DATA}')
    String not_available_data
    @Value('${app.NOT_MATCHING_TEST_TAGS}')
    String not_matching_test_tags

    TestSuite create(Package packageMetadata) {
        create(new TestSuite(testPlans: new ArrayList<>(
                testPlanService.createByPackage(packageMetadata))))
    }

    TestSuite create(TestSuite ts) {
        def testPlans = [] as HashSet
        TestSuite testSuite = testSuiteService.save(new TestSuite(uuid:(!isEmpty(ts.uuid))?ts.uuid:UUID.randomUUID().toString()))
        ts?.testPlans?.each{ it.uuid=(!isEmpty(it.uuid))?it.uuid:UUID.randomUUID().toString()}
                .toSorted().each { tp -> tp = create(tp, testSuite)
            if(tp != null) testPlans.add(tp)
        }
        workflowManager.searchForScheduledPlan()
        testSuite.testPlans = new ArrayList<>(testPlans)
        testSuite
    }

    TestSuite update(TestSuite ts) {
        def testPlans = [] as HashSet
        TestSuite testSuite = testSuiteService.findByUuid(ts.uuid)
        ts.testPlans?.toSorted().forEach({ tp ->
            update(tp, testSuite)
            testPlans.add(tp)
        })
        workflowManager.searchForScheduledPlan()
        testSuite.testPlans = new ArrayList<>(testPlans)
        testSuite
    }


    TestPlan create(TestPlan tp, TestSuite ts) {
        tp.testSuite = ts
        if (( isEmpty(tp.serviceUuid) && tp.nsd==null) || ( isEmpty(tp.testUuid) && tp.testd==null)){
            tp.status = TEST_PLAN_STATUS.REJECTED
            tp.description = tp.description+" $not_available_data"
        } else {
            NetworkService service;
            if(isEmpty(tp.serviceUuid)){
                service = new NetworkService(uuid: (tp.nsd.uuid), nsd: tp.nsd)
                service?.loadDescriptor()
            } else {
                service = networkServiceService.findByUuid(tp.serviceUuid)
            }
            Test test
            if(isEmpty(tp.testUuid)){
                test = new Test(uuid: tp.testd.uuid, testd: tp.testd)
                test.loadDescriptor()
            } else {
                test = testService.findByUuid(tp.testUuid)
            }
            if(service == null || test == null){
                tp.status = TEST_PLAN_STATUS.REJECTED
                tp.description = tp.description +" $not_available_data"
            } else {
                tp.nsd = service.nsd
                tp.testd = test.testd
                if (!(service?.descriptor.tagMatchedWith(test?.descriptor))) {
                    tp.status = TEST_PLAN_STATUS.REJECTED
                    tp.description = tp.description +" $not_matching_test_tags"
                }
                if ( !isEmpty(tp.testd?.confirm_required) && tp.testd?.confirm_required == '1'
                        && ( isEmpty(tp.testd?.confirmed) || tp.testd?.confirmed != '1'))
                    tp.status = TEST_PLAN_STATUS.NOT_CONFIRMED
                else {
                    tp.status = TEST_PLAN_STATUS.SCHEDULED
                    tp = testPlanService.save(tp)
                }
            }
        }
        tp
    }

    TestPlan update(TestPlan tp, TestSuite ts) {
        TestPlan tpOld = testPlanService.testPlanRepository.find { it.uuid == tp.uuid}
        tpOld.status = TEST_PLAN_STATUS.UPDATED
        testPlanService.testPlanRepository.save(tpOld)
        tp.id = null
        tp.testSuite = ts
        tp.status = TEST_PLAN_STATUS.SCHEDULED
        testPlanService.save(tp)
    }
}
