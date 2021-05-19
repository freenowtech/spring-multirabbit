package com.freenow.multirabbit.example.extension;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.MultiRabbitConstants;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.List;

import static com.freenow.multirabbit.example.extension.ExtendedConfiguration.EXTENDED_CONNECTION_A;
import static com.freenow.multirabbit.example.extension.ExtendedConfiguration.EXTENDED_CONNECTION_B;
import static com.freenow.multirabbit.example.extension.ExtendedConfiguration.EXTENDED_CONNECTION_C;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ApplicationTest {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private RabbitListenerAnnotationBeanPostProcessor rabbitListenerAnnotationBeanPostProcessor;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldLoadConnectionFactoryBean() {
        assertNotNull(connectionFactory);
        assertTrue(connectionFactory instanceof SimpleRoutingConnectionFactory);
    }

    @Test
    void shouldLoadRabbitListenerAnnotationBeanPostProcessorBean() {
        assertNotNull(rabbitListenerAnnotationBeanPostProcessor);
    }

    @Test
    void shouldResolveContainerFactoryBeans() {
        List<String> beans = Arrays.asList(applicationContext
                .getBeanNamesForType(SimpleRabbitListenerContainerFactory.class));
        assertTrue(beans.containsAll(Arrays.asList("rabbitListenerContainerFactory", EXTENDED_CONNECTION_A,
                EXTENDED_CONNECTION_B, EXTENDED_CONNECTION_C)));
        beans.forEach(bean -> assertNotNull(applicationContext
                .getBean(bean, SimpleRabbitListenerContainerFactory.class)));
    }

    @Test
    void shouldResolveRabbitAdminBeans() {
        List<String> beans = Arrays.asList(applicationContext.getBeanNamesForType(RabbitAdmin.class));
        Assertions.assertThat(beans).contains(
                MultiRabbitConstants.DEFAULT_RABBIT_ADMIN_BEAN_NAME,
                EXTENDED_CONNECTION_A + MultiRabbitConstants.RABBIT_ADMIN_SUFFIX,
                EXTENDED_CONNECTION_B + MultiRabbitConstants.RABBIT_ADMIN_SUFFIX,
                EXTENDED_CONNECTION_C + MultiRabbitConstants.RABBIT_ADMIN_SUFFIX);
        beans.forEach(bean -> assertNotNull(applicationContext.getBean(bean, RabbitAdmin.class)));
    }
}
