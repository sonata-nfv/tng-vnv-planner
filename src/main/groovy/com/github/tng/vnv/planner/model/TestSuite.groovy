package com.github.tng.vnv.planner.model

import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name="Test_Suite")
class TestSuite implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "testSuite", orphanRemoval = true, fetch= FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    List<TestPlan> testPlans = []

    String uuid
}