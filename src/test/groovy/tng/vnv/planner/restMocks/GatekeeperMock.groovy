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
import org.springframework.web.bind.annotation.*
import tng.vnv.planner.config.GeneralConfig

@RestController
@RequestMapping('/catalogues')
class GatekeeperMock {

    static def mapper

    //Tango logger
    def tangoLogger = new TangoLogger()
    String tangoLoggerType = null;
    String tangoLoggerOperation = null;
    String tangoLoggerMessage = null;
    String tangoLoggerStatus = null;

    @GetMapping('/packages')
    ResponseEntity getPackageByFilter(@RequestParam(value = "package_content.uuid", required = false) String uuid, @RequestParam(value = "package_content.testing_tags", required = false) String tag){

        def body

        mapper = new GeneralConfig().objectMapper()

        if (uuid != null){
            tangoLoggerType = "I";
            tangoLoggerOperation = "GatekeeperMock.getPackageByFilter";
            tangoLoggerMessage = ("GatekeeperMock: received get package by Id=${uuid.toString()} request");
            tangoLoggerStatus = "200";
            tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

            if (uuid == '57cebe79-96aa-4f41-af80-93050bfddd9f'){
                body = getClass().getResource('/servicePackage.json').text
            } else {
                body = getClass().getResource('/testPackage.json').text
            }
            body = "[${body}]"
        } else if(tag != null) {
            tangoLoggerType = "I";
            tangoLoggerOperation = "GatekeeperMock.getPackageByFilter";
            tangoLoggerMessage = ("GatekeeperMock: received get package by Tag=${tag} request");
            tangoLoggerStatus = "200";
            tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

            def pks = getClass().getResource('/servicePackage.json').text
            def pkt = getClass().getResource('/testPackage.json').text

            body = "[ ${pks}, ${pkt}]"
        } else {

        }
        body = mapper.readValue(body, Object.class)
        ResponseEntity.status(HttpStatus.OK).body(body)
    }

    @GetMapping('/packages/{packageId}')
    ResponseEntity getPackageById(@PathVariable String packageId){
        def body

        mapper = new GeneralConfig().objectMapper()

        tangoLoggerType = "I";
        tangoLoggerOperation = "GatekeeperMock.getPackageById";
        tangoLoggerMessage = ("GatekeeperMock: received get package by Id=${packageId.toString()} request");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        if (packageId == '0e802097-29de-4628-ac5b-2f55e9d781e8'){
            body = getClass().getResource('/servicePackage.json').text
        } else {
            body = getClass().getResource('/testPackage.json').text
        }

        body = mapper.readValue(body, Object.class)

        ResponseEntity.status(HttpStatus.OK).body(body)
    }

    @GetMapping('/network-services/{uuid}')
    ResponseEntity getServiceByUuid(@PathVariable String uuid){
        def body

        mapper = new GeneralConfig().objectMapper()

        tangoLoggerType = "I";
        tangoLoggerOperation = "GatekeeperMock.getServiceByUuid";
        tangoLoggerMessage = ("GatekeeperMock: received get service by Id=${uuid}");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        body = mapper.readValue(getClass().getResource('/service.json').text, Object.class)
        ResponseEntity.status(HttpStatus.OK).body(body)
    }

    @GetMapping('/tests/{uuid}')
    ResponseEntity getTestByUuid(@PathVariable String uuid){
        def body

        mapper = new GeneralConfig().objectMapper()

        tangoLoggerType = "I";
        tangoLoggerOperation = "GatekeeperMock.getTestByUuid";
        tangoLoggerMessage = ("GatekeeperMock: received get test by Id=${uuid}");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        body = mapper.readValue(getClass().getResource('/test.json').text, Object.class)
        ResponseEntity.status(HttpStatus.OK).body(body)
    }
}
