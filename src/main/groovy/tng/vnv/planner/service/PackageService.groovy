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
import tng.vnv.planner.WorkflowManager
import tng.vnv.planner.client.Gatekeeper
import tng.vnv.planner.model.TestPlan
import tng.vnv.planner.model.TestSet
import tng.vnv.planner.utils.TestPlanStatus
import tng.vnv.planner.utils.TestSetType

@Service
class PackageService {

    @Autowired
    Gatekeeper gatekeeper
    @Autowired
    TestService testService
    @Autowired
    WorkflowManager workflowManager

    //Tango logger
    def tangoLogger = new TangoLogger()
    String tangoLoggerType = null;
    String tangoLoggerOperation = null;
    String tangoLoggerMessage = null;
    String tangoLoggerStatus = null;

    TestSet buildTestSetByPackage(packageId, confirmRequired, type = TestSetType.PACKAGE) throws RestClientException {
        if (packageId != null) {
            def matchedTests = [] as HashSet<Map>
            def matchedServices = [] as HashSet<Map>

            def pack = gatekeeper.getPackage(packageId)

            def testSet = new TestSet(uuid: UUID.randomUUID().toString(),
                    requestUuid: packageId,
                    requestType: type,
                    confirmRequired: confirmRequired,
                    status: confirmRequired ? TestPlanStatus.WAITING_FOR_CONFIRMATION : TestPlanStatus.SCHEDULED)

            def testingTags = pack.pd.package_content.collect { it.testing_tags }
            testingTags?.each { tags ->
                tags?.each { tag ->

                    List packageList = gatekeeper.getPackageByTag(tag)

                    tangoLoggerType = "I";
                    tangoLoggerOperation = "PackageService.buildTestSetByPackage";
                    tangoLoggerMessage = ("getting packages with tag: ${tag}. Obtained ${packageList.size()} packages");
                    tangoLoggerStatus = "200";
                    tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

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

            tangoLoggerType = "I";
            tangoLoggerOperation = "PackageService.buildTestSetByPackage";
            tangoLoggerMessage = ("matched pairs:");
            tangoLoggerStatus = "200";
            tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

            matchedServices.each { service ->
                matchedTests.each { test ->

                    tangoLoggerType = "I";
                    tangoLoggerOperation = "PackageService.buildTestSetByPackage";
                    tangoLoggerMessage = ("test: ${test.uuid} - service: ${service.uuid}");
                    tangoLoggerStatus = "200";
                    tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

                    testSet.testPlans << new TestPlan(uuid: UUID.randomUUID().toString(),
                            testSetUuid: testSet.uuid,
                            serviceUuid: service.uuid,
                            serviceName: service.id.name +"."+service.id.vendor+"."+service.id.version,
                            testUuid: test.uuid,
                            testName: test.id.name +"."+test.id.vendor+"."+test.id.version,
                            confirmRequired: confirmRequired,
                            testStatus: confirmRequired ? TestPlanStatus.WAITING_FOR_CONFIRMATION : TestPlanStatus.SCHEDULED)
                }
            }

            return testSet
        }

        throw new IllegalArgumentException("PackageUUID cannot be null")
    }

    TestSet buildTestSetByServicePackage(packageId, confirmRequired, executionHost, spName, policyId, type = TestSetType.SERVICE, serviceUuid) throws RestClientException {
        if (packageId != null) {
            def matchedTests = [] as HashSet<Map>

            def pack = gatekeeper.getPackage(packageId)
            def service = gatekeeper.getService(serviceUuid)

            def testSet = new TestSet(uuid: UUID.randomUUID().toString(),
                    requestUuid: packageId,
                    requestType: type,
                    confirmRequired: confirmRequired,
                    executionHost: executionHost,
                    spName: spName,
                    policyId: policyId,
                    status: confirmRequired ? TestPlanStatus.WAITING_FOR_CONFIRMATION : TestPlanStatus.SCHEDULED)

            def testingTags = pack.pd.package_content.collect { it.testing_tags }
            testingTags?.each { tags ->
                tags?.each { tag ->
                    List packageList = gatekeeper.getPackageByTag(tag)

                    tangoLoggerType = "I";
                    tangoLoggerOperation = "PackageService.buildTestSetByServicePackage";
                    tangoLoggerMessage = ("getting packages with tag: ${tag}. Obtained ${packageList.size()} packages");
                    tangoLoggerStatus = "200";
                    tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

                    packageList?.each {
                        it?.pd?.package_content?.each { resource ->
                            switch (resource.get('content-type')) {
                                case 'application/vnd.5gtango.tstd':
                                    matchedTests << (resource as Map)
                                    break

                                case 'application/vnd.etsi.osm.tstd':
                                    matchedTests << (resource as Map)
                                    break

                                default: break
                            }
                        }
                    }
                }
            }

            tangoLoggerType = "I";
            tangoLoggerOperation = "PackageService.buildTestSetByServicePackage";
            tangoLoggerMessage = ("matched pairs:");
            tangoLoggerStatus = "200";
            tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)


            matchedTests.each { test ->

                tangoLoggerType = "I";
                tangoLoggerOperation = "PackageService.buildTestSetByServicePackage";
                tangoLoggerMessage = ("test: ${test.uuid} - service: ${serviceUuid}");
                tangoLoggerStatus = "200";
                tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

                testSet.testPlans << new TestPlan(uuid: UUID.randomUUID().toString(),
                        testSetUuid: testSet.uuid,
                        serviceUuid: serviceUuid,
                        serviceName: service.nsd.name+"."+service.nsd.vendor+"."+service.nsd.version,
                        testUuid: test.uuid,
                        testName: test.id.name +"."+test.id.vendor+"."+test.id.version,
                        confirmRequired: confirmRequired,
                        executionHost: executionHost,
                        spName: spName,
                        policyId: policyId,
                        testStatus: confirmRequired ? TestPlanStatus.WAITING_FOR_CONFIRMATION : TestPlanStatus.SCHEDULED)
            }

            return testSet
        }

        throw new IllegalArgumentException("PackageUUID cannot be null")
    }

    TestSet buildTestSetByTestPackage(packageId, confirmRequired, executionHost, spName, policyId, type = TestSetType.SERVICE, testUuid) throws RestClientException {
        if (packageId != null) {
            def matchedServices = [] as HashSet<Map>

            def pack = gatekeeper.getPackage(packageId)
            def test = gatekeeper.getTest(testUuid)

            def testSet = new TestSet(uuid: UUID.randomUUID().toString(),
                    requestUuid: packageId,
                    requestType: type,
                    confirmRequired: confirmRequired,
                    executionHost: executionHost,
                    spName: spName,
                    policyId: policyId,
                    status: confirmRequired ? TestPlanStatus.WAITING_FOR_CONFIRMATION : TestPlanStatus.SCHEDULED)

            def testingTags = pack.pd.package_content.collect { it.testing_tags }
            testingTags?.each { tags ->
                tags?.each { tag ->
                    List packageList = gatekeeper.getPackageByTag(tag)

                    tangoLoggerType = "I";
                    tangoLoggerOperation = "PackageService.buildTestSetByTestPackage";
                    tangoLoggerMessage = ("getting packages with tag: ${tag}. Obtained ${packageList.size()} packages");
                    tangoLoggerStatus = "200";
                    tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

                    packageList?.each {
                        it?.pd?.package_content?.each { resource ->
                            switch (resource.get('content-type')) {
                                case 'application/vnd.5gtango.nsd':
                                    matchedServices << (resource as Map)
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

            tangoLoggerType = "I";
            tangoLoggerOperation = "PackageService.buildTestSetByTestPackage";
            tangoLoggerMessage = ("matched pairs:");
            tangoLoggerStatus = "200";
            tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

            matchedServices.each { service ->

                tangoLoggerType = "I";
                tangoLoggerOperation = "PackageService.buildTestSetByTestPackage";
                tangoLoggerMessage = ("test: ${testUuid} - service: ${service.uuid}");
                tangoLoggerStatus = "200";
                tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

                testSet.testPlans << new TestPlan(uuid: UUID.randomUUID().toString(),
                        testSetUuid: testSet.uuid,
                        serviceUuid: service.uuid,
                        serviceName: service.id.name +"."+service.id.vendor+"."+service.id.version,
                        testUuid: testUuid,
                        testName: test.testd.name+"."+test.testd.vendor+"."+test.testd.version,
                        confirmRequired: confirmRequired,
                        executionHost: executionHost,
                        spName: spName,
                        policyId: policyId,
                        testStatus: confirmRequired ? TestPlanStatus.WAITING_FOR_CONFIRMATION : TestPlanStatus.SCHEDULED)
            }

            return testSet
        }

        throw new IllegalArgumentException("PackageUUID cannot be null")
    }

    TestSet buildTestSetByTestingTag(tag, confirmRequired, executionHost, spName, policyId, type = TestSetType.TEST_AND_SERVICE) throws RestClientException {

        def matchedServices = [] as HashSet<Map>
        def matchedTests = [] as HashSet<Map>

        def testSet = new TestSet(uuid: UUID.randomUUID().toString(),
                requestUuid: UUID.randomUUID().toString(),
                requestType: type,
                confirmRequired: confirmRequired,
                executionHost: executionHost,
                spName: spName,
                policyId: policyId,
                status: confirmRequired ? TestPlanStatus.WAITING_FOR_CONFIRMATION : TestPlanStatus.SCHEDULED)

        List packageList = gatekeeper.getPackageByTag(tag)

        tangoLoggerType = "I";
        tangoLoggerOperation = "PackageService.buildTestSetByTestingTag";
        tangoLoggerMessage = ("getting packages with tag: ${tag}. Obtained ${packageList.size()} packages");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        packageList?.each {
            it?.pd?.package_content?.each { resource ->
                switch (resource.get('content-type')) {
                    case 'application/vnd.5gtango.nsd':
                        matchedServices << (resource as Map)
                        break
                    case 'application/vnd.etsi.osm.nsd':
                        matchedServices << (resource as Map)
                        break
                    case 'application/vnd.5gtango.tstd':
                        matchedTests << (resource as Map)
                        break
                    case 'application/vnd.etsi.osm.tstd':
                        matchedTests << (resource as Map)
                        break
                    default: break
                }
            }
        }

        tangoLoggerType = "I";
        tangoLoggerOperation = "PackageService.buildTestSetByTestingTag";
        tangoLoggerMessage = ("matched pairs:");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        matchedServices.each { service ->
            matchedTests.each { test ->

                tangoLoggerType = "I";
                tangoLoggerOperation = "PackageService.buildTestSetByTestingTag";
                tangoLoggerMessage = ("test: ${test.uuid} - service: ${service.uuid}");
                tangoLoggerStatus = "200";
                tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

                testSet.testPlans << new TestPlan(uuid: UUID.randomUUID().toString(),
                        testSetUuid: testSet.uuid,
                        serviceUuid: service.uuid,
                        serviceName: service.id.name +"."+service.id.vendor+"."+service.id.version,
                        testUuid: test.uuid,
                        testName: test.id.name +"."+test.id.vendor+"."+test.id.version,
                        confirmRequired: confirmRequired,
                        executionHost: executionHost,
                        spName: spName,
                        policyId: policyId,
                        testStatus: confirmRequired ? TestPlanStatus.WAITING_FOR_CONFIRMATION : TestPlanStatus.SCHEDULED)
            }
        }

        return testSet

    }

    TestSet buidTestSetByTestAndService(testUuid, serviceUuid, confirmRequired, executionHost, spName, policyId, type) {

        def testSet = new TestSet(uuid: UUID.randomUUID().toString(),
                requestType: type,
                confirmRequired: confirmRequired,
                executionHost: executionHost,
                spName: spName,
                policyId: policyId,
                status: confirmRequired ? TestPlanStatus.WAITING_FOR_CONFIRMATION : TestPlanStatus.SCHEDULED)

        def test = gatekeeper.getTest(testUuid)
        def service = gatekeeper.getService(serviceUuid)

        testSet.testPlans << new TestPlan(uuid: UUID.randomUUID().toString(),
                testSetUuid: testSet.uuid,
                serviceUuid: serviceUuid,
                serviceName: service.nsd.name+"."+service.nsd.vendor+"."+service.nsd.version,
                testUuid: testUuid,
                testName: test.testd.name+"."+test.testd.vendor+"."+test.testd.version,
                confirmRequired: confirmRequired,
                executionHost: executionHost,
                spName: spName,
                policyId: policyId,
                testStatus: confirmRequired ? TestPlanStatus.WAITING_FOR_CONFIRMATION : TestPlanStatus.SCHEDULED)

        return testSet
    }

    TestSet buildTestPlansByTestPackage(def uuid, def confirmRequired, def executionHost, def spName, def policyId) {
        def pack = gatekeeper.getPackageByUuid(uuid)
        def testSet = buildTestSetByTestPackage(pack[0].uuid, confirmRequired, executionHost, spName, policyId, TestSetType.TEST, uuid)

        testService.save(testSet)

        new Thread(new Runnable() {
            @Override
            void run() {
                workflowManager.searchForScheduledSet()
            }
        }).start()

        testSet
    }

    TestSet buildTestPlansByServicePackage(def uuid, def confirmRequired, def executionHost, def spName, def policyId) {
        def pack = gatekeeper.getPackageByUuid(uuid)
        def testSet = buildTestSetByServicePackage(pack[0].uuid, confirmRequired, executionHost, spName, policyId, TestSetType.SERVICE, uuid)

        testService.save(testSet)

        new Thread(new Runnable() {
            @Override
            void run() {
                workflowManager.searchForScheduledSet()
            }
        }).start()

        testSet

    }

    TestSet buildTestPlansByTestingTag(def tag, def confirmRequired, def executionHost, def spName, def policyId) {
        def testSet = buildTestSetByTestingTag(tag, confirmRequired, executionHost, spName, policyId, TestSetType.TEST_AND_SERVICE)

        testService.save(testSet)

        new Thread(new Runnable() {
            @Override
            void run() {
                workflowManager.searchForScheduledSet()
            }
        }).start()

        testSet

    }

    TestSet buildTestPlansByServiceAndTest(def testUuid, def serviceUuid, def confirmRequired, def executionHost, def spName, def policyId) {
        def testSet = buidTestSetByTestAndService(testUuid, serviceUuid, confirmRequired, executionHost, spName, policyId, TestSetType.TEST_AND_SERVICE)

        testService.save(testSet)

        new Thread(new Runnable() {
            @Override
            void run() {
                workflowManager.searchForScheduledSet()
            }
        }).start()

        testSet
    }
}
