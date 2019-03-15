package com.github.tng.vnv.planner.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.amqp.core.Queue;

@Configuration
class QueueConfig {

    @Value('${app.rabbitmq.test.plan.queue}')
    String TEST_PLANS_QUEUE_NAME


    @Bean
    Queue testPlansQueue() {
        return new Queue(TEST_PLANS_QUEUE_NAME);
    }
}
