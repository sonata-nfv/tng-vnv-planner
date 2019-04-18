package com.github.tng.vnv.planner.controller

import com.github.tng.vnv.planner.config.TestRestSpec
import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.model.TestSuite
import com.github.tng.vnv.planner.restmock.CuratorMock
import com.github.tng.vnv.planner.restmock.DataMock
import com.github.tng.vnv.planner.service.TestPlanService
import com.github.tng.vnv.planner.service.TestSuiteService
import com.github.tng.vnv.planner.utils.TEST_PLAN_STATUS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import spock.lang.Ignore

class TestPlanControllerTest extends TestRestSpec {

    @Autowired
    TestPlanService testPlanService
    @Autowired
    TestSuiteService testSuiteService

    @Autowired
    CuratorMock curatorMock

    public static final String IMEDIA_TEST_PLAN_SERVICE_UUID = 'immedia0-9429-4a07-b7af-dd429d6d04o3'
    public static final String IMEDIA_TEST_PLAN_TEST_UUID = 'immedia0-8cc7-47a9-9112-6wff9e88wu2k'
    public static final String LATENCY_TEST_PLAN_SERVICE_UUID = 'input0ns-f213-4fae-8d3f-04358e1e1451'
    public static final String LATENCY_TEST_PLAN_TEST_UUID = 'input0ts-75f5-4ca1-90c8-12ec80a79836'
    public static final String TAG_UNRELATED_TEST_PLAN_SERVICE_UUID = 'input0ns-f213-4fae-8d3f-04358e1e1451'
    public static final String TAG_UNRELATED_TEST_PLAN_TEST_UUID = 'input0ts-75f5-4ca1-90c8-12ec80a79836'
    public static final String DIY_DESCRIPTOR_TEST_PLAN_SERVICE_UUID = '4dd4cb15-76b8-46fd-b3c0-1b165cc332f9'
    public static final String DIY_DESCRIPTOR_TEST_PLAN_TEST_UUID = 'b68dbe19-5c02-4865-8c4b-5e43ada1b67d'
    public static final String DIY_DESCRIPTOR_TEST_PLAN_VALIDATION_REQUIRED_TEST_UUID = 'b68dbe19-5c02-4865-8c4b-5e43ada1b67c'
    public static final String DIY_DESCRIPTOR_TEST_PLAN_CONFIRMED_TEST_UUID = 'b68dbe19-5c02-4865-8c4b-5e43ada1b67b'

    public static final String TEST_PLAN_UUID = '109873681'
    public static final String TEST_PLAN_UUID2 = '109873682'

    void "when curator is busy, schedule request of a test plan list should successfully save all test plans"() {
        when:
        curatorMock.active = false
        def entity = postForEntity('/api/v1/test-plans',
                [
                        'test_plans':
                                [
                                        [
                                                'service_uuid': IMEDIA_TEST_PLAN_SERVICE_UUID,
                                                'test_uuid'   : IMEDIA_TEST_PLAN_TEST_UUID,
                                                'description' : 'dummyTestPlan1-index1',
                                                'index'       : '1',
                                        ],
                                        [
                                                'service_uuid': LATENCY_TEST_PLAN_SERVICE_UUID,
                                                'test_uuid'   : LATENCY_TEST_PLAN_TEST_UUID,
                                                'description' : 'dummyTestPlan2-index3',
                                                'index'       : '3',
                                        ],
                                        [
                                                'service_uuid': TAG_UNRELATED_TEST_PLAN_SERVICE_UUID,
                                                'test_uuid'   : TAG_UNRELATED_TEST_PLAN_TEST_UUID,
                                                'description' : 'dummyTestPlan3-index2',
                                                'index'       : '2',
                                        ],
                                        [
                                                'nsd'        : DataMock.getService(DIY_DESCRIPTOR_TEST_PLAN_SERVICE_UUID).nsd,
                                                'testd'      : DataMock.getTest(DIY_DESCRIPTOR_TEST_PLAN_TEST_UUID).testd,
                                                'description': 'dummyTestPlan4-index4',
                                                'index'      : '4',
                                        ],
                                ]
                ]
                , Void.class)
        then:
        entity.statusCode == HttpStatus.OK
        def testPlans = testPlanService.testPlanRepository.findAll().findAll { it.status == "SCHEDULED" }
        testPlans[1].description == 'dummyTestPlan3-index2'
        testPlans[2].description == 'dummyTestPlan2-index3'
        cleanup:
        cleanTestPlanDB()
    }

    void "schedule request with validation required for one test plan should successfully schedule only the not validation required test plans"() {
        when:
        curatorMock.active = true
        def entity = postForEntity('/api/v1/test-plans',
                [
                        'test_plans':
                                [
                                        [
                                                'service_uuid': IMEDIA_TEST_PLAN_SERVICE_UUID,
                                                'test_uuid'   : IMEDIA_TEST_PLAN_TEST_UUID,
                                                'description' : 'dummyTestPlan1-non-validation_required',
                                                'index'       : '1',
                                        ],
                                        [
                                                'nsd'        : DataMock.getService(DIY_DESCRIPTOR_TEST_PLAN_SERVICE_UUID).nsd,
                                                'testd'      : DataMock.getTest(DIY_DESCRIPTOR_TEST_PLAN_VALIDATION_REQUIRED_TEST_UUID).testd,
                                                'description': 'dummyTestPlan-validation_required',
                                                'index'      : '2',
                                        ],
                                        [
                                                'nsd'        : DataMock.getService(DIY_DESCRIPTOR_TEST_PLAN_SERVICE_UUID).nsd,
                                                'testd'      : DataMock.getTest(DIY_DESCRIPTOR_TEST_PLAN_CONFIRMED_TEST_UUID).testd,
                                                'description': 'dummyTestPlan-validation_confirmed',
                                                'index'      : '3',
                                        ],
                                ]
                ]
                , Void.class)
        then:
        entity.statusCode == HttpStatus.OK
        def testPlans = testPlanService.testPlanRepository.findAll().takeRight(2)
        testPlans.get(0).status == TEST_PLAN_STATUS.STARTING
        testPlans.get(0).description == 'dummyTestPlan1-non-validation_required'
        testPlans.get(1).status == TEST_PLAN_STATUS.SCHEDULED
        testPlans.get(1).description == 'dummyTestPlan-validation_confirmed'
        cleanup:
        cleanTestPlanDB()
    }

    void "schedule request with validation required for one test plan should successfully schedule no test plans"() {
        when:
        curatorMock.active = true
        def entity = postForEntity('/api/v1/test-plans',
                [
                        'test_plans':
                                [
                                        [
                                                "service_uuid": DIY_DESCRIPTOR_TEST_PLAN_SERVICE_UUID,
                                                "test_uuid": DIY_DESCRIPTOR_TEST_PLAN_VALIDATION_REQUIRED_TEST_UUID,
                                                'description': 'dummyTestPlan1-validation_required',
                                                'index': '1',
                                        ],
                                ]
                ]
                , Void.class)
        then:
        entity.statusCode == HttpStatus.OK
        testPlanService.testPlanRepository.findAll()
                .findAll{it.status == "SCHEDULED"}.size() == 0
        cleanup:
        cleanTestPlanDB()
    }

    void "delete request for one test plan should successfully change the status of the test plan to CANCELING scheduled test plan"() {
        when:
        scheduleTestPlan(TEST_PLAN_UUID, TEST_PLAN_STATUS.CREATED, 'scheduled testPlan\'s status which will turn into canceling')
        delete('/api/v1/test-plans/{uuid}',TEST_PLAN_UUID)
        then:
        testPlanService.testPlanRepository.findByUuid(TEST_PLAN_UUID).status == TEST_PLAN_STATUS.CANCELLING
        cleanup:
        cleanTestPlanDB()
    }

    void "list test plans request for one testPlan list uuid test plan should successfully return the list of corresponding test plans"() {
        when:
        def testPlan = scheduleTestPlan(TEST_PLAN_UUID, TEST_PLAN_STATUS.CREATED, 'scheduled testPlan\'s status which will be listed for a specific testPlanListUuid')
        def entity = getForEntity('/api/v1/test-plans/{testPlanListUuid}', TestPlan[], testPlan.testSuite.uuid)
        then:
        entity.statusCode == HttpStatus.OK
        entity.body.size() == 1
        cleanup:
        cleanTestPlanDB()
    }

    void "test plans request for testPlan list equal to 0 should successfully return the list of all test plans"() {
        when:
        scheduleTestPlan(TEST_PLAN_UUID, "TEST_LIST_ALL_STATUS", '')
        then:
        def testPlans = getForEntity('/api/v1/test-plans/{testPlanListUuid}', TestPlan[],'0').body
        testPlans.size()>=1
        cleanup:
        cleanTestPlanDB()
    }

    TestPlan scheduleTestPlan(String uuid, String status, String description){
        def testPlan = new TestPlan(uuid: uuid, status: status, description: description)
        def testSuite = new TestSuite(uuid: UUID.randomUUID().toString())
        testSuite = testSuiteService.save(testSuite)
        testPlan.testSuite = testSuite
        testSuite.testPlans.add(testPlan)
        testPlanService.save(testPlan)
    }


}
