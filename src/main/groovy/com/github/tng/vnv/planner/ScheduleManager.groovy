/*
 * Copyright (c) 2015 SONATA-NFV, 2019 5GTANGO [, ANY ADDITIONAL AFFILIATION]
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

package com.github.tng.vnv.planner

import com.github.tng.vnv.planner.service.NetworkServiceService
import com.github.tng.vnv.planner.service.TestPlanService
import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.service.TestService
import com.github.tng.vnv.planner.utils.TEST_PLAN_STATUS
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import static org.springframework.util.StringUtils.isEmpty

@Slf4j
@Component
class ScheduleManager {

    @Autowired
    TestService testService
    @Autowired
    NetworkServiceService networkServiceService
    @Autowired
    TestPlanService testPlanService
    @Autowired
    WorkflowManager workflowManager

    List<TestPlan> createByPackage(def uuid) {
                testPlanService.buildTestPlansByPackage(uuid)?.each{create(it)}
    }

    TestPlan create(TestPlan tp) {
        tp.uuid=(!isEmpty(tp.uuid))?tp.uuid:UUID.randomUUID().toString()
        if (!isEmpty(tp.nsdUuid) && !isEmpty(tp.testdUuid)) {
            log.info("#~# vnvlog isConfirmRequired({})",testService.isConfirmRequired(tp.testdUuid))  
            log.info("#~# vnvlog tp.confirmRequired({})",tp.confirmRequired)
            log.info("#~# vnvlog tp.confirmed({})",tp.confirmed)
                if ((testService.isConfirmRequired(tp.testdUuid)
                        || !isEmpty(tp.confirmRequired) && tp.confirmRequired == '1')
                        && (isEmpty(tp.confirmed) || tp.confirmed != '1')) {
                    tp.status = TEST_PLAN_STATUS.NOT_CONFIRMED
                } else {
                    tp.status = TEST_PLAN_STATUS.SCHEDULED
                }
            testPlanService.save(tp)
        }
    }

    TestPlan update(TestPlan tp) {
        tp.status = TEST_PLAN_STATUS.SCHEDULED
        testPlanService.save(tp)
    }
}
