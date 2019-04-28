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

package com.github.tng.vnv.planner

import com.github.tng.vnv.planner.config.TestRestSpec
import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.restmock.CuratorMock
import com.github.tng.vnv.planner.service.TestPlanService
import com.github.tng.vnv.planner.utils.TEST_PLAN_STATUS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

class WorkFlowManagerTest extends TestRestSpec {

    @Autowired
    TestPlanService testPlanService
    @Autowired
    CuratorMock curatorMock

    public static final String TEST_RESULT_UUID = UUID.randomUUID().toString()
    public static final String TEST_PLAN_WFM_UUID_1 = '109873681'
    public static final String TEST_PLAN_WFM_UUID_2 = '109873682'
    public static final String TEST_PLAN_WFM_UUID_3 = '109873683'
    public static final String TEST_PLAN_WFM_UUID_4 = '109873684'
    public static final String TEST_PLAN_WFM_UUID_5 = '109873685'
    public static final String TEST_PLAN_WFM_UUID_6 = '109873686'
    public static final String TEST_PLAN_WFM_UUID_7 = '109873687'
    public static final String IMEDIA_TEST_PLAN_SERVICE_UUID = 'immedia0-9429-4a07-b7af-dd429d6d04o3'
    public static final String IMEDIA_TEST_PLAN_TEST_UUID = 'immedia0-8cc7-47a9-9112-6wff9e88wu2k'


    void 'When curator sends a COMPLETED testPlan should Planner complete the testPlan lifycycle'() {
        setup:
        curatorMock.isBusy(false)
        when:
        scheduleTestPlan( TEST_PLAN_WFM_UUID_1, TEST_PLAN_STATUS.SCHEDULED, '1st scheduled testPlan')
        and:
        def entity = postForEntity('/api/v1/test-plans/on-change/completed',
                [
                        event_actor: 'tng-vnv-curator',
                        status: 'COMPLETED',
                        test_plan_uuid: TEST_PLAN_WFM_UUID_1,
                        test_results: [
                                [
                                        test_uuid: TEST_RESULT_UUID,
                                        test_result_uuid: '45678',
                                        status: TEST_PLAN_STATUS.COMPLETED,
                                ],
                        ],
                        test_plan_repository: 'tng-rep',
                        test_results_repository: 'tng-res',
                ]
                , Void.class)
        then:
        entity.statusCode == HttpStatus.OK
        testPlanService.findByUuid(TEST_PLAN_WFM_UUID_1).status == TEST_PLAN_STATUS.COMPLETED
        cleanup:
        cleanTestPlanDB()
    }

    void 'When curator sends a COMPLETED testPlan should Planner should send the next testPlan'() {
        setup:
        curatorMock.isBusy(false)
        when:
        scheduleTestPlan( TEST_PLAN_WFM_UUID_2, TEST_PLAN_STATUS.SCHEDULED, '1st scheduled testPlan')
        scheduleTestPlan( TEST_PLAN_WFM_UUID_3, TEST_PLAN_STATUS.SCHEDULED, '2nd scheduled testPlan')
        and:
        def entity = postForEntity('/api/v1/test-plans/on-change/completed',
                [
                        event_actor: 'tng-vnv-curator',
                        status: 'COMPLETED',
                        test_plan_uuid: TEST_PLAN_WFM_UUID_2,
                ]
                , Void.class)
        then:
        entity.statusCode == HttpStatus.OK
        testPlanService.findByUuid(TEST_PLAN_WFM_UUID_2).status == TEST_PLAN_STATUS.COMPLETED
        def nextTestPlan = testPlanService.findByUuid(TEST_PLAN_WFM_UUID_3)
        nextTestPlan.status == TEST_PLAN_STATUS.STARTING
        curatorMock.currentTestPlan.nsd.name == "mediapilot-service"

        cleanup:
        cleanTestPlanDB()
    }

    void 'When curator sends COMPLETED testPlans sequentially Planner Planner should send back each next scheduled testPlan'() {
        setup:
        curatorMock.isBusy(false)
        when:
        scheduleTestPlan( TEST_PLAN_WFM_UUID_4, TEST_PLAN_STATUS.SCHEDULED, '1st scheduled testPlan')
        scheduleTestPlan( TEST_PLAN_WFM_UUID_5, TEST_PLAN_STATUS.SCHEDULED, '2nd scheduled testPlan')
        scheduleTestPlan( TEST_PLAN_WFM_UUID_6, TEST_PLAN_STATUS.SCHEDULED, '3rd scheduled testPlan')
        scheduleTestPlan( TEST_PLAN_WFM_UUID_7, TEST_PLAN_STATUS.SCHEDULED, '4th scheduled testPlan')
        and:
        postForEntity('/api/v1/test-plans/on-change/completed',
                [
                        event_actor: 'tng-vnv-curator',
                        status: 'COMPLETED',
                        test_plan_uuid: TEST_PLAN_WFM_UUID_4,
                ]
                , Void.class)
        and:
        postForEntity('/api/v1/test-plans/on-change/completed',
                [
                        event_actor: 'tng-vnv-curator',
                        status: 'COMPLETED',
                        test_plan_uuid: TEST_PLAN_WFM_UUID_5,
                ]
                , Void.class)
        and:
        postForEntity('/api/v1/test-plans/on-change/completed',
                [
                        event_actor: 'tng-vnv-curator',
                        status: 'COMPLETED',
                        test_plan_uuid: TEST_PLAN_WFM_UUID_6,
                ]
                , Void.class)
        and:
        def entity = postForEntity('/api/v1/test-plans/on-change/completed',
                [
                        event_actor: 'tng-vnv-curator',
                        status: 'COMPLETED',
                        test_plan_uuid: TEST_PLAN_WFM_UUID_7,
                ]
                , Void.class)
        then:
        entity.statusCode == HttpStatus.OK
        def testPlans = testPlanService.testPlanRepository.findAll().findAll{it.status == TEST_PLAN_STATUS.COMPLETED}
        testPlans.size() == 4
        cleanup:
        cleanTestPlanDB()
    }

    TestPlan scheduleTestPlan(String uuid, String status, String description){
        testPlanService.save(new TestPlan(uuid: uuid, status: status, description: description, serviceUuid: IMEDIA_TEST_PLAN_SERVICE_UUID, testUuid: IMEDIA_TEST_PLAN_TEST_UUID))
    }
}
