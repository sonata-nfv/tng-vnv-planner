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

package com.github.tng.vnv.planner.controller

import com.github.tng.vnv.planner.ScheduleManager
import com.github.tng.vnv.planner.WorkflowManager
import com.github.tng.vnv.planner.aspect.TriggerNextTestPlan
import com.github.tng.vnv.planner.model.NetworkService
import com.github.tng.vnv.planner.model.Test
import com.github.tng.vnv.planner.model.TestPlan
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

    @GetMapping
    @ResponseBody
    List<TestPlan> listAllTestPlans() {
        testPlanService.findAll()
    }

    @GetMapping('/{uuid}')
    @ResponseBody
    TestPlan findTestPlan(@PathVariable String uuid) {
        testPlanService.findByUuid(uuid)
    }

    @DeleteMapping('{uuid}')
    @ResponseBody
    void deleteTestPlan(@PathVariable String uuid) {
        log.info("#~#vnvlog deleteTestPlan STR [test_plan.uuid: ${uuid}]")
        manager.deleteTestPlan(uuid)
        log.info("#~#vnvlog deleteTestPlan END [test_plan.uuid: ${uuid}]")
    }

    @TriggerNextTestPlan
    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PostMapping('')
    @ResponseBody
    TestPlan save(@Valid @RequestBody TestPlan testPlan) {
        scheduler.create(testPlan)
    }

    @TriggerNextTestPlan
    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PutMapping('{uuid}')
    @ResponseBody
    TestPlan update(@Valid @RequestBody TestPlan testPlan) {
        scheduler.update(testPlan)
    }

    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PostMapping('/services')
    @ResponseBody
    List<TestPlan> buildTestPlansByService(@Valid @RequestBody NetworkService body) {
        new ArrayList(testPlanService.buildTestPlansByService(body.uuid))
    }

    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PostMapping('/tests')
    @ResponseBody
    List<TestPlan> buildTestPlansByTest(@Valid @RequestBody Test body) {
        new ArrayList(testPlanService.buildTestPlansByTest(body.uuid))
    }
}