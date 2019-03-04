package com.github.tng.vnv.planner.repository

import com.github.tng.vnv.planner.helper.DebugHelper
import com.github.tng.vnv.planner.model.NetworkService
import com.github.tng.vnv.planner.oldlcm.model.TestPlanOld
import com.github.tng.vnv.planner.oldlcm.model.TestSuiteOld
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate
import groovy.util.logging.Log

import static com.github.tng.vnv.planner.helper.DebugHelper.callExternalEndpoint

@Log
@Repository("TestPlanRepository")
class TestPlanRepositoryImpl implements TestPlanRepository {

    @Autowired
    @Qualifier('restTemplateWithAuth')
    RestTemplate restTemplate

    @Autowired
    @Qualifier('restTemplateWithAuth')
    RestTemplate restTemplateWithAuth

    @Value('${app.trr.test.plan.create.endpoint}')
    def testPlanCreateEndpoint

    @Value('${app.trr.test.plan.update.endpoint}')
    def testPlanUpdateEndpoint

    @Value('${app.vnvgk.test.list.by.tag.endpoint}')
    def testListByTagEndpoint

    @Value('${app.gk.service.list.by.tag.endpoint}')
    def serviceListByTagEndpoint

    TestPlanOld createTestPlan(TestPlanOld testPlan) {
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        def entity = new HttpEntity<TestPlanOld>(testPlan ,headers)
        callExternalEndpoint(restTemplate.postForEntity(testPlanCreateEndpoint,entity,TestPlanOld),'TestResultRepository.createTestPlan',testPlanCreateEndpoint).body
    }

    TestPlanOld updateTestPlan(TestPlanOld testPlan) {
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        def entity = new HttpEntity<TestPlanOld>(testPlan ,headers)
        callExternalEndpoint(restTemplate.exchange(testPlanUpdateEndpoint, HttpMethod.PUT, entity, TestPlanOld.class ,testPlan.uuid),'TestResultRepository.updatePlan',testPlanUpdateEndpoint).body
    }


    List<NetworkService> findNssByTestTag(String tag) {
        DebugHelper.callExternalEndpoint(restTemplateWithAuth.getForEntity(serviceListByTagEndpoint, NetworkService[], tag),
                'TestPlanRepositoryImpl.findNssByTestTag',serviceListByTagEndpoint).body
    }

    List<TestSuiteOld> findTssByTestTag(String tag) {
        DebugHelper.callExternalEndpoint(restTemplateWithAuth.getForEntity(testListByTagEndpoint, TestSuiteOld[], tag),
                'TestPlanRepositoryImpl.findTssByTestTag',testListByTagEndpoint).body
    }
}
