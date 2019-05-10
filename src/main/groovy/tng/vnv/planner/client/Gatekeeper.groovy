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

package tng.vnv.planner.client

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

import tng.vnv.planner.model.TestSet

import javax.xml.ws.WebServiceException

@Component
class Gatekeeper {
    @Autowired
    @Qualifier('restTemplateWithoutAuth')
    RestTemplate restTemplate

    @Value('${app.gatekeeper.package.metadata.endpoint}')
    def packageMetadataEndpoint

    @Value('${app.gatekeeper.package.list.endpoint}')
    def packageListEndpoint

    @Value('${app.gatekeeper.test.metadata.endpoint}')
    def testMetadataEndpoint

    @Value('${app.gatekeeper.service.metadata.endpoint}')
    def serviceMetadataEndpoint

    Object getPackage(def packageId) throws RestClientException {
        def headers = new HttpHeaders()
        headers.add("Content-Type", "application/json")
        restTemplate.exchange(packageMetadataEndpoint as String, HttpMethod.GET,  new HttpEntity<Object>(headers), Object.class, packageId).body
    }

    Object[] getPackageByTag(def tag){
        def headers = new HttpHeaders()
        headers.add("Content-Type", "application/json")
        def builder = UriComponentsBuilder.fromUriString(packageListEndpoint)
                .queryParam("package_content.testing_tags", tag)
        restTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<Object>(headers), Object[]).body
    }

    Object getPackageByTest(def uuid){
        def headers = new HttpHeaders()
        headers.add("Content-Type", "application/json")
        def builder = UriComponentsBuilder.fromUriString(packageListEndpoint)
                .queryParam("package_content.uuid", uuid)
        restTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<Object>(headers), Object).body
    }

    Object getPackageByService(def uuid){
        def headers = new HttpHeaders()
        headers.add("Content-Type", "application/json")
        def builder = UriComponentsBuilder.fromUriString(packageListEndpoint)
                .queryParam("package_content.uuid", uuid)
        restTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<Object>(headers), Object).body
    }

    Object getTest(def uuid){
        def headers = new HttpHeaders()
        headers.add("Content-Type", "application/json")
        restTemplate.exchange(testMetadataEndpoint as String, HttpMethod.GET,  new HttpEntity<Object>(headers), Object.class, uuid).body
    }

    Object getService(def uuid){
        def headers = new HttpHeaders()
        headers.add("Content-Type", "application/json")
        restTemplate.exchange(serviceMetadataEndpoint as String, HttpMethod.GET,  new HttpEntity<Object>(headers), Object.class, uuid).body
    }

}
