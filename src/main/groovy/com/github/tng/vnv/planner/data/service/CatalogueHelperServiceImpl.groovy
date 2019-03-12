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

package com.github.tng.vnv.planner.data.service

import com.github.tng.vnv.planner.data.repository.NetworkServiceRepository
import com.github.tng.vnv.planner.data.repository.PackageRepository
import com.github.tng.vnv.planner.data.repository.TestRepository
import com.github.tng.vnv.planner.model.NetworkServiceDescriptor
import com.github.tng.vnv.planner.model.PackageMetadata
import com.github.tng.vnv.planner.model.TestDescriptor
import com.github.tng.vnv.planner.model.Test
import groovy.util.logging.Log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

import static com.github.tng.vnv.planner.helper.DebugHelper.callExternalEndpoint
import static com.github.tng.vnv.planner.helper.DebugHelper.nsAndTestsMappingToString

@Log
@Service("CatalogueHelperService")
class CatalogueHelperServiceImpl implements CatalogueHelperService {

    @Autowired
    @Qualifier('restTemplateWithAuth')
    RestTemplate restTemplateWithAuth

    @Autowired
    @Qualifier('restTemplateWithoutAuth')
    RestTemplate restTemplate

    @Autowired
    PackageRepository packageRepository

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


    @Override
    Map discoverAssociatedNssAndTests(PackageMetadata packageMetadata) {
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
        packageMetadata.testSuites?.each { ts -> ts.testd.testExecution?.each { tag ->
            if(!tag.testTag.isEmpty()) {
                catalogueOld.findNssByTestTag(tag.testTag)?.each { ns ->
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

    PackageMetadata loadByMetadata(PackageMetadata packageMetadata) {
        if(!packageMetadata) return
        PackageMetadata metadata = new PackageMetadata();

        packageMetadata.networkServices?.each { ns ->
            def s = networkServiceRepository.findByUuid(ns.networkServiceId)
            if(s) metadata.networkServices << s
        }

        packageMetadata.testSuites?.each { ts ->
            def t = testRepository.findByUuid(ts.testUuid)
            if(t) metadata.testSuites << t
        }

        log.info("##vnvlog: \nnetworkServices: ${metadata.networkServices}, \ntestSuites: ${metadata.testSuites}")
        metadata
    }

    PackageMetadata loadPackageMetadata(String packageId) {
        def rawPackageMetadata= callExternalEndpoint(restTemplate.getForEntity(packageMetadataEndpoint,Object.class,packageId),
                'TestCatalogue.loadPackageMetadata',packageMetadataEndpoint).body

        PackageMetadata packageMetadata=new PackageMetadata(packageId: packageId)
        rawPackageMetadata?.pd?.package_content.each{resource ->
            switch (resource.get('content-type')) {
                case 'application/vnd.5gtango.tstd':
                    TestDescriptor ts = testRepository.findByUuid(resource.uuid)
                    log.info("##vnvlog res: testSuite: $ts")
                    log.info("##vnvlog agnostic obj " + testRepository.printAgnosticObjByUuid(resource.uuid))
                    if(ts.testUuid)
                        packageMetadata.testSuites << ts
                    break
                case 'application/vnd.5gtango.nsd':
                    NetworkServiceDescriptor ns =  networkServiceRepository.findByUuid(resource.uuid)
                    log.info("##vnvlog Request: res: networkService: $ns")
                    log.info("##vnvlog agnostic obj: " + networkServiceRepository.printAgnosticObjByUuid(resource.uuid))
                    if(ns.networkServiceId)
                        packageMetadata.networkServices << ns
                    break
            }
        }
        packageMetadata
    }

    //todo: this is a workaround - to add the packageId to the Test - until the packageId be removed from the source
    def addPackageIdToTestSuit(PackageMetadata metadata, Test ts) {
        ts.packageId = metadata.packageId?: catalogueOld.findPackageId(ts)
        ts
    }
}
