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
import tng.vnv.planner.model.TestResponse
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
            def nextTextPlan = nextTestSet.testPlans[0]
            log.info("Next testplan: ${nextTextPlan.uuid}. nsd: ${nextTextPlan.serviceUuid}, td: ${nextTextPlan.testUuid}")
            proceedWith(nextTextPlan)

        }
    }

    @Synchronized
    void testPlanUpdated(def justUpdatedTestPlanUUID) {

        def justUpdatedTestPlan = testService.findPlanByUuid(justUpdatedTestPlanUUID as UUID)
        def justExecutedTestSet = testService.findSetByUuid(justUpdatedTestPlan.testSetUuid)

        // If it has an error, it is stopped
        if(justUpdatedTestPlan.testStatus == TestPlanStatus.ERROR) {
            completeTestSet(justUpdatedTestPlan.testSetUuid, TestPlanStatus.ERROR)
            return
        }

        // If all test-plan with same status
        def testplansStatus = []
        justExecutedTestSet.testPlans.each { tp ->
            testplansStatus << tp.testStatus
        }
        testplansStatus.unique()
        if (testplansStatus.size() == 1){
            log.info("all testplans with ${testplansStatus[0]} status")
            if (testplansStatus[0] == TestPlanStatus.COMPLETED || testplansStatus[0] == TestPlanStatus.CANCELLED){
                completeTestSet(justUpdatedTestPlan.testSetUuid, testplansStatus[0])
            } else {
                testService.updateSet(justUpdatedTestPlan.testSetUuid as UUID, testplansStatus[0] as String)
            }
            return
        }

        //def indexOfCompletedTestPlan = justExecutedTestSet.testPlans.indexOf(justUpdatedTestPlan)

        def nextTestPlan = null
        def indexOfNextTestPlan = 0
        while(indexOfNextTestPlan < justExecutedTestSet.testPlans.size()){
            def currentTestPlan = justExecutedTestSet.testPlans[indexOfNextTestPlan]

            if(currentTestPlan.testStatus == TestPlanStatus.SCHEDULED) {
                nextTestPlan = currentTestPlan
                break
            }

            indexOfNextTestPlan = indexOfNextTestPlan +1
        }

        /*def indexOfNextTestPlan = indexOfCompletedTestPlan + 1
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
        }*/

        if(nextTestPlan != null) {
            proceedWith(nextTestPlan)
            return
        }

        //completeTestSet(justUpdatedTestPlan.testSetUuid)
    }

    void deleteTestPlan(UUID uuid){
        curator.delete(uuid)
        testService.deletePlan(uuid)
    }

    private void completeTestSet(def completedTestSetUUID, def status) {
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

        def testResponse = curator.post(new TestRequest(testPlanUuid: testPlan.uuid,
                nsdUuid: testPlan.serviceUuid,
                testdUuid: testPlan.testUuid)).body as TestResponse

        testService.updatePlan(testPlan.uuid, testResponse.status)
        log.info("TestPlan with UUID {}, received by the Curator, new testStatus: {}", testPlan.uuid, testResponse.status)
    }

    void cancelTestSet(UUID uuid){
        testService.cancelTestSet(uuid)
        testService.cancelAllTestPlansByTestSetUuid(uuid)
    }
}
