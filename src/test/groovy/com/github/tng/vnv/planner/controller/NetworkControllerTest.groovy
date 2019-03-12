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

import com.github.tng.vnv.planner.restmock.TestCatalogueMock
import com.github.tng.vnv.planner.restmock.CuratorMock
import com.github.tng.vnv.planner.restmock.TestPlanRepositoryMock
import com.github.mrduguo.spring.test.AbstractSpec
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore

class NetworkControllerTest extends AbstractSpec {

    final def NETWORK_SERVICE_ID = 'input0ns-f213-4fae-8d3f-04358e1e1445'

    @Autowired
    CuratorMock curatorMock

    @Autowired
    TestCatalogueMock testCatalogueMock

    @Autowired
    TestPlanRepositoryMock testPlanRepositoryMock

    @Ignore
    void "schedule single NetworkService should produce successfully 1 Result for 1 testPlan"() {

        when:
        def entity = postForEntity('/tng-vnv-planner/api/v1/schedulers/services',
                ["service_uuid": NETWORK_SERVICE_ID]
                , Void.class)


        then:
        Thread.sleep(10000L);
/*
        while (executorMock.testSuiteResults.values().last().status!='SUCCESS')
            Thread.sleep(1000L);
*/
        curatorMock.networkServiceInstances.size()==1

        testPlanRepositoryMock.testPlans.size()==1
        testPlanRepositoryMock.testPlans.values().last().status=='SUCCESS'
        testPlanRepositoryMock.testPlans.values().each{testPlan ->
            testPlan.uuid.toInteger() != null
        }

        cleanup:
        curatorMock.reset()
        testPlanRepositoryMock.reset()
    }

    @Ignore
    void "retrieval of a single t st suite's related tests should successfully all the tag related tests"() {
        when:
        List tss = getForEntity('/tng-vnv-planner/api/v1/schedulers/services/{serviceUuid}/tests', List, NETWORK_SERVICE_ID).body
        then:

        tss.size() == 1
        cleanup:
        curatorMock.reset()

    }
}