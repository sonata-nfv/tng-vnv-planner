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

package com.github.tng.vnv.planner.data.repository

import com.github.tng.vnv.planner.helper.DebugHelper
import com.github.tng.vnv.planner.model.TestDescriptor
import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.oldlcm.model.TestSuiteOld
import groovy.util.logging.Log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

import static com.github.tng.vnv.planner.helper.DebugHelper.callExternalEndpoint

@Log
@Repository("TestRepository")
class TestRepositoryImpl implements TestRepository {

    @Autowired
    @Qualifier('restTemplateWithAuth')
    RestTemplate restTemplateWithAuth

    @Value('${app.vnvgk.test.metadata.endpoint}')
    def testMetadataEndpoint

    @Value('${app.vnvgk.test.list.by.tag.endpoint}')
    def testListByTagEndpoint

    @Value('${app.gk.service.metadata.endpoint}')
    def serviceMetadataEndpoint


    @Override
    TestSuiteOld findByUuid(String uuid) {
        TestSuiteOld ts = callExternalEndpoint(restTemplateWithAuth.getForEntity(testMetadataEndpoint, TestSuiteOld.class, uuid),
                'TestCatalogue.findNssByTestTag','TestCatalogue.loadPackageMetadata',testMetadataEndpoint).body

    }

    String printAgnosticObjByUuid(String uuid) {
        callExternalEndpoint(
                restTemplateWithAuth.getForEntity(testMetadataEndpoint, Object.class, uuid),
                'TestCatalogue.loadPackageMetadata','TestCatalogue.loadPackageMetadata',
                testMetadataEndpoint).body.each {println it}
    }

    List<TestDescriptor> findTssByTestTag(String tag) {
		UriComponentsBuilder builder = UriComponentsBuilder
		.fromUriString(testListByTagEndpoint)
		.queryParam("testing_tag", tag)
		DebugHelper.callExternalEndpoint(restTemplateWithAuth.getForObject(builder.toUriString(), TestDescriptor[]),
                'TestPlanRepositoryImpl.findTssByTestTag',testListByTagEndpoint).body
    }


}
