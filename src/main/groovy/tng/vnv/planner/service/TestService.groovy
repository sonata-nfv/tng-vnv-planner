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

import tng.vnv.planner.utils.TangoLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import tng.vnv.planner.client.Curator
import tng.vnv.planner.client.Gatekeeper
import tng.vnv.planner.model.TestPlan
import tng.vnv.planner.model.TestSet
import tng.vnv.planner.repository.TestPlanRepository
import tng.vnv.planner.repository.TestSetRepository
import tng.vnv.planner.utils.TestPlanStatus

@Service
class TestService {

    @Autowired
    TestPlanRepository testPlanRepository

    @Autowired
    TestSetRepository testSetRepository

    @Autowired
    PackageService packageService

    @Autowired
    Gatekeeper gatekeeper

    @Autowired
    Curator curator

    //Tango logger
    def tangoLogger = new TangoLogger()
    String tangoLoggerType = null;
    String tangoLoggerOperation = null;
    String tangoLoggerMessage = null;
    String tangoLoggerStatus = null;

    TestSet buildTestPlansByTest(def uuid, def confirmRequired) {
        packageService.buildTestPlansByTestPackage(uuid, confirmRequired)
    }

    TestSet buildTestPlansByService(def uuid, def confirmRequired) {
        packageService.buildTestPlansByServicePackage(uuid, confirmRequired)
    }

    TestSet buildTestPlansByTestingTag(def tag, def confirmRequired){
        packageService.buildTestPlansByTestingTag(tag,confirmRequired)
    }

    TestSet buildTestPlansByServiceAndTest(def testUuid, def serviceUuid, def confirmRequired){
        packageService.buildTestPlansByServiceAndTest(testUuid, serviceUuid, confirmRequired)
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

    TestSet updateSet(String uuid, String status) {
        def testSet = findSetByUuid(uuid)
        testSet.status = status
        testSetRepository.save(testSet)
    }

    TestPlan updatePlanStatus(String uuid, String status) {
        def testPlan = findPlanByUuid(uuid)
        testPlan.testStatus = status
        testPlanRepository.save(testPlan)
    }

    TestPlan updatePlanResultId(String uuid, String resultUuid) {
        def testPlan = findPlanByUuid(uuid)
        testPlan.testResultUuid = resultUuid
        testPlanRepository.save(testPlan)
    }

    void cancelTestSet(String uuid) {
        updateSet(uuid, TestPlanStatus.CANCELLING)
    }

    void deletePlan(String uuid) {
        updatePlanStatus(uuid, TestPlanStatus.CANCELLING)
    }


    TestPlan findPlanByUuid(String uuid){
        testPlanRepository.findByUuid(uuid)
    }

    TestSet findNextScheduledTestSet() {
        testSetRepository.findFirstByStatus(TestPlanStatus.SCHEDULED)
    }

    TestSet findLastScheduledTestSet() {
        testSetRepository.findTopByStatusOrderByCreatedAtDesc(TestPlanStatus.SCHEDULED)
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

    TestSet findSetByUuid(String uuid){
        testSetRepository.findByUuid(uuid)
    }

    List<TestSet> findAllTestSets(){
        testSetRepository.findAll()
    }

    TestPlan findByTestPlanStatus(TestPlanStatus status){
        testPlanRepository.findByTestStatus(status)
    }

    void cancelAllTestPlansByTestSetUuid(String testSetUuid){
        def testPlans = testPlanRepository.findByTestSetUuid(testSetUuid)

        testPlans.each { testPlan ->
            if (testPlan.testStatus == TestPlanStatus.SCHEDULED){
                updatePlanStatus(testPlan.uuid, TestPlanStatus.CANCELLED)
            } else {
                curator.delete(testPlan.uuid)
                updatePlanStatus(testPlan.uuid, TestPlanStatus.CANCELLING)
            }


        }
    }

    TestSet findByUuid(String uuid) {
        gatekeeper.getTest(uuid).body
    }


    List findServicesByTest(def uuid){
        tangoLoggerType = "I";
        tangoLoggerOperation = "TestService.findServicesByTest";
        tangoLoggerMessage = ("Looking for Services related with test_uuid: ${uuid}");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        def matchedServices = [] as HashSet<Object>
        def packs = gatekeeper.getPackageByUuid(uuid)
        if(packs != null){
            packs.each { pack->
                pack.pd.package_content.each { resource ->
                    if (resource.get('content-type') == 'application/vnd.5gtango.tstd'
                            || resource.get('content-type') == 'application/vnd.etsi.osm.tstd') {
                        def testing_tag = resource.get('testing_tags')
                        testing_tag.each { tt ->

                            tangoLoggerType = "I";
                            tangoLoggerOperation = "TestService.findServicesByTest";
                            tangoLoggerMessage = ("including services with tag: ${tt}");
                            tangoLoggerStatus = "200";
                            tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

                            matchedServices << findServicesByTag(tt)
                        }
                    }
                }
            }
        }
        new ArrayList(matchedServices)
    }

    List findServicesByTag(def tag){
        tangoLoggerType = "I";
        tangoLoggerOperation = "TestService.findServicesByTag";
        tangoLoggerMessage = ("Looking for Services with testing_tag: ${tag}");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        def matchedServices = [] as HashSet<Object>
        def packs = gatekeeper.getPackageByTag(tag)
        if(packs != null){
            packs.each { pack ->
                pack.pd.package_content.each { resource ->
                    if (resource.get('content-type') == 'application/vnd.5gtango.nsd'
                            || resource.get('content-type') == 'application/vnd.etsi.osm.nsd') {
                        tangoLoggerType = "I";
                        tangoLoggerOperation = "TestService.findServicesByTag";
                        tangoLoggerMessage = ("including services with uuid: ${resource.uuid}");
                        tangoLoggerStatus = "200";
                        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

                        matchedServices << gatekeeper.getService(resource.uuid)
                    }
                }
            }
        }
        new ArrayList(matchedServices)
    }
}
