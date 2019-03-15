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


import com.github.tng.vnv.planner.model.TestDescriptor
import com.github.tng.vnv.planner.queue.TestPlanConsumer
import com.github.tng.vnv.planner.queue.TestPlanProducer
import com.github.tng.vnv.planner.service.NetworkServiceService
import com.github.tng.vnv.planner.service.TestService
import groovy.util.logging.Log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import java.sql.Timestamp

@Log
@RestController
@RequestMapping('/api/v1/test-plans')
class DummyQueueController {

    @Autowired
    TestPlanProducer testPlanProducer

    @Autowired
    TestPlanConsumer testPlanConsumer

    @GetMapping('/queue/{action}')
    ResponseEntity<Void> testQueue(@PathVariable('action') String action) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        log.info("##DummyQueueController: Access to /api/v1/test-plans/queue/{action} endpoint  with the received action: $action")
        def o
        if('add'.contains(action)) {
            o = testPlanProducer.send(timestamp.toString())
            log.info("##DummyQueueController: call testPlanProducer to add a timestamp message: $timestamp")

        }
        else {
            o = testPlanConsumer.receive()
            log.info("##DummyQueueController: call testPlanConsumer to receive a message")
        }

        ResponseEntity.ok().build()
    }
}