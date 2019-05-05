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

package com.github.tng.vnv.planner.model

import groovy.transform.EqualsAndHashCode
import groovy.transform.Sortable
import io.swagger.annotations.ApiModelProperty
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Transient
import javax.validation.constraints.NotNull

@Entity
@Table(name="Test_Plan")
@Sortable(includes = ['index'])
class TestPlan implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id

    @ApiModelProperty(
            value = 'Test Plan uuid',
            allowEmptyValue = false)
    String uuid
    @ApiModelProperty(
            value = 'Service Package id',
            allowEmptyValue = false)
    String servicePackageId
    @ApiModelProperty(
            value = 'Test Package id',
            allowEmptyValue = false)
    String testPackageId
    @ApiModelProperty(
            value = 'Network service uuid',
            allowEmptyValue = false)
    String serviceUuid
    @ApiModelProperty(
            value = 'Test uuid',
            allowEmptyValue = false)
    String testUuid
    @ApiModelProperty(
            value = 'Execution index',
            allowEmptyValue = false)
    int index
    @ApiModelProperty(
            value = 'Confirmed',
            allowEmptyValue = false)
    String confirmed
    @ApiModelProperty(
            value = 'Confirmation is required',
            allowEmptyValue = false)
    String confirmRequired
    @ApiModelProperty(
            value = 'Status',
            allowEmptyValue = false)
    String status
    @ApiModelProperty(
            value = 'Description',
            allowEmptyValue = true)
    String description

    boolean equals(o) {
        (o.uuid).contains(uuid)? true:false
    }

    int hashCode() {
        uuid.hashCode()
    }
}

@EqualsAndHashCode
class TestPlanRequest {
    @ApiModelProperty(
            value = 'Network service uuid',
            allowEmptyValue = false)
    String serviceUuid
    @ApiModelProperty(
            value = 'Test uuid',
            allowEmptyValue = false)
    String testUuid

    @ApiModelProperty(
            value = 'Test Plan uuid',
            allowEmptyValue = false)
    String testPlanUuid
    @ApiModelProperty(value = 'is Last Test Plan')
    Boolean lastTest = false
    List<TestPlanCallback> testPlanCallbacks = [
            new TestPlanCallback(eventActor: 'Curator', url: '/api/v1/test-plans/on-change/completed/', status:'COMPLETED'),
            new TestPlanCallback(eventActor: 'Curator', url: '/api/v1/test-plans/on-change/'),
    ]
}

@EqualsAndHashCode
class TestPlanResponse {
    @ApiModelProperty(
            value = 'Test Plan Status',
            allowEmptyValue = false,
            example = 'STARTING, COMPLETED, CANCELLING, CANCELLED, ERROR')
    String status

    @ApiModelProperty(
            value = 'Test Plan Exception message',
            allowEmptyValue = false,
            example = 'run time exception')
    String exception
}

class TestPlanCallback {

    @ApiModelProperty(
            value = 'Event Actor',
            allowEmptyValue = false,
            example = 'Curator, Executor',
            required = true)
    @NotNull
    String eventActor

    @ApiModelProperty(
            value = 'Test Plan Status',
            allowEmptyValue = false,
            example = 'STARTING, COMPLETED, CANCELLING, CANCELLED, ERROR',
            required = true)
    @NotNull
    String status

    @ApiModelProperty(
            value = 'Test Plan Exception message',
            allowEmptyValue = false,
            example = 'run time exception')
    String exception

    @ApiModelProperty(
            value = 'Callback URL',
            allowEmptyValue = false,
            example = '/test-plans/on-change')
    String url

    @ApiModelProperty(
            value = 'Test Plan Result List',
            allowEmptyValue = true)
    List<TestResult> testResults

    @ApiModelProperty(
            value = 'Test Plan uuid',
            allowEmptyValue = false,
            required = true)
    @NotNull
    String testPlanUuid
}