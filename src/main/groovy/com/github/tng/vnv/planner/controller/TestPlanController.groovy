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

package com.github.tng.vnv.planner.controller

import com.github.tng.vnv.planner.ScheduleManager
import com.github.tng.vnv.planner.WorkflowManager
import com.github.tng.vnv.planner.model.NetworkServiceDescriptor
import com.github.tng.vnv.planner.model.TestDescriptor
import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.model.TestSuite
import com.github.tng.vnv.planner.service.TestPlanService
import groovy.util.logging.Log
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

import javax.validation.Valid

@Log
@RestController
@RequestMapping('/api/v1/test-plans')
class TestPlanController {

    @Autowired
    ScheduleManager scheduler
    @Autowired
    WorkflowManager manager
    @Autowired
    TestPlanService testPlanService

    @GetMapping('/{testPlanListUuid}')
    @ResponseBody
    List<TestPlan> listByTestSuite(@PathVariable('testPlanListUuid') String uuid) {
        (uuid == '0')? testPlanService.findAll():testPlanService.findByTestSuiteUuid(uuid)
    }

    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PostMapping('')
    @ResponseBody
    TestSuite save(@Valid @RequestBody TestSuite testSuite) {
        log.info("#~#vnvlogPlanner.TestPlanController.save: TestSuite.uuid: ${testSuite?.uuid} STR [PackageCallback: ${testSuite}]")
        scheduler.create(testSuite)
        log.info("#~#vnvlogPlanner.TestPlanController.save: TestSuite.uuid: ${testSuite?.uuid} END [PackageCallback: ${testSuite}]")
    }

    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PutMapping('{uuid}')
    @ResponseBody
    TestSuite update(@Valid @RequestBody TestSuite testSuite) {
        log.info("#~#vnvlogPlanner.TestPlanController.update: TestSuite.uuid: ${testSuite?.uuid} STR [PackageCallback: ${testSuite}]")
        scheduler.update(testSuite)
        log.info("#~#vnvlogPlanner.TestPlanController.update: TestSuite.uuid: ${testSuite?.uuid} END [PackageCallback: ${testSuite}]")
    }

    @DeleteMapping('{uuid}')
    @ResponseBody
    void deleteTestPlan(@PathVariable String uuid) {
        log.info("#~#vnvlogPlanner.TestPlanController.deleteTestPlan: TestSuite.uuid: $uuid STR")
        manager.deleteTestPlan(uuid)
        log.info("#~#vnvlogPlanner.TestPlanController.deleteTestPlan: TestSuite.uuid: $uuid END")
    }

    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PostMapping('/services')
    @ResponseBody
    List<TestPlan> createTestPlansByServiceDescriptor(@Valid @RequestBody NetworkServiceDescriptor body) {
        testPlanService.createByService(body)
    }

    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PostMapping('/tests')
    @ResponseBody
    List<TestPlan> createTestPlansByTestDescriptor(@Valid @RequestBody TestDescriptor body) {
        testPlanService.createByTest(body)
    }
}