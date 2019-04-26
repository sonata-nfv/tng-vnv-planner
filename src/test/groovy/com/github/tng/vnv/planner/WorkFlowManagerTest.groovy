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
    public static final String TEST_PLAN_UUID_1 = '109873681'
    public static final String TEST_PLAN_UUID_2 = '109873682'
    public static final String TEST_PLAN_UUID_3 = '109873683'
    public static final String TEST_PLAN_UUID_4 = '109873684'

    void 'Curator sends a COMPLETED testPlan should the next test plan become STARTING'() {
        setup:
        curatorMock.isBusy(false)
        when:
        scheduleTestPlan( TEST_PLAN_UUID_3, TEST_PLAN_STATUS.SCHEDULED, '1st scheduled testPlan')
        and:
        def entity = postForEntity('/api/v1/test-plans/on-change/completed',
                [
                        event_actor: 'tng-vnv-curator',
                        status: 'COMPLETED',
                        test_plan_uuid: TEST_PLAN_UUID_3,
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
        testPlanService.findByUuid(TEST_PLAN_UUID_3).status == TEST_PLAN_STATUS.COMPLETED
        cleanup:
        cleanTestPlanDB()
    }

    TestPlan scheduleTestPlan(String uuid, String status, String description){
        testPlanService.save(new TestPlan(uuid: uuid, status: status, description: description))
    }
}
