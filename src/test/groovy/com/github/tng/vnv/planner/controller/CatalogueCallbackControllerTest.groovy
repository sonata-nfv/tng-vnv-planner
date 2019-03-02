package com.github.tng.vnv.planner.controller


import com.github.mrduguo.spring.test.AbstractSpec
import com.github.tng.vnv.planner.restmock.TestCatalogueMock
import com.github.tng.vnv.planner.oldlcm.restmock.ExecutorMock
import com.github.tng.vnv.planner.restmock.CuratorMock
import com.github.tng.vnv.planner.restmock.TestPlanRepositoryMock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

class CatalogueCallbackControllerTest extends AbstractSpec {

    public static final String MULTIPLE_TEST_PLANS_PACKAGE_ID ='multiple_scheduler:test:0.0.1'
    public static final String BAD_REQUEST_PACKAGE_ID ='error:test:0.0.1'

    @Autowired
    CuratorMock curatorMock

    @Autowired
    ExecutorMock executorMock

    @Autowired
    TestCatalogueMock testCatalogueMock

    @Autowired
    TestPlanRepositoryMock testPlanRepositoryMock

    void 'schedule single TestSuite and single NetworkService should produce successfully 2 Result for 2 testPlan'() {

        when:
        def entity = postForEntity('/tng-vnv-planner/api/v1/packages/on-change',
                [
                        event_name: UUID.randomUUID().toString(),
                        package_id:  MULTIPLE_TEST_PLANS_PACKAGE_ID,
                ]
                , Void.class)

        then:
        Thread.sleep(10000L);
        while (curatorMock.networkServiceInstances.values().last().status!='TERMINATED')
            Thread.sleep(1000L);
        entity.statusCode == HttpStatus.OK
        curatorMock.networkServiceInstances.size()==3
        executorMock.testSuiteResults.size()==3
        executorMock.testSuiteResults.values().last().status=='SUCCESS'

        testPlanRepositoryMock.testPlans.size()==3
        testPlanRepositoryMock.testPlans.values().last().status=='SUCCESS'
        testPlanRepositoryMock.testPlans.values().last().networkServiceInstances.size()==1
        testPlanRepositoryMock.testPlans.values().last().testSuiteResults.last().status=='SUCCESS'

        cleanup:
        curatorMock.reset()
        executorMock.reset()
        testPlanRepositoryMock.reset()
    }
}
