package com.github.tng.vnv.planner.model

import io.swagger.annotations.ApiModelProperty
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name="Test_Suite")
class TestSuite implements Serializable {
    @Id
    @GeneratedValue
    Long id

    @ApiModelProperty(
            value = 'Test plan list',
            allowEmptyValue = false,
            required = true)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "testSuite", orphanRemoval = true, fetch= FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    List<TestPlan> testPlans = []

    @ApiModelProperty(
            value = 'Test plan list uuid',
            allowEmptyValue = false,
            required = false)
    String uuid
}