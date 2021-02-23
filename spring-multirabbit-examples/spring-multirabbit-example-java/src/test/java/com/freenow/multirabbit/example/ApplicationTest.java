package com.freenow.multirabbit.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ApplicationTest {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private RabbitListenerAnnotationBeanPostProcessor rabbitListenerAnnotationBeanPostProcessor;

    @Test
    public void shouldLoadConnectionFactoryBean() {
        assertNotNull(connectionFactory);
        assertTrue(connectionFactory instanceof SimpleRoutingConnectionFactory);
    }

    @Test
    public void shouldLoadRabbitListenerAnnotationBeanPostProcessorBean() {
        assertNotNull(rabbitListenerAnnotationBeanPostProcessor);
    }
}
