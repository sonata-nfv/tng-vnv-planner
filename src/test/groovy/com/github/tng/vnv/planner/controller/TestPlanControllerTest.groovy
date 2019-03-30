package com.github.tng.vnv.planner.controller

import com.github.mrduguo.spring.test.AbstractSpec
import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.repository.TestPlanRepository
import com.github.tng.vnv.planner.restmock.DataMock
import com.github.tng.vnv.planner.restmock.TestPlanRepositoryMock
import com.github.tng.vnv.planner.service.TestPlanService
import com.github.tng.vnv.planner.service.TestSuiteService
import com.github.tng.vnv.planner.utils.TEST_PLAN_STATUS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

class TestPlanControllerTest extends AbstractSpec {

    public static final String IMEDIA_TEST_PLAN_SERVICE_UUID ='immedia0-9429-4a07-b7af-dd429d6d04o3'
    public static final String IMEDIA_TEST_PLAN_TEST_UUID ='immedia0-8cc7-47a9-9112-6wff9e88wu2k'
    public static final String LATENCY_TEST_PLAN_SERVICE_UUID ='input0ns-f213-4fae-8d3f-04358e1e1451'
    public static final String LATENCY_TEST_PLAN_TEST_UUID ='input0ts-75f5-4ca1-90c8-12ec80a79836'
    public static final String TAG_UNRELATED_TEST_PLAN_SERVICE_UUID ='input0ns-f213-4fae-8d3f-04358e1e1451'
    public static final String TAG_UNRELATED_TEST_PLAN_TEST_UUID ='input0ts-75f5-4ca1-90c8-12ec80a79836'
    public static final String DIY_DESCRIPTOR_TEST_PLAN_SERVICE_UUID ='4dd4cb15-76b8-46fd-b3c0-1b165cc332f9'
    public static final String DIY_DESCRIPTOR_TEST_PLAN_TEST_UUID ='b68dbe19-5c02-4865-8c4b-5e43ada1b67d'
    public static final String DIY_DESCRIPTOR_TEST_PLAN_VALIDATION_REQUIRED_TEST_UUID ='b68dbe19-5c02-4865-8c4b-5e43ada1b67c'
    public static final String DIY_DESCRIPTOR_TEST_PLAN_CONFIRMED_TEST_UUID ='b68dbe19-5c02-4865-8c4b-5e43ada1b67b'


    @Autowired
    TestPlanRepository testPlanRepository

    @Autowired
    TestPlanRepositoryMock testPlanRepositoryMock

    void "schedule request of a test plan list should successfully save all test plans"() {


        when:

        def entity = postForEntity('/tng-vnv-planner/api/v1/test-plans',
                [
                        'test_plans':
                                [
                                        [
                                                'service_uuid': IMEDIA_TEST_PLAN_SERVICE_UUID,
                                                'test_uuid': IMEDIA_TEST_PLAN_TEST_UUID,
                                                'description': 'dummyTestPlan1-index1',
                                                'index': '1',
                                        ],
                                        [
                                                'service_uuid': LATENCY_TEST_PLAN_SERVICE_UUID,
                                                'test_uuid': LATENCY_TEST_PLAN_TEST_UUID,
                                                'description': 'dummyTestPlan2-index3',
                                                'index': '3',
                                        ],
                                        [
                                                'service_uuid': TAG_UNRELATED_TEST_PLAN_SERVICE_UUID,
                                                'test_uuid': TAG_UNRELATED_TEST_PLAN_TEST_UUID,
                                                'description': 'dummyTestPlan3-index2',
                                                'index': '2',
                                        ],
                                        [
                                                'nsd': DataMock.getService(DIY_DESCRIPTOR_TEST_PLAN_SERVICE_UUID).nsd,
                                                'testd': DataMock.getTest(DIY_DESCRIPTOR_TEST_PLAN_TEST_UUID).testd,
                                                'description': 'dummyTestPlan4-index4',
                                                'index': '4',
                                        ],
                                ]
                ]
                , Void.class)

        then:
        entity.statusCode == HttpStatus.OK
        testPlanRepositoryMock.testPlans.size() == 4
        def testPlans = testPlanRepositoryMock.listTestPlans()
        testPlans.get(1).description == 'dummyTestPlan3-index2'
        testPlans.get(2).description == 'dummyTestPlan2-index3'




        cleanup:

        testPlanRepositoryMock.reset()

    }

    void "schedule request with validation required for one test plan should successfully schedule only the not validation required test plans"() {


        when:
        def entity = postForEntity('/tng-vnv-planner/api/v1/test-plans',
                [
                        'test_plans':
                                [
                                        [
                                                'service_uuid': IMEDIA_TEST_PLAN_SERVICE_UUID,
                                                'test_uuid': IMEDIA_TEST_PLAN_TEST_UUID,
                                                'description': 'dummyTestPlan1-non-validation_required',
                                                'index': '1',
                                        ],
                                        [
                                                'nsd': DataMock.getService(DIY_DESCRIPTOR_TEST_PLAN_SERVICE_UUID).nsd,
                                                'testd': DataMock.getTest(DIY_DESCRIPTOR_TEST_PLAN_VALIDATION_REQUIRED_TEST_UUID).testd,
                                                'description': 'dummyTestPlan-validation_required',
                                                'index': '2',
                                        ],
                                        [
                                                'nsd': DataMock.getService(DIY_DESCRIPTOR_TEST_PLAN_SERVICE_UUID).nsd,
                                                'testd': DataMock.getTest(DIY_DESCRIPTOR_TEST_PLAN_CONFIRMED_TEST_UUID).testd,
                                                'description': 'dummyTestPlan-validation_confirmed',
                                                'index': '3',
                                        ],
                                ]
                ]
                , Void.class)

        then:
        entity.statusCode == HttpStatus.OK
        testPlanRepositoryMock.testPlans.size() == 2
        def testPlans = testPlanRepositoryMock.listTestPlans()
        testPlans.get(0).status == TEST_PLAN_STATUS.SCHEDULED
        testPlans.get(0).description == 'dummyTestPlan1-non-validation_required'
        testPlans.get(1).status == TEST_PLAN_STATUS.SCHEDULED
        testPlans.get(1).description == 'dummyTestPlan-validation_confirmed'

        cleanup:
        testPlanRepositoryMock.reset()

    }

    void "update request of a test plan list should successfully update all test plans"() {


        when:

        def entity = postForEntity('/tng-vnv-planner/api/v1/test-plans',
                [
                        'test_plans':
                                [
                                        [
                                                'service_uuid': IMEDIA_TEST_PLAN_SERVICE_UUID,
                                                'test_uuid': IMEDIA_TEST_PLAN_TEST_UUID,
                                                'description': 'dummyTestPlan1-index1',
                                                'index': '1',
                                        ],
                                        [
                                                'service_uuid': LATENCY_TEST_PLAN_SERVICE_UUID,
                                                'test_uuid': LATENCY_TEST_PLAN_TEST_UUID,
                                                'description': 'dummyTestPlan2-index3',
                                                'index': '3',
                                        ],
                                        [
                                                'service_uuid': TAG_UNRELATED_TEST_PLAN_SERVICE_UUID,
                                                'test_uuid': TAG_UNRELATED_TEST_PLAN_TEST_UUID,
                                                'description': 'dummyTestPlan3-index2',
                                                'index': '2',
                                        ],
                                        [
                                                'nsd': DataMock.getService(DIY_DESCRIPTOR_TEST_PLAN_SERVICE_UUID).nsd,
                                                'testd': DataMock.getTest(DIY_DESCRIPTOR_TEST_PLAN_TEST_UUID).testd,
                                                'description': 'dummyTestPlan4-index4',
                                                'index': '4',
                                        ],
                                ]
                ]
                , Void.class)

        then:
        entity.statusCode == HttpStatus.OK
        testPlanRepositoryMock.testPlans.size() == 4
        def testPlans = testPlanRepositoryMock.listTestPlans()
        testPlans.get(1).description == 'dummyTestPlan3-index2'
        testPlans.get(2).description == 'dummyTestPlan2-index3'




        cleanup:

        testPlanRepositoryMock.reset()

    }

}
