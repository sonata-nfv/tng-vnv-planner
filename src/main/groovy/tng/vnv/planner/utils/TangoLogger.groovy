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

package tng.vnv.planner.utils

import groovy.util.logging.Slf4j
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Component
import java.sql.Timestamp;

@Component
@Slf4j
class TangoLogger {

  /*
    type: Info -> I, Error -> E or Debug -> D
    operation: Description of operation
    message: Message to logger
    status: Status HTTP code, 200 OK, 500 Error
  */

  def log(String type, String operation, String message, String status){

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    String timestamps = timestamp.toString();

    message = message.replace("\"", "\\\"")

    if(type == 'E'){
      log.error("{\"type\":\"$type\",\"timestamp\":\"$timestamps\",\"start_stop\":\"\",\"component\":\"tng-vnv-planner\",\"operation\":\"$operation\",\"message\":\"$message\",\"status\":\"$status\",\"time_elapsed\":\"\"}")
    }
    else if (type == 'D'){
      log.debug("{\"type\":\"$type\",\"timestamp\":\"$timestamps\",\"start_stop\":\"\",\"component\":\"tng-vnv-planner\",\"operation\":\"$operation\",\"message\":\"$message\",\"status\":\"$status\",\"time_elapsed\":\"\"}")
    }
    else if (type == 'I'){
      log.info("{\"type\":\"$type\",\"timestamp\":\"$timestamps\",\"start_stop\":\"\",\"component\":\"tng-vnv-planner\",\"operation\":\"$operation\",\"message\":\"$message\",\"status\":\"$status\",\"time_elapsed\":\"\"}")
    }
  }
}
