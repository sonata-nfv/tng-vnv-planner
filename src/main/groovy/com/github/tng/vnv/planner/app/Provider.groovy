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

package com.github.tng.vnv.planner.app

import com.github.tng.vnv.planner.client.Curator
import com.github.tng.vnv.planner.model.TEST_PLAN_STATUS
import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.model.TestPlanResponse
import com.github.tng.vnv.planner.queue.TestPlanConsumer
import com.github.tng.vnv.planner.service.TestPlanService
import groovy.util.logging.Log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Log
@Component
class Provider {

    @Autowired
    Curator curator

    @Autowired
    TestPlanConsumer testPlanConsumer

    @Autowired
    Scheduler scheduler

    @Autowired
    TestPlanService testPlanService

    def delegateNextTestPlan() {
        //fixme-gandreou: a) check the condition that the queue is empty of messages...
        //fixme-gandreou: b) or the messages are already cancelled&updated through another client call.

        TestPlan testPlan = testPlanConsumer.getTestPlan()

        TestPlanResponse testPlanResponse = curator.proceedWith(testPlan)

        if('202'.contains(testPlanResponse.status)){
            //fixme-gandreou: if res is valid, set TestPlan status like "proceeded to curator"
            // The Provider gets the message from the Queue, but without ack yet
            // The message acknowledgement could be used, to let the broker completely remove the message from a queue. This will happens
            // when it receives a notification for that message (or group of messages).
            testPlanResponse.testPlan.status = TEST_PLAN_STATUS.STARTING
            testPlanService.update(testPlanResponse.testPlan)
            // If curator is not responding as it should the message returns as a new plan in RabbitMQ
            // and then, send the ack back to the Broker.
        } else {
            // if Curator won't reply with HTTP Status Code 202 (The request has been accepted for processing)
            // it will response with a log, but the message will stay in the list

            log.info("Curator response wasn't 202. The current TestPlan has been rescheduled")
            testPlan.status = TEST_PLAN_STATUS.REJECTED
            scheduler.update(testPlan)
        }

    }

}
