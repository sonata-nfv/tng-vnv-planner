package com.github.tng.vnv.planner

import com.github.tng.vnv.planner.config.TestRestSpec
import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.model.TestSuite
import com.github.tng.vnv.planner.restmock.CuratorMock
import com.github.tng.vnv.planner.restmock.TestPlanRepositoryMock
import com.github.tng.vnv.planner.service.TestPlanService
import com.github.tng.vnv.planner.service.TestSuiteService
import com.github.tng.vnv.planner.utils.TEST_PLAN_STATUS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

class WorkFlowManagerTest extends TestRestSpec {

    @Autowired
    TestPlanService testPlanService
    @Autowired
    TestSuiteService testSuiteService

    @Autowired
    TestPlanRepositoryMock testPlanRepositoryMock

    @Autowired
    CuratorMock curatorMock

    public static final String TEST_RESULT_UUID = UUID.randomUUID().toString()
    public static final String TEST_PLAN_UUID_1 = '109873681'
    public static final String TEST_PLAN_UUID_2 = '109873682'
    public static final String TEST_PLAN_UUID_3 = '109873683'
    public static final String TEST_PLAN_UUID_4 = '109873684'

    void 'when workflowManager checks the scheduled test plans should send the oldest SCHEDULED test plan to Curator'() {

        setup:
        cleanTestPlansRepo()
        cleanTestPlanDB()
        when:
        curatorMock.active = false
        scheduleTestPlan(TEST_PLAN_UUID_1, TEST_PLAN_STATUS.SCHEDULED, 'Single scheduled testPlan')
        then:
        testPlanService.findNextScheduledTestPlan().status==TEST_PLAN_STATUS.SCHEDULED


    }

    void 'when workflowManager checks the scheduled test plans and there is Curator to get the testPlan should testPlan status changed to PENDING'() {

        setup:
        cleanTestPlansRepo()
        cleanTestPlanDB()
        when:
        curatorMock.active = true
        curatorMock.testPlanResponseUuid = TEST_PLAN_UUID_2
        scheduleTestPlan(TEST_PLAN_UUID_2, TEST_PLAN_STATUS.SCHEDULED, 'Single scheduled testPlan')
        then:
        Thread.sleep(1500L);
        testPlanService.testPlanRepository.findAll().find { it.uuid == TEST_PLAN_UUID_2}.status ==TEST_PLAN_STATUS.PENDING
    }

    void 'checks the test plans and successfully passes a test plan to Curator should the oldest SCHEDULED test plan become initially PENDING and finally COMPLETED'() {

        setup:

        when:
        curatorMock.active = true
        scheduleTestPlan( TEST_PLAN_UUID_3, TEST_PLAN_STATUS.SCHEDULED, '1st scheduled testPlan')
        scheduleTestPlan(TEST_PLAN_UUID_4, TEST_PLAN_STATUS.SCHEDULED, '2nd scheduled testPlan')
        and:
        Thread.sleep(1500L);
        def entity = postForEntity('/tng-vnv-planner/api/v1/test-plans/on-change/completed',
                [
                        event_actor: 'tng-vnv-curator',
                        status: 'COMPLETED',
                        test_plan_uuid: TEST_PLAN_UUID_3,
                        test_results_uuid: TEST_RESULT_UUID,
                        test_plan_repository: 'tng-rep',
                        test_results_repository: 'tng-res',
                ]
                , Void.class)
        then:
        Thread.sleep(15000L);
        entity.statusCode == HttpStatus.OK
        testPlanService.testPlanRepository.findLastByUuid(TEST_PLAN_UUID_3).status == TEST_PLAN_STATUS.COMPLETED
        cleanup:
        cleanTestPlanDB()
    }

    TestPlan scheduleTestPlan(String uuid, String status, String description){

        def testPlan = new TestPlan(uuid: uuid, status: status, description: description)
        def testSuite = new TestSuite()
        testSuite = testSuiteService.save(testSuite)
        testPlan.testSuite = testSuite
        testSuite.testPlans.add(testPlan)
        testPlanService.save(testPlan)
    }
}
