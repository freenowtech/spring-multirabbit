package org.springframework.amqp.rabbit.annotation;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.amqp.MultiRabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MultiRabbitAutoConfiguration} is normally triggered before the processing of the Listeners by the
 * {@link ExtendedMultiRabbitListenerAnnotationBeanPostProcessor}. However, this does not happen whenever there is no
 * injection of {@link org.springframework.amqp.rabbit.connection.ConnectionFactory}.
 * This test makes sure to test MultiRabbit without the injection of a RabbitTemplate as a workaround for the
 * initialization.
 */
class AutoConfigInitializationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(MultiRabbitAutoConfiguration.class, RabbitAutoConfiguration.class));

    // TODO https://github.com/freenowtech/spring-multirabbit/issues/49
    @Test
    void shouldStartContextWithoutConnectionFactory() {
        this.contextRunner
                .withPropertyValues("spring.multirabbitmq.enabled=true")
                .withPropertyValues("spring.multirabbitmq.connections.broker1.port=5673")
                .withPropertyValues("spring.multirabbitmq.connections.broker2.port=5674")
                .withBean(ListenerBeans.class)
                .run((context) -> assertThat(context.getBeansOfType(RabbitAdmin.class)).hasSize(3));
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
}
