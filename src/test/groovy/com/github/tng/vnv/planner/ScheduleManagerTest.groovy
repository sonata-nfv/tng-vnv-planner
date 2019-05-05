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

package com.github.tng.vnv.planner


import com.github.tng.vnv.planner.model.Package
import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.restmock.CatalogueMock
import com.github.tng.vnv.planner.config.TestRestSpec
import com.github.tng.vnv.planner.service.TestPlanService
import org.springframework.beans.factory.annotation.Autowired

class ScheduleManagerTest extends TestRestSpec {

    static final String MULTIPLE_TEST_PLANS_MOCKED_PACKAGE_ID_FOR_HTTP_BENCHMARK_TEST_1_AND_HAPROXY_1 ='multi_d07742ed-9429-4a07-b7af-d0b24a6d5c4c_input0ts-75f5-4ca1-90c8-12ec80a79836_a77f66d5-b1dc-4f19-9dc5-32d7d79cc897'

    @Autowired
    ScheduleManager scheduler

    @Autowired
    CatalogueMock testCatalogueMock

    @Autowired
    TestPlanService testPlanService

    void 'schedule multiple test plans should produce success result'() {
        when:
        List<TestPlan> testPlans = scheduler.create(MULTIPLE_TEST_PLANS_MOCKED_PACKAGE_ID_FOR_HTTP_BENCHMARK_TEST_1_AND_HAPROXY_1)
        then:
        testPlans.size() == 10
        cleanup:
        cleanTestPlanDB()
    }
}