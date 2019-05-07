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

package com.github.tng.vnv.planner.restmock

import com.github.tng.vnv.planner.model.NetworkService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import com.github.tng.vnv.planner.model.Test

@RestController
class CatalogueMock {

    static String TESTD_UUID_HTTP_BENCHMARK_TEST_1 ='input0ts-75f5-4ca1-90c8-12ec80a79836' //test_tags: latency (#4), http (#4)
    static String NSD_UUID_NS_SQUID ='4dd4cb15-76b8-46fd-b3c0-1b165cc332f9' //test_tags: latency (#3), aux_test
    static String MOCKED_TEST_PLANS_MOCKED_PACKAGE_ID_FOR_HTTP_BENCHMARK_TEST_1_AND_NS_SQUID ='mocked_input0ts-75f5-4ca1-90c8-12ec80a79836_4dd4cb15-76b8-46fd-b3c0-1b165cc332f9'

    @GetMapping('/mock/gk/packages')
    def findPackages(@RequestParam(name='package_content.testing_tags',required = false) String testingTags,
                     @RequestParam(name='package_content.uuid', required = false) String uuid) {
        if (testingTags != null)
            DataMock.getPackageByTag(testingTags)
        else if (uuid != null)
            DataMock.getPackageByUuid(uuid)
        else
            DataMock.packages
    }

    @GetMapping('/mock/gk/packages/{packageId:.+}')
    Map loadPackageMetadata(@PathVariable('packageId') String packageId) {
        if (packageId == MOCKED_TEST_PLANS_MOCKED_PACKAGE_ID_FOR_HTTP_BENCHMARK_TEST_1_AND_NS_SQUID) {
            [pd:[package_content:[
                    [
                            'uuid':TESTD_UUID_HTTP_BENCHMARK_TEST_1,
                            'content-type':'application/vnd.5gtango.tstd',
                            "testing_tags": [
                                    "http",
                                    "latency"
                            ]
                    ],
                    [
                            'uuid':NSD_UUID_NS_SQUID,
                            'content-type':'application/vnd.5gtango.nsd',
                            "testing_tags": [
                                    "proxy-advanced"
                            ]
                    ],
            ], test_type: 'fgh'],
            ]
        } else {
            DataMock.getPackage(packageId)
        }
    }

    @GetMapping('/mock/gk/services')
    List<NetworkService> findServices(@RequestParam(value='testing_tag',required=false) String tag) {
        if(!tag) {
            return DataMock.services
        }
        DataMock.getServiceByTag(tag)
    }

    @GetMapping('/mock/gk/services/{uuid:.+}')
    def findService(@PathVariable('uuid') String uuid) {
        DataMock.getService(uuid)
    }

    @GetMapping('/mock/gk/tests/descriptors')
    List<Test> findTests(@RequestParam(value='test_tag',required=false) String tag) {
		if(!tag) {
			return  DataMock.tests
		}
		DataMock.getTestByTag(tag)
    }

    @GetMapping('/mock/gk/tests/descriptors/{uuid:.+}')
    def findTest(@PathVariable('uuid') String uuid) {
        DataMock.getTest(uuid)
    }
	
}

