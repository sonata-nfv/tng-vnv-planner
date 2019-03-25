package com.github.tng.vnv.planner.controller

import com.github.mrduguo.spring.test.AbstractSpec
import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.restmock.TestPlanRepositoryMock
import com.github.tng.vnv.planner.service.TestPlanService
import com.github.tng.vnv.planner.service.TestSuiteService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

class TestPlanControllerTest extends AbstractSpec {

    public static final String LIST_TEST_PLANS_PACKAGE_ID ='multiple_scheduler:test:0.0.1'

    @Autowired
    TestPlanRepositoryMock testPlanRepositoryMock

    void "schedule request of a test plan list should successfully save all test plans"() {

        when:

        def entity = postForEntity('/tng-vnv-planner/api/v1/test-plans',
                [
                        'test_plans':
                                [
                                        [
                                                'package_id':  LIST_TEST_PLANS_PACKAGE_ID,
                                                'status': 'dummyTestPlan1',
                                                'index': '1',
                                        ],
                                        [
                                                'package_id':  LIST_TEST_PLANS_PACKAGE_ID,
                                                'status': 'dummyTestPlan2',
                                                'index': '2',
                                        ],
                                        [
                                                'package_id':  LIST_TEST_PLANS_PACKAGE_ID,
                                                'status': 'dummyTestPlan3',
                                                'index': '3',
                                        ],
                                        [
                                                'package_id':  LIST_TEST_PLANS_PACKAGE_ID,
                                                'status': 'dummyTestPlan4',
                                                'index': '4',
                                        ],
                        ]
                ]
                , Void.class)

        then:
        entity.statusCode == HttpStatus.OK
        testPlanRepositoryMock.testPlans.size() == 4

        cleanup:

        testPlanRepositoryMock.reset()

    }

}
