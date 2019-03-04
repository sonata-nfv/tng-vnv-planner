package com.github.tng.vnv.planner.repository

interface TestSuiteRepository {
    def findByUuid(String uuid)

    def save(def testSuite)

    def update(def testSuite, String uuid)

    def deleteByUuid(String l)
}
