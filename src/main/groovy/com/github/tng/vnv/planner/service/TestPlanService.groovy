package com.github.tng.vnv.planner.service

import com.github.tng.vnv.planner.model.NetworkServiceDescriptor
import com.github.tng.vnv.planner.model.TestDescriptor

interface TestPlanService {

    def findByService(NetworkServiceDescriptor nsd)

    def findByTest(TestDescriptor td)
}