package com.github.tng.vnv.planner.repository

import groovy.util.logging.Log
import org.springframework.stereotype.Repository

@Log
@Repository("TestSuiteRepository")
class TestSuiteRepositoryImpl implements TestSuiteRepository{
    def findByUuid(String uuid) {}

    def save(def testSuite) {}

    def update(def testSuite, String uuid) {}

    def deleteByUuid(String l) {}
}
