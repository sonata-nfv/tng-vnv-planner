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

package tng.vnv.planner


import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder
import spock.lang.Specification
import tng.vnv.planner.controller.PackageController
import tng.vnv.planner.model.PackageCallback
import tng.vnv.planner.repository.TestSetRepository
import tng.vnv.planner.utils.TestPlanStatus

@Transactional
@SpringBootTest(classes = [Application.class], webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class PackageControllerSpec extends Specification {

    @Autowired
    TestSetRepository testSetRepository

    @Autowired
    PackageController packageController

    @Autowired
    TestRestTemplate testRestTemplate

    @Test
    void "Get Package By Uuid"() {
        when:
        def headers = new HttpHeaders()
        headers.add("Content-Type", "application/json")
        def builder = UriComponentsBuilder.fromUriString("http://localhost:6100/catalogues/packages")
                .queryParam("package_content.uuid", "0d274a40-191a-4f6f-b6ef-2a6960c24bc2")
        def response = testRestTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<Object>(headers), Object).body
        then:
        response[0].uuid == "0d274a40-191a-4f6f-b6ef-2a6960c24bc2"
    }

    @Test
    def "Scheduled Test Set"(){
        when:
        def packageCallback = new PackageCallback(packageId: '0d274a40-191a-4f6f-b6ef-2a6960c24bc2', confirmRequired: false)
        def testPlan = packageController.onChange(packageCallback).body
        def aux = testSetRepository.findByUuid(testPlan[0].testSetUuid)
        then:
        aux.status == TestPlanStatus.SCHEDULED
    }

    @Test
    def "Waiting For Confirmation Test Set"(){
        when:
        def packageCallback = new PackageCallback(packageId: '0d274a40-191a-4f6f-b6ef-2a6960c24bc2', confirmRequired: true)
        def testPlan = packageController.onChange(packageCallback).body
        def aux = testSetRepository.findByUuid(testPlan[0].testSetUuid)
        then:
        aux.status == TestPlanStatus.WAITING_FOR_CONFIRMATION
    }
}
