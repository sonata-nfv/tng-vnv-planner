package com.github.tng.vnv.planner.service

import com.github.mrduguo.spring.test.AbstractSpec
import com.github.tng.vnv.planner.model.TestPlan
import com.github.tng.vnv.planner.model.TestSuite
import com.github.tng.vnv.planner.restmock.TestPlanRepositoryMock
import com.github.tng.vnv.planner.service.TestPlanService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.http.HttpStatus
import spock.lang.Ignore

class TestPlanServiceTest extends AbstractSpec {

    @Autowired
    TestPlanRepositoryMock testPlanRepositoryMock

    void 'request for 2 test plans should store in the db and consequently schedule 2 test plans hopefully'(){

        when:
        def entity = postForEntity('/tng-vnv-planner/api/v1/test-plans',
                [
                        uuid: UUID.randomUUID(),
                        test_plans:
                            [
                                    [
                                            uuid: '',
                                            nsd:
                                                    [
                                                            uuid: 'd07742ed-9429-4a07-b7af-d0b24a6d5c4c',
                                                            testing_tags:  ['http-advanced'],
                                                    ],
                                            testd:
                                                    [
                                                            uuid: 'aa5c779a-8cc7-47a9-9112-d2ff348898b4',
                                                            test_tags: ['http-advanced'],
                                                    ],
                                    ],
                                    [
                                            uuid: '',
                                            nsd:
                                                    [
                                                            uuid: 'input0ns-f213-4fae-8d3f-04358e1e1445',
                                                            testing_tags:  ['latency'],
                                                    ],
                                            testd:
                                                    [
                                                            uuid: 'fe7ec2a8-644f-4788-9aa7-bc2ff059819e',
                                                            test_tags: ['latency'],
                                                    ],
                                    ],
                            ],
                    ]
                , Void.class)

        then:
        Thread.sleep(10000L);
        entity.statusCode == HttpStatus.OK

        testPlanRepositoryMock.testPlans.size()==2

        cleanup:
        testPlanRepositoryMock.reset()

    }

}
