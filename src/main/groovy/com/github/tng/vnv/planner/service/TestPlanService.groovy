package com.github.tng.vnv.planner.service

import com.github.tng.vnv.planner.model.NetworkService
import com.github.tng.vnv.planner.model.NetworkServiceDescriptor
import com.github.tng.vnv.planner.model.TestDescriptor
import com.github.tng.vnv.planner.model.TestPlan

interface TestPlanService {

    def findByService(NetworkServiceDescriptor nsd)

    def findByTest(TestDescriptor td)
	
	def findTestSuiteByTestingTags(String testingTags)
	
	def findNssByTestingTags(String testingTags)
	
	def findTdByExecutionTestingTags(String testingTags)
}