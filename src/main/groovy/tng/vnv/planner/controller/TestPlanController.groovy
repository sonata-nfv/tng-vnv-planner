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
import tng.vnv.planner.ScheduleManager
import tng.vnv.planner.WorkflowManager
import tng.vnv.planner.model.CuratorCallback
import tng.vnv.planner.model.TestPlan

import tng.vnv.planner.model.TestSet
import tng.vnv.planner.service.NetworkService
import tng.vnv.planner.service.TestService
import groovy.util.logging.Slf4j
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import tng.vnv.planner.service.TDService

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
    @Autowired
    TDService tdService

    @GetMapping
    @ApiOperation(value="Find all test plan", notes="Finding all test plans")
    @ResponseBody
    List<TestPlan> listAllTestPlans() {
        testService.findAll()
    }

    @GetMapping('/{uuid}')
    @ApiOperation(value="Find a test plan", notes="Finding test plan by uuid")
    @ResponseBody
    TestPlan findTestPlan(@PathVariable UUID uuid) {
        testService.findPlanByUuid(uuid)
    }

    @DeleteMapping('{uuid}')
    @ApiOperation(value="Delete a test plan", notes="deleting test plan by uuid")
    @ResponseBody
    void deleteTestPlan(@PathVariable UUID uuid) {
        manager.deleteTestPlan(uuid)
    }

    @ApiOperation(value="Create a test plan", notes="Creating a test plan")
    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PostMapping('')
    @ResponseBody
    TestPlan save(@Valid @RequestBody TestPlan testPlan) {
        scheduler.scheduleNewTestSet(testPlan)
    }

    @ApiOperation(value="Update a test plan", notes="Updating a test plan by uuid")
    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PutMapping('{uuid}')
    @ResponseBody
    TestPlan update(@Valid @PathVariable UUID uuid, @Valid @RequestBody TestPlan testPlan) {
        scheduler.update(uuid, testPlan.status)
    }

    @ApiOperation(value="Create a test plan by service uuid")
    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PostMapping('/services')
    @ResponseBody
    List<TestPlan> buildTestPlansByService(@Valid @RequestParam UUID serviceUuid) {
       testService.buildTestPlansByService(serviceUuid).testPlans
    }

    @ApiOperation(value="Create a test plan by test uuid")
    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PostMapping('/tests')
    @ResponseBody
    List<TestPlan> buildTestPlansByTest(@Valid @RequestParam UUID testUuid) {
        testService.buildTestPlansByTest(testUuid).testPlans
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
        log.info("Received finished test plan notification from Curator: test plan uuid = ${callback.test_plan_uuid}")
        testService.updatePlan(callback.test_plan_uuid as UUID, callback.status)
        manager.testPlanFinished(callback.test_plan_uuid as UUID)
    }

    @ApiOperation(value="Callback from curator")
    @ApiResponses(value = [
            @ApiResponse(code = 400, message = 'Bad Request'),
            @ApiResponse(code = 404, message = 'Could not find package with that packageId'),
    ])
    @PostMapping('/on-change/')
    @ResponseBody
    void onChange(@Valid @RequestBody CuratorCallback callback) {
        log.info("Received test plan notification from Curator: test plan uuid = ${callback.test_plan_uuid} with status = ${callback.status}")
        testService.updatePlan(callback.test_plan_uuid as UUID, callback.status)
        manager.testPlanFinished(callback.test_plan_uuid as UUID)
    }

    // Network Services
    @ApiOperation(value="Find all tests related with a service uuid")
    @GetMapping('/services/{nsdUuid}/tests')
    List<TestSet> listTestsByService(@PathVariable('nsdUuid') UUID uuid) {
        networkServiceService.findByTest(uuid)
    }

    // Tests

    @ApiOperation(value="Find all services related with a test")
    @GetMapping('/tests/{testdUuid}/services')
    List<String> listServicesByTest(@PathVariable('testdUuid') UUID uuid) {
        testService.findByService(uuid)
    }
}