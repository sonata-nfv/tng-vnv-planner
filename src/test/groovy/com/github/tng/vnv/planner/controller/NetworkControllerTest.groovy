package com.github.tng.vnv.planner.controller

import com.github.tng.vnv.planner.restmock.TestCatalogueMock
import com.github.tng.vnv.planner.oldlcm.restmock.ExecutorMock
import com.github.tng.vnv.planner.restmock.CuratorMock
import com.github.tng.vnv.planner.restmock.TestPlanRepositoryMock
import com.github.mrduguo.spring.test.AbstractSpec
import org.springframework.beans.factory.annotation.Autowired

class NetworkControllerTest extends AbstractSpec {

    final def NETWORK_SERVICE_ID = 'input0ns-f213-4fae-8d3f-04358e1e1445'

    @Autowired
    CuratorMock curatorMock

    @Autowired
    ExecutorMock executorMock

    @Autowired
    TestCatalogueMock testCatalogueMock

    @Autowired
    TestPlanRepositoryMock testPlanRepositoryMock

    void "schedule single NetworkService should produce successfully 1 Result for 1 testPlan"() {

        when:
        def entity = postForEntity('/tng-vnv-planner/api/v1/schedulers/services',
                ["service_uuid": NETWORK_SERVICE_ID]
                , Void.class)


        then:
        Thread.sleep(10000L);
        while (executorMock.testSuiteResults.values().last().status!='SUCCESS')
            Thread.sleep(1000L);
        curatorMock.networkServiceInstances.size()==1

        executorMock.testSuiteResults.size()==1

        testPlanRepositoryMock.testPlans.size()==1
        testPlanRepositoryMock.testPlans.values().last().status=='SUCCESS'
        testPlanRepositoryMock.testPlans.values().last().networkServiceInstances.size()==1
        testPlanRepositoryMock.testPlans.values().each{testPlan ->
            testPlan.testSuiteResults.size()==1
        }
        testPlanRepositoryMock.testPlans.values().last().testSuiteResults.last().status=='SUCCESS'

        cleanup:
        curatorMock.reset()
        executorMock.reset()
        testPlanRepositoryMock.reset()
    }

    void "retrieval of a single test suite's related testSuites should successfully all the tag related tests"() {
        when:
        List tss = getForEntity('/tng-vnv-planner/api/v1/schedulers/services/{serviceUuid}/tests', List, NETWORK_SERVICE_ID).body
        then:

        tss.size() == 1
        cleanup:
        curatorMock.reset()

    }
}