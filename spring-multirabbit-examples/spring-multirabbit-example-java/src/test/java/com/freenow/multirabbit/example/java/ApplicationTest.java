package com.freenow.multirabbit.example.java;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ApplicationTest {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private RabbitListenerAnnotationBeanPostProcessor rabbitListenerAnnotationBeanPostProcessor;

    @Test
    void shouldLoadConnectionFactoryBean() {
        assertNotNull(connectionFactory);
        assertTrue(connectionFactory instanceof SimpleRoutingConnectionFactory);
    }

    @Test
    void shouldLoadRabbitListenerAnnotationBeanPostProcessorBean() {
        assertNotNull(rabbitListenerAnnotationBeanPostProcessor);
    }
}
