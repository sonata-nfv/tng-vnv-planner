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

import com.github.tng.vnv.planner.model.NetworkServiceDescriptor
import com.github.tng.vnv.planner.model.TestDescriptor
import com.github.tng.vnv.planner.model.TestSuite
import com.github.tng.vnv.planner.service.TestPlanService
import com.github.tng.vnv.planner.service.TestSuiteService
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import javax.validation.Valid

@RestController
@RequestMapping('/api/v1/test-plans')
class TestPlanController {


    @Autowired
    TestSuiteService testSuiteService

    @Autowired
    TestPlanService testPlanService

    @GetMapping('{uuid}')
    TestSuite findOne(@PathVariable String uuid) {
        testPlanService.findByUuid(uuid)
    }

    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PostMapping('')
    ResponseEntity<Void> save(@Valid @RequestBody TestSuite body) {
        testPlanService.save(body)
        ResponseEntity.ok().build()
    }

    @PutMapping('{uuid}')
    TestSuite update(@RequestBody TestSuite request, @PathVariable String uuid) {
        testPlanService.update(request, uuid)
    }

    @DeleteMapping('{uuid}')
    TestSuite deleteById(@PathVariable String uuid) {
        testPlanService.deleteByUuid(uuid)
    }

    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PostMapping('/services')
    TestSuite createTestPlansByServiceDescriptor(@Valid @RequestBody NetworkServiceDescriptor body) {
        testPlanService.findByService(body)
    }

    @ApiResponses(value = [@ApiResponse(code = 400, message = 'Bad Request')])
    @PostMapping('/tests')
    TestSuite createTestPlansByTestDescriptor(@Valid @RequestBody TestDescriptor body) {
        testPlanService.findByTest(body)
    }
}