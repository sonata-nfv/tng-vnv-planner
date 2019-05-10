/*
 * Copyright (c) 2015 SONATA-NFV, 2019 5GTANGO [, ANY ADDITIONAL AFFILIATION]
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

package tng.vnv.planner

import groovy.transform.Synchronized
import tng.vnv.planner.client.Curator
import tng.vnv.planner.model.TestPlan
import tng.vnv.planner.model.TestRequest
import tng.vnv.planner.model.TestResult
import tng.vnv.planner.model.TestSet
import tng.vnv.planner.service.TestService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import groovy.util.logging.Slf4j

import tng.vnv.planner.utils.TestPlanStatus

@Slf4j
@Component
class WorkflowManager {

    @Autowired
    Curator curator

    @Autowired
    TestService testService

    @Synchronized
    void searchForScheduledSet() {
        TestSet nextTestSet = testService.findNextScheduledTestSet()
        if (nextTestSet != null) {

            def nextTestServices = nextTestSet.testPlans?.collect {it.serviceUuid} as List
            def currentExecutingTestSets = testService.findExecutingTestSet()

            if(currentExecutingTestSets != null && !currentExecutingTestSets.isEmpty()) {
                def canTestSetBeExecuted = currentExecutingTestSets.every {testSet -> testSet.testPlans.every { testPlan -> !nextTestServices.contains(testPlan.serviceUuid) }}
                if(!canTestSetBeExecuted) {
                    log.info("There are currently executing test sets that contains the same service -- This test set have to wait")
                    return
                }
            }

            testService.updateSet(nextTestSet.uuid, TestPlanStatus.STARTING)
            proceedWith(nextTestSet.testPlans[0])

        }
    }

    @Synchronized
    void testPlanFinished(def justCompletedTestPlanUUID) {

        def justCompletedTestPlan = testService.findPlanByUuid(justCompletedTestPlanUUID as UUID)
        def justExecutedTestSet = testService.findSetByUuid(justCompletedTestPlan.testSetUuid)

        // If it has an error, it is stopped
        if(justCompletedTestPlan.testStatus == TestPlanStatus.ERROR) {
            completeTestSet(justCompletedTestPlan.testSetUuid, TestPlanStatus.ERROR)
            return
        }

        def indexOfCompletedTestPlan = justExecutedTestSet.testPlans.indexOf(justCompletedTestPlan)

        // TestSet completed
        if(justExecutedTestSet.testPlans.size()  == (indexOfCompletedTestPlan + 1)) {
            completeTestSet(justCompletedTestPlan.testSetUuid)
            return
        }

        def nextTestPlan = null
        def indexOfNextTestPlan = indexOfCompletedTestPlan + 1
        while(indexOfNextTestPlan < justExecutedTestSet.testPlans.size()) {
            def currentTestPlan = justExecutedTestSet.testPlans[indexOfNextTestPlan]

            if(currentTestPlan.testStatus == TestPlanStatus.NOT_CONFIRMED) {
                indexOfNextTestPlan = indexOfNextTestPlan + 1
                continue
            }

            if(currentTestPlan.testStatus == TestPlanStatus.SCHEDULED) {
                nextTestPlan = currentTestPlan
                break
            }

            throw new IllegalStateException(String.format("TestPlan with UUID %s cannot be at state %s at this moment", currentTestPlan.uuid, currentTestPlan.testStatus))
        }

        if(nextTestPlan != null) {
            proceedWith(nextTestPlan)
            return
        }

        completeTestSet(justCompletedTestPlan.testSetUuid)
    }

    void deleteTestPlan(UUID uuid){
        curator.delete(uuid)
        testService.deletePlan(uuid)
    }

    private void completeTestSet(def completedTestSetUUID, def status = TestPlanStatus.COMPLETED) {
        testService.updateSet(completedTestSetUUID as UUID, status as String)
        new Thread(new Runnable() {
            @Override
            void run() {
                searchForScheduledSet()
            }
        }).start()
    }

    void proceedWith(TestPlan testPlan) {

        log.info("Starting TestPlan with UUID {}", testPlan.testUuid)
        testService.updatePlan(testPlan.uuid, TestPlanStatus.STARTING)

        def testResult = curator.post(new TestRequest(testPlanUuid: testPlan.uuid,
                nsdUuid: testPlan.serviceUuid,
                testdUuid: testPlan.testUuid)).body as TestResult

        testService.updatePlan(testPlan.uuid, testResult.testStatus)
        log.info("TestPlan with UUID {}, received by the Curator, new testStatus: {}", testPlan.uuid, testResult.testStatus)
    }

    void cancelTestSet(UUID uuid){
        //curator.delete(uuid)
        //TODO:  cancell all associated testplans
        // testService.cancelAllTestPlansByTestSetUuid(uuid)
        testService.cancelTestSet(uuid)
    }

    /*void cancelAllTestPlans(){
        //curator.delete(uuid)
        testService.canceAllT
        testService.deletePlan(uuid)
    }*/
}
