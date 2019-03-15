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

import com.github.tng.vnv.planner.repository.TestPlanRepository
import com.github.tng.vnv.planner.model.NetworkServiceDescriptor
import com.github.tng.vnv.planner.model.TestDescriptor
import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.repository.NetworkServiceRepository
import com.github.tng.vnv.planner.repository.TestRepository
import groovy.util.logging.Log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Log
@Service
class TestPlanService {

    @Autowired
    TestRepository testRepository

    @Autowired
    TestPlanRepository testPlanRepository

    @Autowired
    NetworkServiceRepository networkServiceRepository

    def findByService(NetworkServiceDescriptor nsd) {
        List<TestPlan> tps = [] as ArrayList
        nsd.testingTags?.each { tt ->
            testRepository.findTssByTestTag(tt)?.each { td ->
                tps << new TestPlan(nsd:nsd, testd:td)
            }
        }
        tps
    }

    def findByTest(TestDescriptor td) {
        List<TestPlan> tps = [] as ArrayList
        td.testExecution?.each { tt ->
            networkServiceRepository.findNssByTestTag(tt)?.each { nsd ->
                tps <<  new TestPlan(nsd:nsd, testd:td)
            }
        }
        tps
    }

    def update(TestPlan tp) {
        return testPlanRepository.update(tp)
    }

    TestPlan createTestPlan(NetworkServiceDescriptor nsd, TestDescriptor td) {
        def testPlanUuid = UUID.randomUUID().toString()
        def testPlan = new TestPlan(
                uuid: testPlanUuid,
                packageId: testSuites.first().packageId,

                nsdUuid: nsd.uuid,
                tdUuid: td.uuid,
                index: 0,
                NetworkServiceDescriptor: nsd,
                TestDescriptor: td,
                status: 'CREATED',
        )
        TestPlan tpo = testPlanRepository.create(testPlan)
        tpo
    }

}
