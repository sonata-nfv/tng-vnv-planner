package com.github.tng.vnv.planner.repository

import com.github.tng.vnv.planner.model.TestPlan
import org.springframework.data.jpa.repository.JpaRepository

interface TestPlanRepository extends JpaRepository<TestPlan, Long> {}