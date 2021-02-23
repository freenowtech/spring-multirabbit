package com.freenow.spring.multirabbit.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.MultiRabbitConstants;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static com.freenow.spring.multirabbit.example.ExtendedConfiguration.EXTENDED_CONNECTION_A;
import static com.freenow.spring.multirabbit.example.ExtendedConfiguration.EXTENDED_CONNECTION_B;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ApplicationTest {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private RabbitListenerAnnotationBeanPostProcessor rabbitListenerAnnotationBeanPostProcessor;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void shouldLoadConnectionFactoryBean() {
        assertNotNull(connectionFactory);
        assertTrue(connectionFactory instanceof SimpleRoutingConnectionFactory);
    }

    @Test
    public void shouldLoadRabbitListenerAnnotationBeanPostProcessorBean() {
        assertNotNull(rabbitListenerAnnotationBeanPostProcessor);
    }

    @Test
    public void shouldResolveContainerFactoryBeans() {
        List<String> beans = Arrays.asList(applicationContext
                .getBeanNamesForType(SimpleRabbitListenerContainerFactory.class));
        assertTrue(beans.containsAll(Arrays.asList("rabbitListenerContainerFactory", EXTENDED_CONNECTION_A,
                EXTENDED_CONNECTION_B)));
        beans.forEach(bean -> assertNotNull(applicationContext
                .getBean(bean, SimpleRabbitListenerContainerFactory.class)));
    }

    @Test
    public void shouldResolveRabbitAdminBeans() {
        List<String> beans = Arrays.asList(applicationContext.getBeanNamesForType(RabbitAdmin.class));
        assertTrue(beans.containsAll(Arrays.asList(
                MultiRabbitConstants.DEFAULT_RABBIT_ADMIN_BEAN_NAME,
                EXTENDED_CONNECTION_A + MultiRabbitConstants.RABBIT_ADMIN_SUFFIX,
                EXTENDED_CONNECTION_B + MultiRabbitConstants.RABBIT_ADMIN_SUFFIX)));
        beans.forEach(bean -> assertNotNull(applicationContext.getBean(bean, RabbitAdmin.class)));
    }
}
