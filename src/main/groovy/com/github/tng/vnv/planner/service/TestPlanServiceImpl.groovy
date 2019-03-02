package com.github.tng.vnv.planner.service

import com.github.tng.vnv.planner.model.NetworkServiceDescriptor
import com.github.tng.vnv.planner.model.TestDescriptor
import com.github.tng.vnv.planner.model.TestPlan
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
}
