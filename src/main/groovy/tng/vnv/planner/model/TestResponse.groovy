package tng.vnv.planner.model

import io.swagger.annotations.ApiModelProperty

import javax.validation.constraints.NotNull

class TestResponse {

    @ApiModelProperty(
            value = 'Test Plan status',
            allowEmptyValue = false,
            example = 'STARTING, COMPLETED, CANCELLING, CANCELLED, ERROR')
    String status

    @ApiModelProperty(
            value = 'Test Plan Exception message',
            allowEmptyValue = false,
            example = 'run time exception')
    String exception
}
