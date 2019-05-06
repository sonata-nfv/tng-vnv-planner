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

package com.github.tng.vnv.planner.controller

import com.github.tng.vnv.planner.config.TestRestSpec
import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.restmock.CuratorMock
import com.github.tng.vnv.planner.restmock.DataMock
import com.github.tng.vnv.planner.service.TestPlanService
import com.github.tng.vnv.planner.utils.TEST_PLAN_STATUS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

class TestPlanControllerTest extends TestRestSpec {

    @Autowired
    TestPlanService testPlanService

    @Autowired
    CuratorMock curatorMock

    static final String IMEDIA_TEST_PLAN_NSD_UUID_MEDIAPILOT_SERVICE = 'immedia0-9429-4a07-b7af-dd429d6d04o3'
    static final String IMEDIA_TEST_PLAN_TESTD_UUID_TEST_IMMERSIVE_MEDIA = 'immedia0-8cc7-47a9-9112-6wff9e88wu2k'
    static final String LATENCY_TEST_PLAN_NSD_UUID_HAPROXY_1 = 'input0ns-f213-4fae-8d3f-04358e1e1451'
    static final String LATENCY_TEST_PLAN_TESTD_UUID_HTTP_BENCHMARK_TEST_1 = 'input0ts-75f5-4ca1-90c8-12ec80a79836'
    static final String TAG_UNRELATED_TEST_PLAN_NSD_UUID_HAPROXY_1 = 'input0ns-f213-4fae-8d3f-04358e1e1451'
    static final String TAG_UNRELATED_TEST_PLAN_TESTD_UUID_HTTP_BENCHMARK_TEST_1 = 'input0ts-75f5-4ca1-90c8-12ec80a79836'
    static final String DIY_DESCRIPTOR_TEST_PLAN_NSD_UUID_NS_SQUID = '4dd4cb15-76b8-46fd-b3c0-1b165cc332f9'
    static final String DIY_DESCRIPTOR_TEST_PLAN_TESTD_UUID_TEST_HTTP_BENCHMARK_ADVANCED_PROXY_1 = 'b68dbe19-5c02-4865-8c4b-5e43ada1b67d'
    static final String DIY_DESCRIPTOR_TEST_PLAN_VALIDATION_REQUIRED_TESTD_UUID_TEST_HTTP_BENCHMARK_ADVANCED_PROXY_2 = 'b68dbe19-5c02-4865-8c4b-5e43ada1b67c'
    static final String DIY_DESCRIPTOR_TEST_PLAN_CONFIRMED_TESTD_UUID_TEST_HTTP_BENCHMARK_ADVANCED_PROXY_3 = 'b68dbe19-5c02-4865-8c4b-5e43ada1b67b'
    static final String UNKNOWN_UUID = '00000000-5c02-4865-8c4b-5e43ada1b67b'
     static final String TEST_DESCRIPTOR_UUID_HTTP_BENCHMARK_TEST_1 = 'input0ts-75f5-4ca1-90c8-12ec80a79836'


    static final String TEST_PLAN_TPC_UUID0 = '109873670'
    static final String TEST_PLAN_TPC_UUID1 = '109873671'
    static final String TEST_PLAN_TPC_UUID2 = '109873672'

    void "when curator is busy, schedule request of a test plan list should successfully save all test plans unsorted"() {
        setup:
        curatorMock.isBusy(true)
        scheduleTestPlan(TEST_PLAN_TPC_UUID0, TEST_PLAN_STATUS.STARTING, 'starting in mock curator')
        when:
        def entity = postForEntity('/api/v1/test-plans',
                [
                        'nsd_uuid': IMEDIA_TEST_PLAN_NSD_UUID_MEDIAPILOT_SERVICE,
                        'testd_uuid'   : IMEDIA_TEST_PLAN_TESTD_UUID_TEST_IMMERSIVE_MEDIA,
                        'description' : 'dummyTestPlan1-index1',
                        'index'       : '1',
                ]
                , Void.class)
        entity = postForEntity('/api/v1/test-plans',
                [
                        'nsd_uuid': LATENCY_TEST_PLAN_NSD_UUID_HAPROXY_1,
                        'testd_uuid'   : LATENCY_TEST_PLAN_TESTD_UUID_HTTP_BENCHMARK_TEST_1,
                        'description' : 'dummyTestPlan2-index3',
                        'index'       : '3',
                ]
                , Void.class)
        entity = postForEntity('/api/v1/test-plans',
                [
                        'nsd_uuid': TAG_UNRELATED_TEST_PLAN_NSD_UUID_HAPROXY_1,
                        'testd_uuid'   : TAG_UNRELATED_TEST_PLAN_TESTD_UUID_HTTP_BENCHMARK_TEST_1,
                        'description' : 'dummyTestPlan3-index2',
                        'index'       : '2',
                ]
                , Void.class)
        entity = postForEntity('/api/v1/test-plans',
                [
                    'nsd_uuid': DIY_DESCRIPTOR_TEST_PLAN_NSD_UUID_NS_SQUID,
                    'testd_uuid'   : DIY_DESCRIPTOR_TEST_PLAN_TESTD_UUID_TEST_HTTP_BENCHMARK_ADVANCED_PROXY_1,
                    'description': 'dummyTestPlan4-index4',
                    'index'      : '4',
                ]
                , Void.class)
        then:
        entity.statusCode == HttpStatus.OK
        def testPlans = testPlanService.testPlanRepository.findAll().findAll { it.status == "SCHEDULED" }
        testPlans[1].description == 'dummyTestPlan2-index3'
        testPlans[2].description == 'dummyTestPlan3-index2'
        cleanup:
        cleanTestPlanDB()
    }

    void "schedule request with validation required for one test plan should successfully schedule only the not validation required test plans"() {
        setup:
        curatorMock.isBusy(true)
        scheduleTestPlan(TEST_PLAN_TPC_UUID0, TEST_PLAN_STATUS.STARTING, 'starting in mock curator')
        when:
        def entity = postForEntity('/api/v1/test-plans',
                [
                        'nsd_uuid': LATENCY_TEST_PLAN_NSD_UUID_HAPROXY_1,
                        'testd_uuid'   : LATENCY_TEST_PLAN_TESTD_UUID_HTTP_BENCHMARK_TEST_1,
                        'description' : 'dummyTestPlan1-non-validation_required',
                        'index'       : '1',
                ], Void.class)
        entity = postForEntity('/api/v1/test-plans',
                [
                        'nsd_uuid': DIY_DESCRIPTOR_TEST_PLAN_NSD_UUID_NS_SQUID,
                        'testd_uuid'   : DIY_DESCRIPTOR_TEST_PLAN_VALIDATION_REQUIRED_TESTD_UUID_TEST_HTTP_BENCHMARK_ADVANCED_PROXY_2,
                        'description': 'dummyTestPlan-validation_required',
                        'index'      : '2',
                ], Void.class)
        entity = postForEntity('/api/v1/test-plans',
                [
                        'nsd_uuid': DIY_DESCRIPTOR_TEST_PLAN_NSD_UUID_NS_SQUID,
                        'testd_uuid'   : DIY_DESCRIPTOR_TEST_PLAN_CONFIRMED_TESTD_UUID_TEST_HTTP_BENCHMARK_ADVANCED_PROXY_3,
                        'description': 'dummyTestPlan-validation_confirmed',
                        'index'      : '3',
                ], Void.class)
        then:
        entity.statusCode == HttpStatus.OK
        def testPlans = testPlanService.testPlanRepository.findAll().findAll { it.status != TEST_PLAN_STATUS.REJECTED }
        testPlans.get(0).status == TEST_PLAN_STATUS.STARTING
        testPlans.get(0).description == 'starting in mock curator'
        testPlans.get(1).status == TEST_PLAN_STATUS.SCHEDULED
        testPlans.get(1).description == 'dummyTestPlan1-non-validation_required'
        testPlans.get(2).status == TEST_PLAN_STATUS.NOT_CONFIRMED
        testPlans.get(2).description == 'dummyTestPlan-validation_required'
        testPlans.get(3).status == TEST_PLAN_STATUS.SCHEDULED
        testPlans.get(3).description == 'dummyTestPlan-validation_confirmed'
        cleanup:
        cleanTestPlanDB()
    }

    void "schedule request with validation required for one test plan should successfully schedule no test plans"() {
        setup:
        curatorMock.isBusy(false)
        when:
        def entity = postForEntity('/api/v1/test-plans',
            [
                    "nsd_uuid": DIY_DESCRIPTOR_TEST_PLAN_NSD_UUID_NS_SQUID,
                    "testd_uuid": DIY_DESCRIPTOR_TEST_PLAN_VALIDATION_REQUIRED_TESTD_UUID_TEST_HTTP_BENCHMARK_ADVANCED_PROXY_2,
                    'description': 'dummyTestPlan1-validation_required',
                    'index': '1',
            ], Void.class)
        then:
        entity.statusCode == HttpStatus.OK
        testPlanService.testPlanRepository.findAll()
                .findAll{it.status == "SCHEDULED"}.size() == 0
        cleanup:
        cleanTestPlanDB()
    }

    void "schedule a non valid test.uuid OR non valid service.uuid should store no SCHEDULED testPlans"() {
        setup:
        curatorMock.isBusy(true)
        scheduleTestPlan(TEST_PLAN_TPC_UUID0, TEST_PLAN_STATUS.STARTING, 'starting in mock curator')
        when:
        def entity = postForEntity('/api/v1/test-plans',
                [
                        "nsd_uuid": "",
                ],  Void.class)
        entity = postForEntity('/api/v1/test-plans',
                [
                        "nsd_uuid": LATENCY_TEST_PLAN_NSD_UUID_HAPROXY_1,
                        "testd_uuid": "",
                ],  Void.class)
        entity = postForEntity('/api/v1/test-plans',
                [
                        "nsd_uuid": "",
                        "testd_uuid": LATENCY_TEST_PLAN_TESTD_UUID_HTTP_BENCHMARK_TEST_1,
                ],  Void.class)
        entity = postForEntity('/api/v1/test-plans',
                [
                        "nsd_uuid": "",
                        "testd_uuid": "",
                ],  Void.class)
        then:
        entity.statusCode == HttpStatus.OK
        testPlanService.testPlanRepository.findAll()
                .findAll{it.status == "SCHEDULED"}.size() == 0
        cleanup:
        cleanTestPlanDB()
    }

    void "delete request for one test plan should successfully change the status of the test plan to CANCELING scheduled test plan"() {
        when:
        scheduleTestPlan(TEST_PLAN_TPC_UUID1, TEST_PLAN_STATUS.CREATED, 'scheduled testPlan\'s status which will turn into canceling')
        and:
        delete('/api/v1/test-plans/{uuid}',TEST_PLAN_TPC_UUID1)
        then:
        testPlanService.testPlanRepository.findByUuid(TEST_PLAN_TPC_UUID1).status == TEST_PLAN_STATUS.CANCELLING
        cleanup:
        cleanTestPlanDB()
    }

    void "test plan request for one testPlan should successfully return the corresponding test plan"() {
        setup:
        curatorMock.isBusy(true)
        scheduleTestPlan(TEST_PLAN_TPC_UUID0, TEST_PLAN_STATUS.STARTING, 'starting in mock curator')
        when:
        def testPlan = scheduleTestPlan(TEST_PLAN_TPC_UUID2, TEST_PLAN_STATUS.CREATED, 'retrieve a testPlan through its uuid')
        and:
        def entity = getForEntity('/api/v1/test-plans/{uuid}', TestPlan, testPlan.uuid)
        then:
        entity.statusCode == HttpStatus.OK
        entity.body.description == 'retrieve a testPlan through its uuid'
        entity.body.status == TEST_PLAN_STATUS.CREATED
        cleanup:
        cleanTestPlanDB()
    }

    void "request of all test plans should successfully return the list of all test plans"() {
        when:
        scheduleTestPlan(TEST_PLAN_TPC_UUID1, "TEST_LIST_ALL_STATUS", '')
        and:
        def testPlans = getForEntity('/api/v1/test-plans/',TestPlan[]).body
        then:
        testPlans.size()>=1
        cleanup:
        cleanTestPlanDB()
    }

    void "create test plan from NetworkService"() {
        when:
        def testPlans = postForEntity('/api/v1/test-plans/services', ['uuid' : LATENCY_TEST_PLAN_NSD_UUID_HAPROXY_1], List ).body
        then:
        testPlans.size()>=1
        cleanup:
        cleanTestPlanDB()
    }

    void "create test plan from Test"() {
        when:
        def testPlans = postForEntity('/api/v1/test-plans/tests', ['uuid' : TEST_DESCRIPTOR_UUID_HTTP_BENCHMARK_TEST_1], List ).body
        then:
        testPlans.size()>=1
        cleanup:
        cleanTestPlanDB()
    }

    TestPlan scheduleTestPlan(String uuid, String status, String description){
        testPlanService.save(new TestPlan(uuid: uuid, status: status, description: description))
    }
}
