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

package com.github.tng.vnv.planner.aspect

import com.github.tng.vnv.planner.model.TestPlan
import groovy.util.logging.Slf4j
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Aspect
@Component
@Slf4j
class ServiceCallAspect {

    @Around("@annotation(ServiceCall) && execution(public * * (..))")
    def restCall(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        def out
        if(proceedingJoinPoint.args.size() == 2)
            log.debug("#~#vnvlog SERVICE_CALL_STR {}.{}(uuid:{}, status:{})",
                    proceedingJoinPoint.signature.declaringType.simpleName,
                    proceedingJoinPoint.signature.name,
                    proceedingJoinPoint.args[0],proceedingJoinPoint.args[1])
        else if (proceedingJoinPoint.args.size() == 1 && proceedingJoinPoint.args[0] instanceof TestPlan)
            log.debug("#~#vnvlog SERVICE_CALL_STR {}.{} TestPlan(uuid:{})",
                    proceedingJoinPoint.signature.declaringType.simpleName,
                    proceedingJoinPoint.signature.name,
                    proceedingJoinPoint.args[0].uuid)
        else if (proceedingJoinPoint.args.size() == 1 && proceedingJoinPoint.args[0] instanceof TestPlan)
            log.debug("#~#vnvlog SERVICE_CALL_STR {}.{}(uuid:{})",
                    proceedingJoinPoint.signature.declaringType.simpleName,
                    proceedingJoinPoint.signature.name,
                    proceedingJoinPoint.args[0])
        else
            log.debug("#~#vnvlog SERVICE_CALL_STR {}.{}()",
                    proceedingJoinPoint.signature.declaringType.simpleName,
                    proceedingJoinPoint.signature.name)

        try {
            out = proceedingJoinPoint.proceed();
        } catch (Throwable throwable) {
            throw throwable;
        } finally {
            if(proceedingJoinPoint.args.size() == 2)
                log.debug("#~#vnvlog SERVICE_CALL_END {}.{}(uuid:{}, status:{})",
                        proceedingJoinPoint.signature.declaringType.simpleName,
                        proceedingJoinPoint.signature.name,
                        proceedingJoinPoint.args[0],proceedingJoinPoint.args[1])
            else if (proceedingJoinPoint.args.size() == 1 && proceedingJoinPoint.args[0] instanceof TestPlan)
                log.debug("#~#vnvlog SERVICE_CALL_END {}.{} TestPlan(uuid:{})",
                        proceedingJoinPoint.signature.declaringType.simpleName,
                        proceedingJoinPoint.signature.name,
                        proceedingJoinPoint.args[0].uuid)
            else if (proceedingJoinPoint.args.size() == 1 && proceedingJoinPoint.args[0] instanceof TestPlan)
                log.debug("#~#vnvlog SERVICE_CALL_END {}.{}(uuid:{})",
                        proceedingJoinPoint.signature.declaringType.simpleName,
                        proceedingJoinPoint.signature.name,
                        proceedingJoinPoint.args[0])
            else
                log.debug("#~#vnvlog SERVICE_CALL_END {}.{}()",
                        proceedingJoinPoint.signature.declaringType.simpleName,
                        proceedingJoinPoint.signature.name)
        }
        out
    }

}