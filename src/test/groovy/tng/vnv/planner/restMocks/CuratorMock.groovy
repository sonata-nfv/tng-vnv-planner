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

package tng.vnv.planner.restMocks

import tng.vnv.planner.utils.TangoLogger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tng.vnv.planner.model.TestRequest
import tng.vnv.planner.model.TestResponse
import tng.vnv.planner.utils.TestPlanStatus

@RestController
@RequestMapping('/api/v1')
class CuratorMock {

  //Tango logger
    def tangoLogger = new TangoLogger()
    String tangoLoggerType = null;
    String tangoLoggerOperation = null;
    String tangoLoggerMessage = null;
    String tangoLoggerStatus = null;

    @PostMapping('/test-preparations')
    ResponseEntity<TestResponse> testRequest(@RequestBody TestRequest testRequest){
        tangoLoggerType = "I";
        tangoLoggerOperation = "CuratorMock.testRequest";
        tangoLoggerMessage = ("Curator: received test execution request");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        def testResponse = new TestResponse(status: TestPlanStatus.STARTING)
        ResponseEntity.status(HttpStatus.OK).body(testResponse)
    }

    @DeleteMapping('/test-preparations/{uuid}')
    ResponseEntity cancelTestRequest(@PathVariable('uuid') String uuid){
        tangoLoggerType = "I";
        tangoLoggerOperation = "CuratorMock.cancelTestRequest";
        tangoLoggerMessage = ("Curator: received test cancellation request");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        def testResponse = new TestResponse(status: TestPlanStatus.CANCELLING)
        ResponseEntity.status(HttpStatus.OK).body(testResponse)
    }
}
