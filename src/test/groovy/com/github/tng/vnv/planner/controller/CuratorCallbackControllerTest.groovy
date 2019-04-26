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

package com.github.tng.vnv.planner.controller


import com.github.tng.vnv.planner.config.TestRestSpec
import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.service.TestPlanService
import com.github.tng.vnv.planner.utils.TEST_PLAN_STATUS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

class CuratorCallbackControllerTest extends TestRestSpec {

    public static final String TEST_RESULT_UUID = UUID.randomUUID().toString()
    public static final String TEST_RESULT2_UUID = UUID.randomUUID().toString()
    public static final String TEST_PLAN_UUID = '109873678'
    public static final String TEST_PLAN2_UUID = '561ba353-234c-44ba-9f17-f3e48caca4a5'

    @Autowired
    TestPlanService testPlanService

    void 'curator returns back call as COMPLETED should store the testPlan with status respectively'() {
        when:
        createDummyTestPlan(TEST_PLAN_UUID)
        def status = TEST_PLAN_STATUS.COMPLETED
        def entity = postForEntity('/api/v1/test-plans/on-change/completed',
                [
                        event_actor: 'tng-vnv-curator',
                        status: status,
                        test_plan_uuid: TEST_PLAN_UUID,
                        test_results: [
                                    [
                                        test_uuid: '45678',
                                        test_result_uuid: TEST_RESULT_UUID,
                                        status: 'COMPLETED'
                                    ],
                                    [
                                            test_uuid: '45678',
                                            test_result_uuid: TEST_RESULT2_UUID,
                                            status: 'COMPLETED'
                                    ],
                                ],
                        test_plan_repository: 'tng-rep',
                        test_results_repository: 'tng-res',
                ]
                , Void.class)
        then:
        entity.statusCode == HttpStatus.OK
        testPlanService.findByUuid(TEST_PLAN_UUID).status==status
        cleanup:
        cleanTestPlanDB()
    }

    void 'curator returns back call as NOT COMPLETED should store the testPlan with status respectively'() {
        when:
        createDummyTestPlan(TEST_PLAN_UUID)
        def status = TEST_PLAN_STATUS.CANCELLING
        def entity = postForEntity('/api/v1/test-plans/on-change/',
                [
                        event_actor: 'tng-vnv-curator',
                        status:status,
                        test_plan_uuid:TEST_PLAN_UUID,
                        test_results: [
                                [
                                        test_uuid: '45678',
                                        test_result_uuid: TEST_RESULT_UUID,
                                        status: 'ERROR'
                                ],
                                [
                                        test_uuid: '45678',
                                        test_result_uuid: TEST_RESULT2_UUID,
                                        status: 'COMPLETED'
                                ],
                        ],
                        test_plan_repository:'tng-rep',
                        test_results_repository:'tng-res',
                ]
                , Void.class)
        then:
        entity.statusCode == HttpStatus.OK
        testPlanService.findByUuid(TEST_PLAN_UUID).status==status
        cleanup:
        cleanTestPlanDB()
    }

    void 'curator returns back call as ERROR should store the testPlan with status respectively'() {
        when:
        createDummyTestPlan(TEST_PLAN_UUID)
        def status = TEST_PLAN_STATUS.ERROR
        def entity = postForEntity('/api/v1/test-plans/on-change/',
                [
                        event_actor: 'tng-vnv-curator',
                        status:status,
                        test_plan_uuid:TEST_PLAN_UUID,
                        test_results: [],
                        test_plan_repository:'tng-rep',
                        test_results_repository:'tng-res',
                ]
                , Void.class)
        then:
        entity.statusCode == HttpStatus.OK
        testPlanService.findByUuid(TEST_PLAN_UUID).status==status
        cleanup:
        cleanTestPlanDB()
    }

    void 'following an completed testPlan in Paris should store the testPlan as completed'() {
        when:
        createDummyTestPlan(TEST_PLAN2_UUID)

        def entity = postForEntity('/api/v1/test-plans/on-change/completed',
                [
                        event_actor: 'Curator',
                        status: 'COMPLETED',
                        test_plan_uuid: TEST_PLAN2_UUID,
                        test_results: [
                                [
                                    test_uuid: '639ce960-5a76-4722-9d5c-ee7c476ece10',
                                    test_results_uuid: '1af9de2d-15c0-4dee-a8b1-6801e4e38bec',
                                    test_status: 'COMPLETED'
                                ]

                        ],
                ]
                , Void.class)
        then:
        entity.statusCode == HttpStatus.OK
        testPlanService.findByUuid(TEST_PLAN2_UUID).status=='COMPLETED'
        cleanup:
        cleanTestPlanDB()
    }

    void createDummyTestPlan(String test_plan_uuid){
        def testPlan = new TestPlan(uuid: test_plan_uuid, status: 'dummyTestPlan')
        testPlanService.save(testPlan)
        testPlanService.update(testPlan.uuid, TEST_PLAN_STATUS.STARTING)
    }
}
