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

import com.github.tng.vnv.planner.client.Curator
import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.model.TestPlanResponse
import com.github.tng.vnv.planner.service.TestPlanService
import com.github.tng.vnv.planner.utils.TEST_PLAN_STATUS
import groovy.util.logging.Log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Log
@Component
class WorkflowManager {

    @Autowired
    Curator curator

    @Autowired
    TestPlanService testPlanService

    TestPlan pendingTestPlan

    @Scheduled(fixedRate = 5000L , initialDelay = 1000L)
    void searchForScheduledPlan() {
        pendingTestPlan = testPlanService.findPendingTestPlan()
        if (pendingTestPlan == null) {
            TestPlan nextTestPlan = testPlanService.findNextScheduledTestPlan()?.unBlob()
            if (nextTestPlan != null) {
                log.info("#~#vnvlogPlanner.WorkflowManager.searchForScheduledPlan - Available scheduled Plan Descr: [\"" + nextTestPlan.description + "\"]")
                TestPlanResponse testPlanResponse = curator.proceedWith(nextTestPlan)
                switch (testPlanResponse.status) {
                    case TEST_PLAN_STATUS.STARTING:
                        pendingTestPlan = nextTestPlan
                        testPlanService.update(pendingTestPlan.uuid, TEST_PLAN_STATUS.PENDING)
                        break
                    default:
                        log.info("Get response: ${testPlanResponse.status} for plan description: \"${nextTestPlan.description}\"")
                        break
                }

            }
        }
    }

    void deleteTestPlan(String uuid){
        curator.deleteTestPlan(uuid)
        testPlanService.delete(uuid)
    }
}
