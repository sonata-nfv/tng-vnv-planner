package com.github.tng.vnv.planner.utils

import groovy.util.logging.Log
import org.springframework.http.ResponseEntity

@Log
class DebugHelper {
    static def callExternalEndpoint(ResponseEntity responseEntity, def methodName, def endpoint, String message="ERROR CONNECTING WITH ENDPOINT"){
        if(responseEntity?.statusCodeValue in [200, 201, 202, 203, 204, 205, 206, 207, 208, 226]) {
            log.info("##vnvlog REST_CALL: $methodName call_endpoint: $endpoint, status: ${responseEntity.statusCode}")
            responseEntity
        } else {
            log.severe("##vnvlog REST_CALL: $methodName $message: $endpoint, " +
                    "status: ${(responseEntity?.statusCode!=null)? responseEntity.statusCode :'NO RESPONSE'}"
            )
            null
        }
    }
}
