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

import groovy.util.logging.Slf4j
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
class RestLogAspect {

    @Around("@annotation(RestCall) && execution(public * * (..))")
    ResponseEntity restCall(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
        ResponseEntity responseEntity;
        try {
            responseEntity = proceedingJoinPoint.proceed();
        } catch (Throwable throwable) {
            throw throwable;
        } finally {
            if(responseEntity?.statusCodeValue in [200, 201, 202, 203, 204, 205, 206, 207, 208, 226]) {
                log.info("#~#vnvlog REST_CALL: {}.{}({}), response.status: {}",
                        proceedingJoinPoint.getSignature().getDeclaringType().getSimpleName(),
                        proceedingJoinPoint.getSignature().getName(),
                        responseEntity.statusCode,
                        (proceedingJoinPoint.args.size()!=0)?proceedingJoinPoint.args[0]:' '
                )
            } else {
                log.error("#~#vnvlog REST_CALL: {}.{}, response.status: {}",
                        proceedingJoinPoint.getSignature().getDeclaringType().getSimpleName(),
                        proceedingJoinPoint.getSignature().getName(),
                        (responseEntity!=null)? responseEntity.statusCode :'NO RESPONSE')
            }
        }
        responseEntity
    }

    @AfterReturning(pointcut='@annotation(AfterRestCall) && execution(public * * (..))',returning='retVal')
    ResponseEntity afterRestCall(JoinPoint jp, Object retVal) {
        if(retVal?.statusCodeValue in [200, 201, 202, 203, 204, 205, 206, 207, 208, 226]) {
            log.info("#~#vnvlog REST_CALL2: {}.{}, response.status: {}",
                    jp.signature.declaringType.simpleName,
                    jp.signature.name,
                    retVal.statusCode,
            )
        } else {
            log.error("#~#vnvlog REST_CALL2: {}.{}, response.status: {}",
                    jp.signature.declaringType.simpleName,
                    jp.signature.name,
                    (retVal!=null)? retVal.statusCode :'NO_RESPONSE')
        }
    }
}