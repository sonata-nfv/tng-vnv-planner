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

package tng.vnv.planner.controller

import tng.vnv.planner.utils.TangoLogger
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpStatusCodeException
import tng.vnv.planner.ScheduleManager
import tng.vnv.planner.model.PackageCallback
import tng.vnv.planner.model.TestPlan

import javax.validation.Valid

@Api
@RestController
@RequestMapping('/api/v1/packages')
class PackageController {

    @Autowired
    ScheduleManager scheduler

    //Tango logger
    def tangoLogger = new TangoLogger()
    String tangoLoggerType = null;
    String tangoLoggerOperation = null;
    String tangoLoggerMessage = null;
    String tangoLoggerStatus = null;

    @ApiOperation(value="Start a test via package", notes="Package uploaded notification received")
    @ApiResponses(value = [
            @ApiResponse(code = 400, message = 'Bad Request'),
            @ApiResponse(code = 404, message = 'Could not find package with that packageId'),
    ])
    @PostMapping('/on-change')
    @ResponseBody
    ResponseEntity<List<TestPlan>> onChange(@Valid @RequestBody PackageCallback body) {

        tangoLoggerType = "I";
        tangoLoggerOperation = "PackageController.onChange";
        tangoLoggerMessage = ("onChange (packageId:${body.packageId}, confirmRequired: ${body.confirmRequired})");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        def isConfirmRequired = false
        if(body.confirmRequired != null
                && (body.confirmRequired.toString().equalsIgnoreCase("true" )
                    || body.confirmRequired.equalsIgnoreCase("yes")
                    || body.confirmRequired == "1") ) {
            isConfirmRequired = true
        }

        try {
            def testPlans = scheduler.scheduleNewTestSet(body.getPackageId(), isConfirmRequired)?.testPlans
            ResponseEntity.status(HttpStatus.OK).body(testPlans) as ResponseEntity<List<TestPlan>>
        } catch (IllegalArgumentException e) {
            tangoLoggerType = "I";
            tangoLoggerOperation = "PackageController.onChange";
            tangoLoggerMessage = e.getMessage();
            tangoLoggerStatus = "200";
            tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

            ResponseEntity.status(HttpStatus.OK).body([]) as ResponseEntity<List<TestPlan>>
        } catch (HttpStatusCodeException e) {
            tangoLoggerType = "E";
            tangoLoggerOperation = "PackageController.onChange";
            tangoLoggerMessage = ("HTTPStatusCodeException, testStatus: ${e.statusCode}, message: ${e.message}");
            tangoLoggerStatus = "500";
            tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

            ResponseEntity.status(e.statusCode).body(e.message) as ResponseEntity<List<TestPlan>>
        }
    }
}
