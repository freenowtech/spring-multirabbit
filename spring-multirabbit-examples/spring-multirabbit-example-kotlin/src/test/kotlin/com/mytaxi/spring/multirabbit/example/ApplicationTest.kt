package com.mytaxi.spring.multirabbit.example

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
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
