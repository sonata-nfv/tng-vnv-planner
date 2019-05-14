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
import org.springframework.web.client.RestClientException
import tng.vnv.planner.model.TestPlan
import tng.vnv.planner.model.TestSet
import tng.vnv.planner.utils.TestPlanStatus
import tng.vnv.planner.client.Gatekeeper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tng.vnv.planner.utils.TestSetType

@Slf4j
@Service
class PackageService {

    @Autowired
    Gatekeeper gatekeeper

    TestSet buildTestSetByPackage(packageId, confirmRequired, type = TestSetType.PACKAGE) throws RestClientException{
        if (packageId != null) {
            def matchedTests = [] as HashSet<Map>
            def matchedServices = [] as HashSet<Map>

            def pack = gatekeeper.getPackage(packageId)

            def testSet = new TestSet(uuid: UUID.randomUUID(),
                                        requestUuid: packageId as UUID,
                                        requestType: type,
                                        confirmRequired: confirmRequired,
                                        status: confirmRequired ? TestPlanStatus.WAITING_FOR_CONFIRMATION : TestPlanStatus.SCHEDULED)

            def testingTags = pack.pd.package_content.collect {it.testing_tags}
            testingTags?.each { tags ->
                tags?.each { tag ->
                    List packageList = gatekeeper.getPackageByTag(tag)
                    packageList?.each {
                        it?.pd?.package_content?.each { resource ->
                            switch (resource.get('content-type')) {
                                case 'application/vnd.5gtango.tstd':
                                    matchedTests << (resource as Map)
                                    break

                                case 'application/vnd.5gtango.nsd':
                                    matchedServices << (resource as Map)
                                    break

                                case 'application/vnd.etsi.osm.tstd':
                                    matchedTests << (resource as Map)
                                    break

                                case 'application/vnd.etsi.osm.nsd':
                                    matchedServices << (resource as Map)
                                    break

                                default: break
                            }
                        }
                    }
                }
            }

            log.info("matched pairs:")

            matchedServices.each { service ->
                matchedTests.each { test ->
                    log.info("test: ${test.uuid} - service: ${service.uuid}")
                    testSet.testPlans << new TestPlan(uuid: UUID.randomUUID(),
                                                testSetUuid: testSet.uuid,
                                                serviceUuid: UUID.fromString(service.uuid as String),
                                                testUuid: UUID.fromString(test.uuid as String),
                                                confirmRequired: confirmRequired,
                                                testStatus: confirmRequired ? TestPlanStatus.WAITING_FOR_CONFIRMATION : TestPlanStatus.SCHEDULED)
                }
            }

            return testSet
        }

        throw new IllegalArgumentException("PackageUUID cannot be null")
    }

    TestSet buidTestSetByTestAndService(testUuid, serviceUuid, confirmRequired, type){

        def testSet = new TestSet(uuid: UUID.randomUUID(),
                requestType: TestSetType.TEST_AND_SERVICE,
                confirmRequired: confirmRequired,
                status: confirmRequired ? TestPlanStatus.WAITING_FOR_CONFIRMATION : TestPlanStatus.SCHEDULED)

        testSet.testPlans << new TestPlan(uuid: UUID.randomUUID(),
                testSetUuid: testSet.uuid,
                serviceUuid: serviceUuid,
                testUuid: testUuid,
                confirmRequired: confirmRequired,
                testStatus: confirmRequired ? TestPlanStatus.WAITING_FOR_CONFIRMATION : TestPlanStatus.SCHEDULED)

        return  testSet
    }

    TestSet buildTestPlansByTestPackage(def uuid) {
        def pack = gatekeeper.getPackageByUuid(uuid)
        buildTestSetByPackage(pack.uuid, false, TestSetType.SERVICE)
    }

    TestSet buildTestPlansByServicePackage(def uuid) {
        def pack = gatekeeper.getPackageByUuid(uuid)
        buildTestSetByPackage(pack.uuid, false, TestSetType.TEST)
    }

    TestSet buildTestPlansByServiceAndTest(def testUuid, def serviceUuid){
        buidTestSetByTestAndService(testUuid, serviceUuid, confirmRequired: false, TestSetType.TEST_AND_SERVICE)
    }
}
