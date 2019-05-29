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

package tng.vnv.planner.restMocks

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import tng.vnv.planner.model.CuratorCallback
import tng.vnv.planner.model.PackageCallback

@Component
class PlannerClient {

    @Autowired
    @Qualifier('restTemplateWithoutAuth')
    RestTemplate restTemplate

    ResponseEntity postPackageChanged(def packageId, def confirmRequired){
        def packageCallback = new PackageCallback(packageId: packageId, confirmRequired: confirmRequired)
        restTemplate.postForEntity('http://localhost:6100/api/v1/packages', PackageCallback, ResponseEntity.class)
    }

    ResponseEntity curatorCallback(def eventActor, def testPlanUuid, def status){
        def curatorCallback = new CuratorCallback(eventActor: eventActor, status: status, testPlanUuid: testPlanUuid)

        restTemplate.postForEntity('http://localhost:6100/api/v1/test-plans/on-change', CuratorCallback, ResponseEntity.class)
    }

    ResponseEntity curatorCompletedCallback(def eventActor, def testPlanUuid, def status, def testResult){
        def curatorCallback = new CuratorCallback(eventActor: eventActor, status: status, testResults: testResult, testPlanUuid: testPlanUuid)

        restTemplate.postForEntity('http://localhost:6100/api/v1/test-plans/on-change/completed', CuratorCallback, ResponseEntity.class)
    }
}
