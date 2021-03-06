package com.freenow.multirabbit.example.kotlin

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [Application::class])
class ApplicationTest {

    @Autowired
    lateinit var connectionFactory: ConnectionFactory

    @Autowired
    lateinit var rabbitListenerAnnotationBeanPostProcessor: RabbitListenerAnnotationBeanPostProcessor

    @Test
    fun shouldLoadConnectionFactoryBean() {
        assertNotNull(connectionFactory)
        assertTrue(connectionFactory is SimpleRoutingConnectionFactory)
    }

    @Test
    fun shouldLoadRabbitListenerAnnotationBeanPostProcessorBean() {
        assertNotNull(rabbitListenerAnnotationBeanPostProcessor)
    }
}
