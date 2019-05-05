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

package com.github.tng.vnv.planner.service

import com.github.tng.vnv.planner.client.Gatekeeper
import com.github.tng.vnv.planner.model.NetworkService
import com.github.tng.vnv.planner.model.Package
import com.github.tng.vnv.planner.model.Test
import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.utils.TEST_PLAN_STATUS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import static org.springframework.util.StringUtils.isEmpty


@Service
class PackageService {

    @Autowired
    TestPlanService testPlanService

    @Autowired
    Gatekeeper gatekeeper

    Set<TestPlan> buildTestPlansByPackage(def packageId){
        def testPlans = [] as HashSet
        if(packageId!=null){
            def matchedTests = [] as HashSet<Test>
            def matchedServices = [] as HashSet<NetworkService>
            def pack = gatekeeper.getPackage(packageId).body
            def testingTags = pack.pd.package_content.collect {it.testing_tags}
            testingTags?.each { tags ->
                tags?.each { tag ->
                    List<Package> packageList = gatekeeper.getPackageByTag(tag).body
                    packageList?.each {
                        it?.pd?.package_content.each { resource ->
                            switch (resource.get('content-type')) {
                                case 'application/vnd.5gtango.tstd':
                                    matchedTests << ((isEmpty(resource.confirm_required))?
                                            new Test(uuid: resource.uuid, packageId: it.uuid):
                                            new Test(uuid: resource.uuid, packageId: it.uuid,
                                                    confirmRequired: resource.confirm_required))
                                    break
                                case 'application/vnd.5gtango.nsd':
                                    matchedServices << new NetworkService(uuid: resource.uuid, packageId: it.uuid)
                                    break
                            }
                        }
                    }
                }
            }

            matchedServices.each { service ->
                matchedTests.each { test ->
                    testPlans.add(new TestPlan(uuid: service.uuid+test.uuid,
                            serviceUuid: service.uuid,
                            servicePackageId: service.packageId,
                            testUuid: test.uuid,
                            testPackageId: test.packageId,
                            confirmRequired: test.confirmRequired,
                            status: TEST_PLAN_STATUS.CREATED))
                }
            }
        }
        testPlans
    }

    List buildTestPlansByTestPackage(def uuid){
        Package pack = gatekeeper.getPackageByTest(uuid).body
        new ArrayList(buildTestPlansByPackage(pack.uuid))
    }

    List buildTestPlansByServicePackage(def uuid){
        def pack = gatekeeper.getPackageByService(uuid).body
        new ArrayList(buildTestPlansByPackage(pack.uuid))
    }
}
