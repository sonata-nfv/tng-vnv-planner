package com.github.tng.vnv.planner.repository


import com.github.tng.vnv.planner.oldlcm.model.TestPlanOld

interface TestPlanRepository {
    def findNssByTestTag(String tag)
    def findTssByTestTag(String tag)
    def createTestPlan(TestPlanOld testPlan)
    def updateTestPlan(TestPlanOld testPlan)
}
