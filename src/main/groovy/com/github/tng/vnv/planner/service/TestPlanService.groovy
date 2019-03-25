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
import com.github.tng.vnv.planner.model.Package
import com.github.tng.vnv.planner.model.Test
import com.github.tng.vnv.planner.model.TestDescriptor
import com.github.tng.vnv.planner.repository.TestPlanRepository
import com.github.tng.vnv.planner.repository.TestPlanRestRepository
import com.github.tng.vnv.planner.model.NetworkServiceDescriptor
import com.github.tng.vnv.planner.model.TestPlan
import groovy.util.logging.Log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import static com.github.tng.vnv.planner.utils.TEST_PLAN_STATUS.DELETED

@Log
@Service
class TestPlanService {

    @Autowired
    TestPlanRepository testPlanRepository
    @Autowired
    TestPlanRestRepository testPlanRestRepository

    @Autowired
    TestService testService
    @Autowired
    NetworkServiceService networkServiceService
    @Autowired
    CatalogueService catalogueService

    Set<TestPlan> createByService(NetworkServiceDescriptor nsd) {
        def testPlans = [] as HashSet
        nsd = networkServiceService.findByUuid(nsd.uuid)
        testService.findByService(nsd)?.each { td ->
                testPlans << new TestPlan(uuid: UUID.randomUUID().toString(), nsd:nsd, testd:td)
        }
        testPlans
    }

    Set<TestPlan> createByTest(TestDescriptor testd) {
        def testPlans = [] as HashSet
        testd = testService.findByUuid(testd.uuid)
        networkServiceService.findByTest(testd)?.each { nsd ->
            testPlans <<  new TestPlan(uuid: UUID.randomUUID().toString(), nsd:nsd, testd:testd)
        }
        testPlans
    }

    Set<TestPlan> createByServices(Set<NetworkService> nss) {
        def testPlans = [] as HashSet
        nss?.each { it -> testPlans.addAll(createByService(it.nsd)) }
        testPlans
    }

    Set<TestPlan> createByTests(Set<Test> ts) {
        def testPlans = [] as HashSet<TestPlan>
        ts?.each { it -> testPlans.addAll(createByTest(it.testd)) }
        testPlans
    }

    Set<TestPlan> createByServicesAndByTests(Set nss, Set ts) {
        def testPlans = [] as HashSet<TestPlan>
        testPlans.addAll(createByServices(nss))
        testPlans.addAll(createByTests(ts))
        testPlans
    }

    Set<TestPlan> createByPackage(Package pack){
        catalogueService.createByPackage(pack)
    }

    TestPlan getLast(){
        testPlanRepository.getOne(testPlanRepository.count())
    }

    TestPlan save(TestPlan testPlan){
        testPlan.uuid = testPlan.uuid?:UUID.randomUUID().toString()
        testPlanRepository.save(testPlan)
        testPlanRestRepository.create(testPlan)
    }

    TestPlan update(String uuid, String status) {
        TestPlan testPlan = testPlanRepository.find {it.uuid == uuid}
        testPlan.status = status
        testPlanRepository.save(testPlan)
        testPlanRestRepository.update(testPlan)
        testPlan
    }

    void delete(String uuid) {
        update(uuid, DELETED)
    }

    TestPlan getNextScheduled() {
//        testPlanRepository.find {it.status == SCHEDULED}
        //Todo-allemaos: extract the first correct item which status is SCHEDULED
        getLast()

    }
}


