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

package com.github.tng.vnv.planner.client

import com.github.tng.vnv.planner.model.TestPlanRequest
import com.github.tng.vnv.planner.model.TestPlanResponse
import com.github.tng.vnv.planner.model.TestPlan
import groovy.util.logging.Log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

import static com.github.tng.vnv.planner.utils.DebugHelper.callExternalEndpoint

@Component
@Log
class Curator {

    @Autowired
    @Qualifier('restTemplateWithAuth')
    RestTemplate restTemplate

    @Value('${app.curator.test.plan.prepare.endpoint}')
    def testPlanPrepareEndpoint
    @Value('${app.curator.test.plan.cancel.endpoint}')
    def testPlanCancellationEndpoint
    @Value('${app.curator.ping.endpoint}')
    def testPlanPingEndpoint



    boolean inRunning() {
        (callExternalEndpoint(restTemplate.getForEntity(testPlanPingEndpoint, Object.class),
                'Curator.isRunning()',testPlanPingEndpoint).body.alive_since != null )
    }

    TestPlanResponse proceedWith(TestPlan testPlan) {
        def testPlanRequest = new TestPlanRequest(testPlanUuid: testPlan.uuid, nsd: testPlan.nsd, testd: testPlan.testd)
        callExternalEndpoint(restTemplate.postForEntity(testPlanPrepareEndpoint, testPlanRequest, testPlanRequest),
                'Curator.proceedWith(TestPlan)',testPlanPrepareEndpoint).body
    }

    void deleteTestPlan(uuid) {
        callExternalEndpoint(restTemplate.delete(testPlanCancellationEndpoint, uuid),
                'Curator.deleteTestPlan(TestPlan)',testPlanCancellationEndpoint)
    }

    TestPlan update(def testPlan) {
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        def entity = new HttpEntity<TestPlan>(testPlan ,headers)
        callExternalEndpoint(restTemplate.exchange(testPlanUpdateEndpoint, HttpMethod.PUT, entity, TestPlan.class ,testPlan.id),'TestResultRepository.updatePlan',testPlanUpdateEndpoint).body
    }
}
