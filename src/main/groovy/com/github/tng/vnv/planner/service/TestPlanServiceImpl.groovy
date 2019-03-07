package com.github.tng.vnv.planner.service

import com.github.tng.vnv.planner.model.NetworkService
import com.github.tng.vnv.planner.oldlcm.model.TestSuiteOld


import com.github.tng.vnv.planner.model.NetworkServiceDescriptor
import com.github.tng.vnv.planner.model.TestDescriptor
import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.model.TestSuite
import com.github.tng.vnv.planner.repository.TestPlanRepository
import groovy.util.logging.Log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Log
@Service("TestPlanService")
class TestPlanServiceImpl implements TestPlanService {

	@Autowired
	TestPlanRepository testPlanRepository

	def findByService(NetworkServiceDescriptor nsd) {
		List<TestPlan> tps = [] as ArrayList
		nsd.testingTags?.each { tt ->
			testPlanRepository.findTssByTestTag(tt)?.each { td ->
				tps << new TestPlan(networkServiceDescriptor:nsd, testDescriptor:td)
			}
		}
		tps
	}

	def findByTest(TestDescriptor td) {
		List<TestPlan> tps = [] as ArrayList
		td.testExecution?.each { tt ->
			testPlanRepository.findNssByTestTag(tt)?.each { nsd ->
				tps <<  new TestPlan(networkServiceDescriptor:nsd, testDescriptor:td)
			}
		}
		tps
	}


	def findTestSuiteByTestingTags(String testingTags) {
		def tps = [] as Set
		testingTags = testingTags.trim();
		List<String> tags = testingTags.substring(1, testingTags.length() - 1).trim().split("\\s*,\\s*").toList();
		//findNssByTestingTags
		List<NetworkService> nss = findNssByTestingTags(testingTags)
		//findTdByExecutionTestingTags
		List<TestSuiteOld> tds = findTdByExecutionTestingTags(testingTags)
		//find correspondence and create test suite
		
		TestSuite ts  = new TestSuite()
		ts
	}

	def findNssByTestingTags(String testingTags) {
		def nss = [] as Set
		testingTags = testingTags.trim();
		List<String> tags = testingTags.substring(1, testingTags.length() - 1).trim().split("\\s*,\\s*").toList();
		tags?.each { tt ->
			testPlanRepository.findNssByTestTag(tt)?.each { ns ->
				Boolean add = false
				ns.nsd.testingTags?.each { testingTag ->
					if(tags.contains(testingTag)) {
						add = true
					}
				}
				if(add)
					nss << ns
			}
		}
		nss
	}

	def findTdByExecutionTestingTags(String testingTags) {
		def tds = [] as Set
		testingTags = testingTags.trim();
		List<String> tags = testingTags.substring(1, testingTags.length() - 1).trim().split("\\s*,\\s*").toList();
		tags?.each { tt ->
			testPlanRepository.findTssByTestTag(tt)?.each { ts ->
				Boolean add = false
				ts.testd.testExecution?.each { testingTag ->
					if(tags.contains(testingTag)) {
						add = true
					}
				}
				if(add)
					tds << ts
			}
		}
		tds
	}

}
