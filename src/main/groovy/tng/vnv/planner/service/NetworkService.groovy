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

package tng.vnv.planner.service

import groovy.util.logging.Slf4j
import tng.vnv.planner.client.Gatekeeper

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tng.vnv.planner.model.TestSet
import tng.vnv.planner.repository.TestSetRepository

@Slf4j
@Service
class NetworkService {

    @Autowired
    Gatekeeper gatekeeper

    @Autowired
    TestSetRepository testSetRepository

    List findTestsByService(def serviceUuid){
        def matchedTests = [] as HashSet<TestSet>
        def packs = gatekeeper.getPackageByUuid(serviceUuid).body
        if(packs != null){
            packs.each { pack ->
                pack.pd.package_content.each { resource ->
                    if (resource.get('content-type') == 'application/vnd.5gtango.nsd'
                            || resource.get('content-type') == 'application/vnd.etsi.osm.nsd') {
                        def testing_tag = resource.get('testing_tags')
                        testing_tag.each { tt ->
                            log.info("including tests with tag: ${tt}")
                            matchedTests << findTestsByTag(tt)
                        }
                    }
                }
            }
        }
        new ArrayList(matchedTests)
    }

    List findTestsByTag(def tag){
        def matchedTests = [] as HashSet<TestSet>
        def packs = gatekeeper.getPackageByTag(tag).body
        if(packs != null){
            packs.each { pack ->
                pack.pd.package_content.each { resource ->
                    if (resource.get('content-type') == 'application/vnd.5gtango.tstd'
                            || resource.get('content-type') == 'application/vnd.etsi.osm.tstd') {
                        log.info("including tests that match with test uuid: ${resource.uuid}")
                        matchedTests << gatekeeper.getTest(resource.uuid)
                    }
                }
            }
        }
        new ArrayList(matchedTests)
    }
}
