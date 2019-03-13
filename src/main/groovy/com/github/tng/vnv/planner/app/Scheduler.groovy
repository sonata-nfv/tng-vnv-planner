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

import com.github.tng.vnv.planner.Applicant
import com.github.tng.vnv.planner.service.CatalogueService
import com.github.tng.vnv.planner.service.TestPlanService
import com.github.tng.vnv.planner.model.Package
import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.queue.TestPlanProducer
import groovy.util.logging.Log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

import java.util.concurrent.CompletableFuture


@Log
@Component
class Scheduler extends Applicant {

    @Autowired
    TestPlanProducer testPlanProducer

    @Autowired
    TestPlanService testPlanService

    @Autowired
    CatalogueService catalogueService

    @Async
    CompletableFuture<Boolean> schedule(Package packageMetadata) {
        def map = catalogueService.discoverAssociatedNssAndTests(packageMetadata)

        Boolean out = (map == null) ? false : map.every {nsd,td ->
//            schedule(testPlanService.createTestPlan(nsd: nsd, TestDescriptor: td) == true)
        }
        CompletableFuture.completedFuture(out)
    }

    def schedule(TestPlan testPlan) {
        testPlanProducer.add(testPlan.uuid).to("TEST_PLAN_MESSAGE_QUEUE")
        testPlanProducer.update(testPlan.uuid).to("TEST_SUITE_MESSAGE_QUEUE")
    }

    def schedule(List<TestPlan> testPlanList) {
        testPlanList?.forEach({tp -> schedule(tp)})
    }
}
