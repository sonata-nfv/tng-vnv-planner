package com.github.tng.vnv.planner.repository

import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.model.TestSuite
import org.springframework.data.jpa.repository.JpaRepository

interface TestPlanRepository extends JpaRepository<TestPlan, Long> {
    TestPlan findFirstByStatus(String status)
    TestPlan findLastByUuid(String uuid)
    List<TestPlan> findByTestSuite(TestSuite t)
}