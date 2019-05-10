package tng.vnv.planner.model

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import io.swagger.annotations.ApiModelProperty

@ToString(includeNames = true)
@EqualsAndHashCode
class TestRequest {

    @ApiModelProperty(
            value = 'Network service uuid',
            allowEmptyValue = false)
    String nsdUuid

    @ApiModelProperty(
            value = 'Test uuid',
            allowEmptyValue = false)
    String testdUuid

    @ApiModelProperty(
            value = 'Test Plan uuid',
            allowEmptyValue = false)
    String testPlanUuid

    //@ApiModelProperty(value = 'is Last Test Plan')
    //Boolean lastTest = false

    @ApiModelProperty(
            value = 'Test plan callbacks',
            allowEmptyValue = false)

    List<Map<String, String>> testPlanCallbacks = [
            ["url": "/api/v1/test-plans/on-change/completed/", "status":"COMPLETED"],
            ["url": "/api/v1/test-plans/on-change/", "status": null]
    ]

}

