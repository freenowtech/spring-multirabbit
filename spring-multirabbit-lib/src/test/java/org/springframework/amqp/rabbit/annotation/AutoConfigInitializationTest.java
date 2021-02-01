package org.springframework.amqp.rabbit.annotation;

import org.junit.Test;
import org.springframework.boot.autoconfigure.amqp.MultiRabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * {@link MultiRabbitAutoConfiguration} is normally triggered before the processing of the Listeners by the
 * {@link MultiRabbitListenerAnnotationBeanPostProcessor}. However, this does not happen whenever there is no injection
 * of {@link org.springframework.amqp.rabbit.connection.ConnectionFactory}.
 * This test makes sure to test MultiRabbit without the injection of a RabbitTemplate as a workaround for the
 * initialization.
 */
public class AutoConfigInitializationTest {

    @Test
    public void shouldStartContextWithoutConnectionFactory() {
        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
                ThreeBrokersConfig.class, RabbitAutoConfiguration.class, MultiRabbitAutoConfiguration.class,
                ListenerBeans.class);
        ctx.close(); // Close and stop the listeners
    }

    @Component
    @EnableRabbit
    private static class ListenerBeans {

        @RabbitListener(bindings = @QueueBinding(
                exchange = @Exchange("exchange0"),
                value = @Queue(),
                key = "routingKey0"))
        void listenBroker0(final String message) {
        }

        @RabbitListener(containerFactory = "broker1", bindings = @QueueBinding(
                exchange = @Exchange("exchange1"),
                value = @Queue(),
                key = "routingKey1"))
        void listenBroker1(final String message) {
        }

        @RabbitListener(containerFactory = "broker2", bindings = @QueueBinding(
                exchange = @Exchange("exchange2"),
                value = @Queue(),
                key = "routingKey2"))
        void listenBroker2(final String message) {
        }
    }

    /**
     * Configuration to provide 3 brokers.
     */
    @Configuration
    @PropertySource("classpath:application-three-brokers.properties")
    public static class ThreeBrokersConfig {
    }
}
