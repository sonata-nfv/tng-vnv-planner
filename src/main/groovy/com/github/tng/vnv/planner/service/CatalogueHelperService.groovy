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

package com.github.tng.vnv.planner.service

import com.github.tng.vnv.planner.model.NetworkService
import com.github.tng.vnv.planner.repository.NetworkServiceRepository
import com.github.tng.vnv.planner.repository.TestRepository
import com.github.tng.vnv.planner.model.Package
import com.github.tng.vnv.planner.model.Test
import groovy.util.logging.Log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

import static com.github.tng.vnv.planner.utils.DebugHelper.callExternalEndpoint
import static com.github.tng.vnv.planner.utils.DebugHelper.nsAndTestsMappingToString

@Log
@Service
class CatalogueHelperService {

    @Autowired
    @Qualifier('restTemplateWithAuth')
    RestTemplate restTemplateWithAuth

    @Autowired
    @Qualifier('restTemplateWithoutAuth')
    RestTemplate restTemplate

    @Autowired
    TestRepository testRepository

    @Autowired
    NetworkServiceRepository networkServiceRepository

    @Value('${app.vnvgk.test.metadata.endpoint}')
    def testMetadataEndpoint

    @Value('${app.gk.service.metadata.endpoint}')
    def serviceMetadataEndpoint

    @Value('${app.gk.package.metadata.endpoint}')
    def packageMetadataEndpoint

    @Value('${app.gk.package.list.endpoint}')
    def packageListEndpoint

    Map discoverAssociatedNssAndTests(Package packageMetadata) {
        packageMetadata = (packageMetadata != null && packageMetadata.packageId == null) ?
                packageMetadata : loadPackageMetadata(packageMetadata.packageId)

        //  ------------------------------------------

        packageMetadata = loadByMetadata(packageMetadata)
        //  ------------------------------------------

        if(!packageMetadata) return
        def nsAndTestsMapping = [:] as HashMap
        def tss = [] as Set

        //notes: loadByPackageId the nsAndTestsMapping with all the given services
        packageMetadata.networkServices?.each { ns ->
            ns.nsd.testingTags?.each { tag ->
                testRepository.findTssByTestTag(tag)?.each { ts ->
                    ts = addPackageIdToTestSuit(packageMetadata,ts)
                    tss << ts
                }
            }
            if(!nsAndTestsMapping.containsKey(ns))
                nsAndTestsMapping.put(ns,tss)
            else
                nsAndTestsMapping.put(ns, tss << nsAndTestsMapping.get(ns))
            tss = []
        }

        //notes: loadByPackageId the nsAndTestsMapping with all the associated services according to the given tests
        packageMetadata.tests?.each { ts -> ts.testd.testExecution?.each { tag ->
            if(!tag.testTag.isEmpty()) {
                networkServiceRepository.findNssByTestTag(tag.testTag)?.each { ns ->
                    ts = addPackageIdToTestSuit(packageMetadata,ts)
                    if(!nsAndTestsMapping.containsKey(ns))
                        nsAndTestsMapping.put(ns, tss = [] << ts)
                    else
                        nsAndTestsMapping.put(ns, tss = nsAndTestsMapping.get(ns) << ts)
                }
            }
        }
        }
        if(nsAndTestsMapping.keySet().size() == 0
                || nsAndTestsMapping.values().first() == null
                || nsAndTestsMapping.values()?.first().size() == 0 ) {
            return
        }
        log.info(nsAndTestsMappingToString(nsAndTestsMapping))

        nsAndTestsMapping
    }

    Package loadByMetadata(Package packageMetadata) {
        if(!packageMetadata) return
        Package metadata = new Package();

        packageMetadata.networkServices?.each { ns ->
            def s = networkServiceRepository.findByUuid(ns.uuid)
            if(s) metadata.networkServices << s
        }

        packageMetadata.tests?.each { ts ->
            def t = testRepository.findByUuid(ts.uuid)
            if(t) metadata.tests << t
        }

        log.info("##vnvlog: \nnetworkServices: ${metadata.networkServices}, \ntests: ${metadata.tests}")
        metadata
    }

    Package loadPackageMetadata(String packageId) {
        def rawPackageMetadata= callExternalEndpoint(restTemplate.getForEntity(packageMetadataEndpoint,Object.class,packageId),
                'TestCatalogue.loadPackageMetadata',packageMetadataEndpoint).body

        Package packageMetadata=new Package(packageId: packageId)
        rawPackageMetadata?.pd?.package_content.each{resource ->
            switch (resource.get('content-type')) {
                case 'application/vnd.5gtango.tstd':
                    Test ts = testRepository.findByUuid(resource.uuid)
                    log.info("##vnvlog res: testSuite: $ts")
                    log.info("##vnvlog agnostic obj " + testRepository.printAgnosticObjByUuid(resource.uuid))
                    if(ts.uuid)
                        packageMetadata.tests << ts
                    break
                case 'application/vnd.5gtango.nsd':
                    NetworkService ns =  networkServiceRepository.findByUuid(resource.uuid)
                    log.info("##vnvlog Request: res: networkService: $ns")
                    log.info("##vnvlog agnostic obj: " + networkServiceRepository.printAgnosticObjByUuid(resource.uuid))
                    if(ns.uuid)
                        packageMetadata.networkServices << ns
                    break
            }
        }
        packageMetadata
    }

    //todo: this is a workaround - to send the packageId to the Test - until the packageId be removed from the source
    def addPackageIdToTestSuit(Package metadata, Test ts) {
        ts.packageId = metadata.packageId?: findPackageId(ts)
        ts
    }

    //todo: this is a workaround solution to bypass the null packageId issue for test's
    //todo-y2: remove the packageId from all the TestSuite,TestPlan,TestResult
    def findPackageId(Test test){
        callExternalEndpoint(restTemplateWithAuth.getForEntity(packageListEndpoint, Object[]),
                'TestCatalogue.findPackageId', packageListEndpoint).body?.find { p ->
            p.pd.package_content?.find { pc ->
                pc.get('content-type') == "application/vnd.5gtango.tstd"
            }?.get("uuid") == test.uuid
        }?.get("uuid")
    }

}
