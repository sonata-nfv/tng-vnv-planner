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

package com.github.tng.vnv.planner.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import groovy.transform.EqualsAndHashCode
import groovy.transform.Sortable
import io.swagger.annotations.ApiModelProperty

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.Transient
import javax.validation.constraints.NotNull
import org.hibernate.annotations.Type

@Entity
@Table(name="Test_Plan")
@Sortable(includes = ['index'])
class TestPlan implements Serializable {
    @Id
    @GeneratedValue
    Long id

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "testSuiteId", referencedColumnName = "id", nullable = false)
    TestSuite testSuite

    String uuid
    String packageId
    String serviceUuid
    String testUuid
    int index
    String status
    String description
    @Transient
    def nsd
    @Transient
    def testd

    @JsonIgnore
    @Column(name = "nsd")
    @Type(type="org.hibernate.type.BinaryType")
    BlobOfLinkedHashMap nsdBlob

    @JsonIgnore
    @Column(name = "testd")
    @Type(type="org.hibernate.type.BinaryType")
    BlobOfLinkedHashMap testdBlob


    TestPlan blob(){
            nsdBlob = nsd
            testdBlob = testd
        this
    }
    TestPlan unBlob(){
            nsd = nsdBlob
            testd = testdBlob
        this
    }

    boolean equals(o) {
        if ((o.uuid).contains(uuid)) return true
        return false
    }

    int hashCode() {
        return uuid.hashCode()
    }
}

class BlobOfLinkedHashMap extends LinkedHashMap implements Serializable {}

@EqualsAndHashCode
class TestPlanRequest {
    def nsd
    def testd
    Boolean lastTest = false
    List<TestPlanCallback> testPlanCallbacks = [
            new TestPlanCallback(eventActor: 'Curator', url: '/test-plans/on-change/completed', status:'COMPLETED'),
            new TestPlanCallback(eventActor: 'Curator', url: '/test-plans/on-change'),
    ]
}

@EqualsAndHashCode
class TestPlanResponse {
    String uuid
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
    @NotNull
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
            value = 'Callback URL',
            allowEmptyValue = false,
            example = '/test-plans/on-change')
    String url

    List<TestResult> testResults

    @ApiModelProperty(
            value = 'Test Plan uuid',
            allowEmptyValue = false,
            required = true)
    @NotNull
    String testPlanUuid

    @ApiModelProperty(
            value = 'Test Plan Repository URI',
            allowEmptyValue = false,
            example = 'tng-cat, catalog, or xx.xx',
            required = false)
    @NotNull
    String testPlanRepository

    @ApiModelProperty(required = true)
    @NotNull
    String testResultsUuid

    @ApiModelProperty(
            value = 'Test Results Repository URI',
            allowEmptyValue = false,
            example = 'tng-res, results, or xx.xx',
            required = false)
    @NotNull
    String testResultsRepository
}
