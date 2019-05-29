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

package tng.vnv.planner

import groovy.json.JsonSlurper
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification
import tng.vnv.planner.controller.PackageController
import tng.vnv.planner.controller.TestPlanController
import tng.vnv.planner.model.CuratorCallback
import tng.vnv.planner.model.PackageCallback
import tng.vnv.planner.model.TestResult
import tng.vnv.planner.repository.TestPlanRepository
import tng.vnv.planner.repository.TestSetRepository
import tng.vnv.planner.utils.TestPlanStatus
import tng.vnv.planner.utils.TestSetType

@Transactional
@SpringBootTest(classes = [Application.class], webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class TestPlanControllerSpec extends Specification {

    @Autowired
    TestPlanRepository testPlanRepository

    @Autowired
    PackageController packageController

    @Autowired
    TestPlanController testPlanController

    @Autowired
    TestSetRepository testSetRepository

    @Test
    def "Get all test plans"() {
        when:
        def packageCallback = new PackageCallback(packageId: '0d274a40-191a-4f6f-b6ef-2a6960c24bc2', confirmRequired: false)
        packageController.onChange(packageCallback).body
        def tesPlanList = testPlanController.listAllTestPlans()
        then:
        tesPlanList.size() > 0
    }

    @Test
    def "Get a test plan"() {
        when:
        def packageCallback = new PackageCallback(packageId: '0d274a40-191a-4f6f-b6ef-2a6960c24bc2', confirmRequired: false)
        def testPlans = packageController.onChange(packageCallback).body
        def testPlan = testPlanController.findTestPlan(testPlans[0].uuid)
        then:
        testPlan.testUuid == '88f6c1c4-c614-4f4d-87e6-72ef0192956f'
    }

    @Test
    def "Cancel a test plan"() {
        when:
        def packageCallback = new PackageCallback(packageId: '0d274a40-191a-4f6f-b6ef-2a6960c24bc2', confirmRequired: false)
        def testPlans = packageController.onChange(packageCallback).body
        testPlanController.deleteTestPlan(testPlans[0].uuid)
        def testPlan = testPlanRepository.findByUuid(testPlans[0].uuid)
        then:
        testPlan.testStatus == TestPlanStatus.CANCELLING
    }

    @Test
    def "Update a test plan status"() {
        when:
        def packageCallback = new PackageCallback(packageId: '0d274a40-191a-4f6f-b6ef-2a6960c24bc2', confirmRequired: false)
        def testPlans = packageController.onChange(packageCallback).body
        testPlanController.update(testPlans[0].uuid, TestPlanStatus.COMPLETED)
        def testPlan = testPlanRepository.findByUuid(testPlans[0].uuid)
        def testSet = testSetRepository.findByUuid(testPlan.testSetUuid)
        then:
        testPlan.testStatus == TestPlanStatus.COMPLETED
        testSet.status == TestPlanStatus.COMPLETED
    }

    @Test
    def "Create a test plan by service uuid"() {
        when:
        testPlanController.buildTestPlansByService('57cebe79-96aa-4f41-af80-93050bfddd9f', true)
        def testSetList = testSetRepository.findAll()
        def testPlanList = testPlanRepository.findAll()
        then:
        testSetList.size() == 1
        testPlanList.size() == 1
        testSetList[0].requestType == TestSetType.SERVICE
        testPlanList[0].serviceUuid == '57cebe79-96aa-4f41-af80-93050bfddd9f'
        testPlanList[0].testStatus == TestPlanStatus.WAITING_FOR_CONFIRMATION
        testSetList[0].status == TestPlanStatus.WAITING_FOR_CONFIRMATION
    }

    @Test
    def "Create a test plan by test uuid"() {
        when:
        testPlanController.buildTestPlansByTest('88f6c1c4-c614-4f4d-87e6-72ef0192956f', true)
        def testSetList = testSetRepository.findAll()
        def testPlanList = testPlanRepository.findAll()
        then:
        testSetList.size() == 1
        testPlanList.size() == 1
        testSetList[0].requestType == TestSetType.TEST
        testPlanList[0].testUuid == '88f6c1c4-c614-4f4d-87e6-72ef0192956f'
        testPlanList[0].testStatus == TestPlanStatus.WAITING_FOR_CONFIRMATION
        testSetList[0].status == TestPlanStatus.WAITING_FOR_CONFIRMATION
    }

    @Test
    def "Create a test plan by test uuid and service uuid"() {
        when:
        testPlanController.buildTestPlansByNsTdPair('88f6c1c4-c614-4f4d-87e6-72ef0192956f', '57cebe79-96aa-4f41-af80-93050bfddd9f', false)
        def testSetList = testSetRepository.findAll()
        def testPlanList = testPlanRepository.findAll()
        then:
        testSetList.size() == 1
        testPlanList.size() == 1
        testSetList[0].requestType == TestSetType.TEST_AND_SERVICE
        testPlanList[0].testUuid == '88f6c1c4-c614-4f4d-87e6-72ef0192956f'
        testPlanList[0].serviceUuid == '57cebe79-96aa-4f41-af80-93050bfddd9f'
        testPlanList[0].testStatus == TestPlanStatus.SCHEDULED
        testSetList[0].status == TestPlanStatus.SCHEDULED
    }

    @Test
    def "Create a test plan by test tag"() {
        when:
        testPlanController.buildTestPlansByTestingTag('eu.5gtango.testingtag.example', false)
        def testSetList = testSetRepository.findAll()
        def testPlanList = testPlanRepository.findAll()
        then:
        testSetList.size() == 1
        testPlanList.size() == 1
        testSetList[0].requestType == TestSetType.TEST_AND_SERVICE
        testPlanList[0].testUuid == '88f6c1c4-c614-4f4d-87e6-72ef0192956f'
        testPlanList[0].serviceUuid == '57cebe79-96aa-4f41-af80-93050bfddd9f'
        testPlanList[0].testStatus == TestPlanStatus.SCHEDULED
        testSetList[0].status == TestPlanStatus.SCHEDULED
    }


    @Test
    def "Test plan completed by curator"() {
        when:
        def createdPlan = testPlanController.buildTestPlansByNsTdPair('88f6c1c4-c614-4f4d-87e6-72ef0192956f', '57cebe79-96aa-4f41-af80-93050bfddd9f', false)
        def json="{\"event_actor\": \"Curator\", \"testPlanUuid\": \"${testPlanUuid: createdPlan[0].uuid}\", \"exception\": \"\", \"status\": \"COMPLETED\", \"test_results\": [{\"test_uuid\": \"cf8e1dd9-777f-4f96-9458-5a5fe0a86f7d\",\"test_result_uuid\": \"ff0a1530-c72e-49c3-815c-fa86fe3d952c\",\"test_status\": \"COMPLETED\"}]}"
        def jsonSlurper = new JsonSlurper()
        CuratorCallback curatorCallback = jsonSlurper.parseText(toCamelCase(json))
        testPlanController.onChangeCompleted(curatorCallback)
        def testSetList = testSetRepository.findAll()
        def testPlanList = testPlanRepository.findAll()
        then:
        testSetList[0].status == TestPlanStatus.COMPLETED
        testPlanList[0].testStatus == TestPlanStatus.COMPLETED
    }

    @Test
    def "Test plan status update by curator"() {
        when:
        def createdPlan = testPlanController.buildTestPlansByNsTdPair('88f6c1c4-c614-4f4d-87e6-72ef0192956f', '57cebe79-96aa-4f41-af80-93050bfddd9f', false)
        def result = new TestResult(testUuid: "123", testResultUuid: "5678", testStatus: TestPlanStatus.STARTING)
        def curatorCallback = new CuratorCallback(eventActor: 'Curator', status: TestPlanStatus.STARTING, testPlanUuid: createdPlan[0].uuid, testResults: [result])
        testPlanController.onChange(curatorCallback)
        def testSetList = testSetRepository.findAll()
        def testPlanList = testPlanRepository.findAll()
        then:
        testSetList[0].status == TestPlanStatus.STARTING
        testPlanList[0].testStatus == TestPlanStatus.STARTING
    }

    @Test
    def "Get all tests related with a testing tag"() {
        when:
        def testsByTag = testPlanController.listTestsByTag("eu.5gtango.testingtag.example")
        then:
        testsByTag[0].pd.name == "generic-probes-test-pingonly"
    }

    @Test
    def "Get all services related with a testing tag"() {
        when:
        def servicesByTag = testPlanController.listServicesByTag("eu.5gtango.testingtag.example")
        then:
        servicesByTag[0].pd.name == "test-ns-nsid1v"
    }

    @Test
    def "Get all services related with a test"() {
        when:
        def servicesByTest = testPlanController.listServicesByTest('88f6c1c4-c614-4f4d-87e6-72ef0192956f')
        then:
        servicesByTest[0].pd.name[0] == "test-ns-nsid1v"
    }

    @Test
    def "Get all tests related with a service"() {
        when:
        def testsByService = testPlanController.listTestsByService('57cebe79-96aa-4f41-af80-93050bfddd9f')
        then:
        testsByService[0].pd.name[0] == "generic-probes-test-pingonly"
    }

    static String toCamelCase( String text, boolean capitalized = false ) {
        text = text.replaceAll( "(_)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() } )
        return capitalized ? capitalize(text) : text
    }
}
