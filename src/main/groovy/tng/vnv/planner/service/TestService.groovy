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

import org.springframework.web.client.RestClientException
import tng.vnv.planner.model.TestSet
import tng.vnv.planner.repository.TestPlanRepository
import tng.vnv.planner.model.TestPlan
import tng.vnv.planner.repository.TestSetRepository
import tng.vnv.planner.utils.TestPlanStatus
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Slf4j
@Service
class TestService {

    @Autowired
    TestPlanRepository testPlanRepository

    @Autowired
    TestSetRepository testSetRepository

    @Autowired
    PackageService packageService

    TestSet buildTestPlansByTest(def uuid) {
        packageService.buildTestPlansByTestPackage(uuid)
    }

    TestSet buildTestPlansByService(def uuid) {
        packageService.buildTestPlansByServicePackage(uuid)
    }

    TestSet buildTestPlansByPackage(def packageId, def confirmRequired) throws RestClientException{
        packageService.buildTestSetByPackage(packageId, confirmRequired)
    }

    TestSet save(TestSet testSet){
        testSetRepository.save(testSet)
    }

    TestPlan save(TestPlan testPlan){
        testPlanRepository.save(testPlan)
    }

    TestSet updateSet(UUID uuid, String status) {
        TestSet testSet = findSetByUuid(uuid)
        testSet.status = status
        testSetRepository.save(testSet)
    }

    TestPlan updatePlan(UUID uuid, String status) {
        TestPlan testPlan = findPlanByUuid(uuid)
        testPlan.testStatus = status
        testPlanRepository.save(testPlan)
    }

    void cancelTestSet(UUID uuid) {
        updateSet(uuid, TestPlanStatus.CANCELLED)
    }

    void deletePlan(UUID uuid) {
        updatePlan(uuid, TestPlanStatus.CANCELLING)
    }


    TestPlan findPlanByUuid(UUID uuid){
        testPlanRepository.findByUuid(uuid)
    }

    TestSet findNextScheduledTestSet() {
        testSetRepository.findFirstByStatus(TestPlanStatus.SCHEDULED)
    }

    List<TestSet> findExecutingTestSet() {
        testSetRepository.findByStatus(TestPlanStatus.STARTING)
    }

    boolean existsByStartingStatus() {
        (testPlanRepository.findFirstByStatus(TestPlanStatus.STARTING) != null)
    }

    List<TestPlan> findAll(){
        testPlanRepository.findAll()
    }

    TestSet findSetByUuid(UUID uuid){
        testSetRepository.findByUuid(uuid)
    }

    List<TestSet> findAllTestSets(){
        testSetRepository.findAll()
    }

    TestPlan findByTestPlanStatus(TestPlanStatus status){
        testPlanRepository.findByTestStatus(status)
    }
}

