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

package tng.vnv.planner.model


import groovy.transform.EqualsAndHashCode
import io.swagger.annotations.ApiModelProperty

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name="Test_Plan")
@EqualsAndHashCode
class TestPlan extends AuditModel implements Serializable {

    @Id
    @ApiModelProperty(
            value = 'Test Plan uuid',
            allowEmptyValue = false)
    String uuid

    @ApiModelProperty(
            value = 'Test Set uuid to which it belongs',
            allowEmptyValue = false)
    String testSetUuid

    @ApiModelProperty(
            value = 'Network service uuid',
            allowEmptyValue = false)
    String serviceUuid

    @ApiModelProperty(
            value = 'Network service name',
            allowEmptyValue = false)
    String serviceName

    @ApiModelProperty(
            value = 'Test descriptor uuid',
            allowEmptyValue = false)
    String testUuid

    @ApiModelProperty(
            value = 'Test descriptor name',
            allowEmptyValue = false)
    String testName

    @ApiModelProperty(
            value = 'Test result uuid',
            allowEmptyValue = true)
    String testResultUuid

    @ApiModelProperty(
            value = 'Test Status',
            allowEmptyValue = false)
    String testStatus

    @ApiModelProperty(
            value = 'Does TestPlan require confirmation?',
            allowEmptyValue = true)
    Boolean confirmRequired = false

    @ApiModelProperty(
            value = 'Description',
            allowEmptyValue = true)
    String description

    @ApiModelProperty(
            value = 'Execution Host',
            allowEmptyValue = true)
    String executionHost

    @ApiModelProperty(
            value = 'SP Name',
            allowEmptyValue = true)
    String spName
}


