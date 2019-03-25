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


import com.github.mrduguo.spring.test.AbstractSpec
import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.restmock.CuratorMock
import com.github.tng.vnv.planner.restmock.TestPlanRepositoryMock
import com.github.tng.vnv.planner.service.TestPlanService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import spock.lang.Ignore

class CuratorCallbackControllerTest extends AbstractSpec {

    public static final String MULTIPLE_TEST_PLANS_PACKAGE_ID ='multiple_scheduler:test:0.0.1'
    public static final String TEST_RESULT_UUID = UUID.randomUUID().toString()
    public static final String TEST_PLAN_UUID = '109873678'

    @Autowired
    TestPlanService testPlanService

    @Autowired
    CuratorMock curatorMock

    @Autowired
    TestPlanRepositoryMock testPlanRepositoryMock

    @Ignore
    void 'curator returns back call as COMPLETED should get a new testPlan with status STARTING to curate'() {

        setup:
        testPlanService.send(
                new TestPlan(uuid: UUID.randomUUID().toString(), status: 'dummyTestPlan') )
        when:
        def entity = postForEntity('/tng-vnv-planner/api/v1/test-plans/on-change/completed',
                [
                        event_actor: 'tng-vnv-curator',
                        status: 'COMPLETED',
                        test_plan_uuid: TEST_PLAN_UUID,
                        test_results_uuid: TEST_RESULT_UUID,
                        test_plan_repository: 'tng-rep',
                        test_results_repository: 'tng-res',
                ]
                , Void.class)

        then:
        entity.statusCode == HttpStatus.OK

        testPlanRepositoryMock.testPlans.values()[0].status=='COMPLETED'
        testPlanRepositoryMock.testPlans.values().last().status=='STARTING'

        cleanup:
        testPlanRepositoryMock.reset()
    }

    @Ignore
    void 'curator returns back call as not completed should get a new testPlan with status STARTING to curate'() {

        setup:
        testPlanService.send(
                new TestPlan(uuid: UUID.randomUUID().toString(), status: 'dummyTestPlan') )
        when:
        def entity = postForEntity('/tng-vnv-planner/api/v1/test-plans/on-change/',
                [
                        event_actor: 'tng-vnv-curator',
                        status:'CANCELLING',
                        test_plan_uuid:TEST_PLAN_UUID,
                        test_results_uuid:TEST_RESULT_UUID,
                        test_plan_repository:'tng-rep',
                        test_results_repository:'tng-res',
                ]
                , Void.class)

        then:
        entity.statusCode == HttpStatus.OK

        testPlanRepositoryMock.testPlans.values()[0].status=='CANCELLING'
        testPlanRepositoryMock.testPlans.values().last().status=='STARTING'

        cleanup:
        testPlanRepositoryMock.reset()
    }

}
