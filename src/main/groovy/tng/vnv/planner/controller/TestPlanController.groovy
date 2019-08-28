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

package tng.vnv.planner.controller


import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import tng.vnv.planner.ScheduleManager
import tng.vnv.planner.WorkflowManager
import tng.vnv.planner.model.Counter
import tng.vnv.planner.model.CuratorCallback
import tng.vnv.planner.model.TestPlan
import tng.vnv.planner.service.NetworkService
import tng.vnv.planner.service.TestService
import tng.vnv.planner.utils.TangoLogger
import tng.vnv.planner.utils.TestPlanStatus

import javax.validation.Valid

@RestController
@Api
@RequestMapping('/api/v1/test-plans')
class TestPlanController {

    @Autowired
    ScheduleManager scheduler
    @Autowired
    WorkflowManager manager
    @Autowired
    TestService testService
    @Autowired
    NetworkService networkServiceService

    //Tango logger
    def tangoLogger = new TangoLogger()
    String tangoLoggerType = null;
    String tangoLoggerOperation = null;
    String tangoLoggerMessage = null;
    String tangoLoggerStatus = null;

    @GetMapping('/count')
    @ApiOperation(value="Get number of test plans by status", notes="Getting the number of the test plan with a specific status")
    @ResponseBody
    Counter countTestPlansByStatus(
            @RequestParam(name = "status", required = false) String status
    ){
        tangoLoggerType = "I";
        tangoLoggerOperation = "TestPlanController.countTestPlansByStatus";
        tangoLoggerStatus = "200";

        Counter response = new Counter()

        if (status != null) {
            tangoLoggerMessage = ("/api/v1/test-plans/count?status=$status (count test plans with status=$status request received)")
            tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)
            response.setCount(testService.countTestPlansByStatus(status))
        } else {
            tangoLoggerMessage = ("/api/v1/test-plans/count (count all test plans request received)")
            tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)
            response.setCount(testService.countTestPlans())
        }
        return response
    }

    @GetMapping
    @ApiOperation(value="Find all test plan", notes="Finding all test plans")
    @ResponseBody
    List<TestPlan> listAllTestPlans(
            @RequestParam(name = "testName", required = false) String testName,
            @RequestParam(name = "serviceName", required = false) String serviceName,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "testUuid", required = false) String testUuid
    ) {

        tangoLoggerType = "I";
        tangoLoggerOperation = "TestPlanController.listAllTestPlans";
        tangoLoggerStatus = "200";

        if (testName != null) {
            tangoLoggerMessage = ("/api/v1/test-plans?testName=$testName (find all test plans by testName=$testName request received)")
            tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)
            testService.findPlansByTestName(testName)
        } else if (serviceName != null) {
            tangoLoggerMessage = ("/api/v1/test-plans?serviceName=$serviceName (find all test plans by serviceName=$serviceName request received)")
            tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)
            testService.findPlansByServiceName(serviceName)
        } else if (status != null) {
            tangoLoggerMessage = ("/api/v1/test-plans?status=$status (find all test plans by status=$status request received)")
            tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)
            testService.findPlansByStatus(status)
        } else if (testUuid != null) {
            tangoLoggerMessage = ("/api/v1/test-plans?testUuid=$testUuid (find all test plans by testUuid=$testUuid request received)")
            tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)
            testService.findPlansByTestUuid(testUuid)
        } else {
            tangoLoggerMessage = ("/api/v1/test-plans (find all test plans request received)")
            tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)
            testService.findAll()
        }
    }

    @GetMapping('/{uuid}')
    @ApiOperation(value="Find a test plan", notes="Finding test plan by uuid")
    @ResponseBody
    TestPlan findTestPlan(@PathVariable String uuid) {
        tangoLoggerType = "I";
        tangoLoggerOperation = "TestPlanController.findTestPlan";
        tangoLoggerMessage = ("/api/v1/test-plans/{uuid} (find test plan by uuid request received. UUID=${uuid})");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        testService.findPlanByUuid(uuid)
    }

    @DeleteMapping('{uuid}')
    @ApiOperation(value="Cancel a test plan", notes="canceling test plan by uuid")
    @ResponseBody
    void deleteTestPlan(@PathVariable String uuid) {
        tangoLoggerType = "I";
        tangoLoggerOperation = "TestPlanController.deleteTestPlan";
        tangoLoggerMessage = ("/api/v1/test-plans/{uuid} (Cancel test plan by uuid request received. UUID=${uuid})");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        manager.deleteTestPlan(uuid)
        manager.testPlanUpdated(uuid)
    }

    @ApiOperation(value="Create a test plan", notes="Creating a test plan")
    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PostMapping('')
    @ResponseBody
    TestPlan save(@Valid @RequestBody TestPlan testPlan) {
        scheduler.scheduleNewTestSet(testPlan)
    }

    @ApiOperation(value="Update a test plan status", notes="Updating a test plan status by uuid")
    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PutMapping('{uuid}')
    @ResponseBody
    TestPlan update(@Valid @PathVariable String uuid, @RequestParam String status) {
        tangoLoggerType = "I";
        tangoLoggerOperation = "TestPlanController.update";
        tangoLoggerMessage = ("/api/v1/test-plans/{uuid} (update test plan status by uuid request received. UUID=${uuid})");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        def testPlan = scheduler.update(uuid, status)
        manager.testPlanUpdated(uuid)

        return testPlan
    }

    @ApiOperation(value="Create a test plan by service uuid")
    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PostMapping('/services')
    @ResponseBody
    List<TestPlan> buildTestPlansByService(@Valid @RequestParam String serviceUuid, @RequestParam(required = false) Boolean confirmRequired) {
        tangoLoggerType = "I";
        tangoLoggerOperation = "TestPlanController.buildTestPlansByService";
        tangoLoggerMessage = ("/api/v1/test-plans/services (create a test plan by service uuid request received. Service UUID: ${serviceUuid})");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        testService.buildTestPlansByService(serviceUuid, confirmRequired).testPlans
    }

    @ApiOperation(value="Create a test plan by test uuid")
    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PostMapping('/tests')
    @ResponseBody
    List<TestPlan> buildTestPlansByTest(@Valid @RequestParam String testUuid, @RequestParam(required = false) Boolean confirmRequired) {
        tangoLoggerType = "I";
        tangoLoggerOperation = "TestPlanController.buildTestPlansByTest";
        tangoLoggerMessage = ("/api/v1/test-plans/tests (create a test plan by test uuid request received. Test UUID: ${testUuid})");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        testService.buildTestPlansByTest(testUuid, confirmRequired).testPlans
    }

    @ApiOperation(value="Create a test plan by testing tag")
    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PostMapping('/testing-tags')
    @ResponseBody
    List<TestPlan> buildTestPlansByTestingTag(@Valid @RequestParam String testingTag, @RequestParam(required = false) Boolean confirmRequired) {
        tangoLoggerType = "I";
        tangoLoggerOperation = "TestPlanController.buildTestPlansByTestingTag";
        tangoLoggerMessage = ("/api/v1/test-plans/testing-tags (create a test plan by testing tag request received. Testing tag: ${testingTag})");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        testService.buildTestPlansByTestingTag(testingTag, confirmRequired).testPlans
    }

    @ApiOperation(value="Create a test plan by test uuid and service uuid")
    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PostMapping('/testAndServices')
    @ResponseBody
    List<TestPlan> buildTestPlansByNsTdPair(@Valid @RequestParam String testUuid, @RequestParam String serviceUuid, @RequestParam(required = false) Boolean confirmRequired) {
        tangoLoggerType = "I";
        tangoLoggerOperation = "TestPlanController.buildTestPlansByNsTdPair";
        tangoLoggerMessage = ("/api/v1/test-plans/testAndServices (create a test plan by service uuid and test uuid request received. Service UUID: ${serviceUuid}), test UUID=${testUuid}");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        testService.buildTestPlansByServiceAndTest(testUuid, serviceUuid, confirmRequired).testPlans
    }

    // Curator

    @ApiOperation(value="Completion callback from curator")
    @ApiResponses(value = [
            @ApiResponse(code = 400, message = 'Bad Request'),
            @ApiResponse(code = 404, message = 'Could not find package with that packageId'),
    ])
    @PostMapping('/on-change/completed')
    @ResponseBody
    void onChangeCompleted(@Valid @RequestBody CuratorCallback callback) {
        tangoLoggerType = "I";
        tangoLoggerOperation = "TestPlanController.onChangeCompleted";
        tangoLoggerMessage = ("/api/v1/test-plans/on-change/completed (test update notification received from curator. uuid=${callback.testPlanUuid} with status=${callback.status})");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        testService.updatePlanStatus(callback.testPlanUuid, callback.status)
        if (callback.status == TestPlanStatus.COMPLETED) {
            tangoLoggerType = "I";
            tangoLoggerOperation = "TestPlanController.onChangeCompleted";
            tangoLoggerMessage = ("test_result_uuid = ${callback.testResults.get(0).testResultUuid}");
            tangoLoggerStatus = "200";
            tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

            testService.updatePlanResultId(callback.testPlanUuid, callback.testResults.get(0).testResultUuid)
        }
        manager.testPlanUpdated(callback.testPlanUuid)
    }

    @ApiOperation(value="Callback from curator")
    @ApiResponses(value = [
            @ApiResponse(code = 400, message = 'Bad Request'),
            @ApiResponse(code = 404, message = 'Could not find package with that packageId'),
    ])
    @PostMapping('/on-change/')
    @ResponseBody
    void onChange(@Valid @RequestBody CuratorCallback callback) {
        tangoLoggerType = "I";
        tangoLoggerOperation = "TestPlanController.onChange";
        tangoLoggerMessage = ("/api/v1/test-plans/on-change (test update notification received from curator. uuid=${callback.testPlanUuid} with status=${callback.status})");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        testService.updatePlanStatus(callback.testPlanUuid, callback.status)
        manager.testPlanUpdated(callback.testPlanUuid)
    }

    // Network Services
    @ApiOperation(value="Find all tests related with a service uuid")
    @GetMapping('/services/{nsdUuid}/tests')
    List<Object> listTestsByService(@PathVariable('nsdUuid') String uuid) {
        tangoLoggerType = "I";
        tangoLoggerOperation = "TestPlanController.listTestsByService";
        tangoLoggerMessage = ("/api/v1/test-plans/services/{nsdUuid}/tests (list tests by service uuid request received. UUID=${uuid}");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        networkServiceService.findTestsByService(uuid)
    }

    @ApiOperation(value="Find all tests related with a testing_tag")
    @GetMapping('/testing-tags/{tag}/tests')
    List<Object> listTestsByTag(@PathVariable('tag') String tag) {
        tangoLoggerType = "I";
        tangoLoggerOperation = "TestPlanController.listTestsByTag";
        tangoLoggerMessage = ("/api/v1/test-plans/testing-tags/{tag}/tests (list tests by tag request received. Testing-tag=${tag}");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        networkServiceService.findTestsByTag(tag)
    }

    // Tests

    @ApiOperation(value="Find all services related with a test")
    @GetMapping('/tests/{testdUuid}/services')
    List<Object> listServicesByTest(@PathVariable('testdUuid') String uuid) {
        tangoLoggerType = "I";
        tangoLoggerOperation = "TestPlanController.listServicesByTest";
        tangoLoggerMessage = ("/api/v1/test-plans/tests/{testdUuid}/services (list services by test uuid request received. UUID=${uuid}");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        testService.findServicesByTest(uuid)
    }

    @ApiOperation(value="Find all services related with a tag")
    @GetMapping('/testing-tags/{tag}/services')
    List<Object> listServicesByTag(@PathVariable('tag') String tag) {
        tangoLoggerType = "I";
        tangoLoggerOperation = "TestPlanController.listServicesByTag";
        tangoLoggerMessage = ("/api/v1/test-plans/tests/{tag}/services (list services by tag request received. Testing-tag=${tag}");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        testService.findServicesByTag(tag)
    }
}
