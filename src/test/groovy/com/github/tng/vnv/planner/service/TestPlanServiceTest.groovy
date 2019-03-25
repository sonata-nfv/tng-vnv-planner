package com.github.tng.vnv.planner.service

import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.model.TestSuite
import com.github.tng.vnv.planner.service.TestPlanService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import spock.lang.Ignore

//@DataJpaTest
class TestPlanServiceTest {

/*
    @Autowired
    private TestEntityManager entityManager;
*/

    @Autowired
    TestPlanService testPlanService

    @Autowired
    TestSuiteService testSuiteService

    @Ignore
    void 'schedule single Test and single NetworkService should produce successfully 2 Result for 2 testPlan'() {

        when:
        List testPlanList = [
                new TestPlan(status: 'dummyTestPlan0'),
                new TestPlan(status: 'dummyTestPlan1'),
                new TestPlan(status: 'dummyTestPlan2'),
                new TestPlan(status: 'dummyTestPlan3'),
        ]

        TestSuite testSuite = new TestSuite()
        testSuite = testSuiteService.save(testSuite)
        testPlanList?.forEach{tp -> tp.testSuite = testSuite}
        testPlanList?.forEach{tp -> testSuite.testPlans.add(tp)}
        testPlanList?.forEach{tp -> testPlanService.save(tp)}

        then:
        TestSuite  testSuite2 = testSuiteService.getOne(1L).testPlans?.size() == 4
        testPlanService.getLast().testSuite.uuid == testSuite2.uuid

        testPlanService.findAll().size() == 4
    }

}
