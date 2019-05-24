/*
 * Copyright (c) 2015 SONATA-NFV, 2017 5GTANGO [, ANY ADDITIONAL AFFILIATION]
 * ALL RIGHTS RESERVED.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Neither the name of the SONATA-NFV, 5GTANGO [, ANY ADDITIONAL AFFILIATION]
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * This work has been performed in the framework of the SONATA project,
 * funded by the European Commission under Grant number 671517 through
 * the Horizon 2020 and 5G-PPP programmes. The authors would like to
 * acknowledge the contributions of their colleagues of the SONATA
 * partner consortium (www.sonata-nfv.eu).
 *
 * This work has been performed in the framework of the 5GTANGO project,
 * funded by the European Commission under Grant number 761493 through
 * the Horizon 2020 and 5G-PPP programmes. The authors would like to
 * acknowledge the contributions of their colleagues of the 5GTANGO
 * partner consortium (www.5gtango.eu).
 */

package tng.vnv.planner

import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification
import tng.vnv.planner.controller.PackageController
import tng.vnv.planner.controller.TestSetController
import tng.vnv.planner.model.PackageCallback
import tng.vnv.planner.repository.TestSetRepository
import tng.vnv.planner.utils.TestPlanStatus

@Transactional
@SpringBootTest(classes = [Application.class], webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class TestSetControllerSpec extends Specification {

    @Autowired
    TestSetRepository testSetRepository

    @Autowired
    PackageController packageController

    @Autowired
    TestSetController testSetController

    def packageCallback

    @Test
    void "Get All Test Sets"() {
        when:
        packageCallback = new PackageCallback(packageId: '0d274a40-191a-4f6f-b6ef-2a6960c24bc2', confirmRequired: false)
        packageController.onChange(packageCallback).body
        def testSetList = testSetController.listAllTestSets()
        then:
        testSetList.size() > 0
    }

    @Test
    def "Get Test Set By Uuid"() {
        when:
        packageCallback = new PackageCallback(packageId: '0d274a40-191a-4f6f-b6ef-2a6960c24bc2', confirmRequired: false)
        def testPlan = packageController.onChange(packageCallback).body
        def testSet = testSetController.findTestSet(testPlan[0].testSetUuid)
        then:
        testSet.status == TestPlanStatus.SCHEDULED
    }

    @Test
    def "Cancel Test Set"() {
        when:
        packageCallback = new PackageCallback(packageId: '0d274a40-191a-4f6f-b6ef-2a6960c24bc2', confirmRequired: true)
        def testPlan = packageController.onChange(packageCallback).body
        testSetController.cancelTestSet(testPlan[0].testSetUuid)
        def cancellingTestSet = testSetRepository.findByUuid(testPlan[0].testSetUuid)
        then:
        cancellingTestSet.status == TestPlanStatus.CANCELLING
    }
}
