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

import groovy.util.logging.Slf4j
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import tng.vnv.planner.ScheduleManager
import tng.vnv.planner.WorkflowManager
import tng.vnv.planner.model.CuratorCallback
import tng.vnv.planner.model.TestPlan
import tng.vnv.planner.service.NetworkService
import tng.vnv.planner.service.TestService

import javax.validation.Valid

@Slf4j
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

    @GetMapping
    @ApiOperation(value="Find all test plan", notes="Finding all test plans")
    @ResponseBody
    List<TestPlan> listAllTestPlans() {
        log.info("/api/v1/test-plans (find all test plans request received)")
        testService.findAll()
    }

    @GetMapping('/{uuid}')
    @ApiOperation(value="Find a test plan", notes="Finding test plan by uuid")
    @ResponseBody
    TestPlan findTestPlan(@PathVariable String uuid) {
        log.info("/api/v1/test-plans/{uuid} (find test plan by uuid request received. UUID=${uuid})")
        testService.findPlanByUuid(uuid)
    }

    @DeleteMapping('{uuid}')
    @ApiOperation(value="Cancel a test plan", notes="canceling test plan by uuid")
    @ResponseBody
    void deleteTestPlan(@PathVariable String uuid) {
        log.info("/api/v1/test-plans/{uuid} (Cancel test plan by uuid request received. UUID=${uuid})")
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
        log.info("/api/v1/test-plans/{uuid} (update test plan status by uuid request received. UUID=${uuid})")
        scheduler.update(uuid, status)
        manager.testPlanUpdated(uuid)
    }

    @ApiOperation(value="Create a test plan by service uuid")
    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PostMapping('/services')
    @ResponseBody
    List<TestPlan> buildTestPlansByService(@Valid @RequestParam String serviceUuid, @RequestParam(required = false) Boolean confirmRequired) {
        log.info("/api/v1/test-plans/services (create a test plan by service uuid request received. Service UUID: ${serviceUuid})")
        testService.buildTestPlansByService(serviceUuid, confirmRequired).testPlans
    }

    @ApiOperation(value="Create a test plan by test uuid")
    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PostMapping('/tests')
    @ResponseBody
    List<TestPlan> buildTestPlansByTest(@Valid @RequestParam String testUuid, @RequestParam(required = false) Boolean confirmRequired) {
        log.info("/api/v1/test-plans/tests (create a test plan by test uuid request received. Test UUID: ${testUuid})")
        testService.buildTestPlansByTest(testUuid, confirmRequired).testPlans
    }

    @ApiOperation(value="Create a test plan by testing tag")
    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PostMapping('/testing-tags')
    @ResponseBody
    List<TestPlan> buildTestPlansByTestingTag(@Valid @RequestParam String testingTag, @RequestParam(required = false) Boolean confirmRequired) {
        log.info("/api/v1/test-plans/testing-tags (create a test plan by testing tag request received. Testing tag: ${testingTag})")
        testService.buildTestPlansByTestingTag(testingTag, confirmRequired).testPlans
    }

    @ApiOperation(value="Create a test plan by test uuid and service uuid")
    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PostMapping('/testAndServices')
    @ResponseBody
    List<TestPlan> buildTestPlansByNsTdPair(@Valid @RequestParam String testUuid, @RequestParam String serviceUuid, @RequestParam(required = false) Boolean confirmRequired) {
        log.info("/api/v1/test-plans/testAndServices (create a test plan by service uuid and test uuid request received. Service UUID: ${serviceUuid}), test UUID=${testUuid}")
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
        log.info("/api/v1/test-plans/on-change/completed (test update notification received from curator. uuid=${callback.test_plan_uuid} with status=${callback.status})")
        testService.updatePlanStatus(callback.test_plan_uuid, callback.status)
        testService.updatePlanResultId(callback.test_plan_uuid, callback.test_result.testResultUuid)
        manager.testPlanUpdated(callback.test_plan_uuid)
    }

    @ApiOperation(value="Callback from curator")
    @ApiResponses(value = [
            @ApiResponse(code = 400, message = 'Bad Request'),
            @ApiResponse(code = 404, message = 'Could not find package with that packageId'),
    ])
    @PostMapping('/on-change/')
    @ResponseBody
    void onChange(@Valid @RequestBody CuratorCallback callback) {
        log.info("/api/v1/test-plans/on-change (test update notification received from curator. uuid=${callback.test_plan_uuid} with status=${callback.status})")
        testService.updatePlanStatus(callback.test_plan_uuid, callback.status)
        manager.testPlanUpdated(callback.test_plan_uuid)
    }

    // Network Services
    @ApiOperation(value="Find all tests related with a service uuid")
    @GetMapping('/services/{nsdUuid}/tests')
    List<Object> listTestsByService(@PathVariable('nsdUuid') String uuid) {
        log.info("/api/v1/test-plans/services/{nsdUuid}/tests (list tests by service uuid request received. UUID=${uuid}")
        networkServiceService.findTestsByService(uuid)
    }

    @ApiOperation(value="Find all tests related with a testing_tag")
    @GetMapping('/testing-tags/{tag}/tests')
    List<Object> listTestsByTag(@PathVariable('tag') String tag) {
        log.info("/api/v1/test-plans/testing-tags/{tag}/tests (list tests by tag request received. Testing-tag=${tag}")
        networkServiceService.findTestsByTag(tag)
    }

    // Tests

    @ApiOperation(value="Find all services related with a test")
    @GetMapping('/tests/{testdUuid}/services')
    List<Object> listServicesByTest(@PathVariable('testdUuid') String uuid) {
        log.info("/api/v1/test-plans/tests/{testdUuid}/services (list services by test uuid request received. UUID=${uuid}")
        testService.findServicesByTest(uuid)
    }

    @ApiOperation(value="Find all services related with a tag")
    @GetMapping('/testing-tags/{tag}/services')
    List<Object> listServicesByTag(@PathVariable('tag') String tag) {
        log.info("/api/v1/test-plans/tests/{tag}/services (list services by tag request received. Testing-tag=${tag}")
        testService.findServicesByTag(tag)
    }
}
