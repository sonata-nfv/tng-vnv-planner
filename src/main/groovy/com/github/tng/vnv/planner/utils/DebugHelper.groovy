package com.github.tng.vnv.planner.utils

import groovy.util.logging.Log
import org.springframework.http.ResponseEntity

@Log
class DebugHelper {
    static def callExternalEndpoint(ResponseEntity responseEntity, def methodName, def endpoint, String message="ERROR CONNECTING WITH ENDPOINT"){
        if(responseEntity.statusCodeValue in [200, 201, 202, 203, 204, 205, 206, 207, 208, 226]) {
            log.info("##vnvlog-v.2:$methodName call_endpoint: $endpoint, status: ${responseEntity.statusCode}")
        } else {
            log.severe("##vnvlog-v.2:$methodName $message: $endpoint, status: ${responseEntity.statusCode}")
            return null
        }
        responseEntity
    }
}
