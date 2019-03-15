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

package com.github.tng.vnv.planner.queue

import groovy.util.logging.Log
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Log
@Component
class TestPlanConsumer {

    @Autowired
    private RabbitTemplate template;

    @Autowired
    private Queue testPlansQueue;

    String id
    String action

    def add(String uuid) {
        action = 'ADD'
        id = uuid
        this
    }
    def remove(String uuid) {
        action = 'REMOVE'
        id = uuid
        this
    }


    def update(String uuid) {
        action = 'UPDATE'
        id = uuid
        this
    }

    def to(String mq) {
        //case ADD
        //todo-gandreou: return the result for the ADD of the item from the queue
        //case REMOVE
        //todo-gandreou: return the result for the REMOVE of the item from the queue
        //case UPDATE
        //todo-gandreou: return the result for the UPDATE of the item from the queue
    }

    def fromTestPlansQueue() {

        //cleancode-gandreou: bypass-all-of-these: I will send a message to 'Hello' queue
        receive()

    }

    void receive() {

        log.info("##Consumer:testPlansQueue: ${testPlansQueue.name} [before]")

//        Message message = this.template.receive()
        Message message = this.template.receive("tng-vnv-planner-test-plans")
//        Message message = this.template.receive(testPlansQueue.getName())
/*
        def receivedMassage = new String(message, "UTF-8")
        if (!receivedMassage.isEmpty()) {
            log.info("##Consumer:receives message: " + receivedMassage)
        } else
            log.info("##Consumer:didn't receive message!!!!")
*/

        log.info("##Consumer:testPlansQueue: ${testPlansQueue.name} [after]")

    }
}
