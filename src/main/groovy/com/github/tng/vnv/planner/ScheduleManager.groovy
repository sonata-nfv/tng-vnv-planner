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

import com.github.tng.vnv.planner.model.TestSuite
import com.github.tng.vnv.planner.service.TestPlanService
import com.github.tng.vnv.planner.model.Package
import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.service.TestSuiteService
import com.github.tng.vnv.planner.utils.TEST_PLAN_STATUS
import groovy.util.logging.Log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.concurrent.CompletableFuture

@Log
@Component
class ScheduleManager {

    @Autowired
    TestPlanService testPlanService

    @Autowired
    TestSuiteService testSuiteService

//    @Async
//    CompletableFuture<Boolean> create(Package packageMetadata) {
    TestSuite create(Package packageMetadata) {
        Set testPlans = testPlanService.createByPackage(packageMetadata)

        TestSuite testSuite = new TestSuite(testPlans: testPlans)
        testSuite = create(testSuite)

        //fixme-allemaso: fix the not_Confirmed testPlans of the test descriptors with t.testd.status == 'confirm_required' by returning testPlan.status as TEST_PLAN_STATUS.NOT_CONFIRMED
//        Boolean out = false
//        CompletableFuture.completedFuture(out)
        testSuite
    }

    def create(TestSuite ts) {
        List<TestPlan> testPlanList = ts.testPlans
        TestSuite testSuite = testSuiteService.save(new TestSuite())
        testPlanList?.forEach({ tp ->
            tp.testSuite = testSuite
            if (tp.testd.confirm_required != null && 'true'.contains(tp.testd.confirm_required) && tp.status != TEST_PLAN_STATUS.CONFIRMED)
                tp.status = TEST_PLAN_STATUS.NOT_CONFIRMED
            else
                tp.status = TEST_PLAN_STATUS.SCHEDULED
            testPlanService.save(tp)
        })
        testSuite.testPlans = ts.testPlans
        testSuite
    }

    def update(TestPlan testPlan) {
        TestPlan testPlanOld = testPlanService.testPlanRepository.find { it.uuid == testPlan.uuid}
        testPlanOld.status = TEST_PLAN_STATUS.UPDATED
        testPlanService.testPlanRepository.save(testPlanOld)
        testPlan.id = null
        testPlan.status = TEST_PLAN_STATUS.SCHEDULED
        testPlanService.save(testPlan)
    }
}
