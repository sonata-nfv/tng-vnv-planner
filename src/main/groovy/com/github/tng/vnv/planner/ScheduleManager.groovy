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

import java.security.AllPermission
import java.util.concurrent.CompletableFuture

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

    @Value('${app.NOT_AVAILABLE_DATA}')
    String not_available_data
    @Value('${app.NOT_MATCHING_TEST_TAGS}')
    String not_matching_test_tags

    TestSuite create(Package packageMetadata) {

        TestSuite testSuite = new TestSuite(testPlans: new ArrayList<>(
                testPlanService.createByPackage(packageMetadata)))
        testSuite = create(testSuite)

        //todo-allemaos: test the impl (the not_Confirmed testPlans of the test descriptors with t.testd.status == 'confirm_required' by returning testPlan.status as TEST_PLAN_STATUS.NOT_CONFIRMED)

        testSuite
    }

    TestSuite create(TestSuite ts) {
        def testPlans = [] as HashSet
        TestSuite testSuite = testSuiteService.save(new TestSuite())
        ts.testPlans?.toSorted().each { tp ->
            tp = create(tp, testSuite)
            if(tp != null)
                testPlans.add(tp)
        }
        testSuite.testPlans = new ArrayList<>(testPlans)
        testSuite
    }

    TestSuite update(TestSuite ts) {
        def testPlans = [] as HashSet
        TestSuite testSuite = testSuiteService.save(new TestSuite())
        ts.testPlans?.toSorted().forEach({ tp ->
            update(tp, testSuite)
            testPlans.add(tp)
        })
        testSuite.testPlans = new ArrayList<>(testPlans)
        testSuite
    }


    TestPlan create(TestPlan tp, TestSuite ts) {
        tp.testSuite = ts
        if (!((tp.serviceUuid != null || tp.nsd != null) && (tp.testUuid != null || tp.testd != null))){
            tp.status = TEST_PLAN_STATUS.REJECTED
            tp.description = tp.description +' [$not_available_data]'
        } else {
            def service = (tp.serviceUuid != null) ? networkServiceService.findByUuid(tp.serviceUuid).reload() :
                    new NetworkService(uuid: tp.nsd.uuid ?: UUID.randomUUID().toString() + 'DIY', nsd: tp.nsd).reload()
            tp.nsd = service.nsd
            def test = (tp.testUuid != null) ? testService.findByUuid(tp.testUuid).reload() :
                    new Test(uuid: tp.testd.uuid ?: UUID.randomUUID().toString() + 'DIY', testd: tp.testd).reload()
            tp.testd = test.testd
            tp.uuid = service.uuid ?:tp.nsd.uuid + test.uuid?:tp.testd.uuid
            if (!(service.descriptor.tagMatchedWith(test.descriptor))) {
                tp.status = TEST_PLAN_STATUS.REJECTED
                tp.description = tp.description + ' [$not_matching_test_tags]'
            }
            if (tp.testd?.confirm_required != null && 'true'.contains(tp.testd.confirm_required) && tp.status != TEST_PLAN_STATUS.CONFIRMED)
                tp.status = TEST_PLAN_STATUS.NOT_CONFIRMED
            else {
                tp.status = TEST_PLAN_STATUS.SCHEDULED
                testPlanService.save(tp)
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
