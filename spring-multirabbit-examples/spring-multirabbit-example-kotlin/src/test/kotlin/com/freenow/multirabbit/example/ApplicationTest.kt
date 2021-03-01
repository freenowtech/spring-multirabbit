package com.freenow.multirabbit.example

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
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
