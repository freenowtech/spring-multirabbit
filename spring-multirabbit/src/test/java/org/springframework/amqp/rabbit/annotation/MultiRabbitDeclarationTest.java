package org.springframework.amqp.rabbit.annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.autoconfigure.amqp.MultiRabbitConstants.DEFAULT_RABBIT_ADMIN_BEAN_NAME;
import static org.springframework.boot.autoconfigure.amqp.MultiRabbitConstants.RABBIT_ADMIN_SUFFIX;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.amqp.MultiRabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.RabbitMQContainer;

class MultiRabbitDeclarationTest {

    private final RabbitMQContainer defaultRabbit = new RabbitMQContainer("rabbitmq:3-management");
    private final RabbitMQContainer rabbit1 = new RabbitMQContainer("rabbitmq:3-management");
    private final RabbitMQContainer rabbit2 = new RabbitMQContainer("rabbitmq:3-management");

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(MultiRabbitAutoConfiguration.class, RabbitAutoConfiguration.class));

    @BeforeEach
    void beforeEach() {
        this.defaultRabbit.start();
        this.rabbit1.start();
        this.rabbit2.start();
    }

    @AfterEach
    void afterEach() {
        this.defaultRabbit.stop();
        this.rabbit1.stop();
        this.rabbit2.stop();
    }

    @Test
    @DisplayName("should ensure declared queues in RabbitMQ")
    void shouldEnsureDeclaredQueues() {
        final Integer defaultRabbitPort = defaultRabbit.getMappedPort(5672);
        final Integer rabbit1Port = rabbit1.getMappedPort(5672);
        final Integer rabbit2Port = rabbit2.getMappedPort(5672);

        final String broker1 = ThreeListenersBeans.BROKER_NAME_1;
        final String broker2 = ThreeListenersBeans.BROKER_NAME_2;
        this.contextRunner
                .withPropertyValues("spring.rabbitmq.port=" + defaultRabbitPort)
                .withPropertyValues("spring.multirabbitmq.enabled=true")
                .withPropertyValues("spring.multirabbitmq.connections." + broker1 + ".port=" + rabbit1Port)
                .withPropertyValues("spring.multirabbitmq.connections." + broker2 + ".port=" + rabbit2Port)
                .withBean(ThreeListenersBeans.class)
                .run((context) -> {
                    final RabbitAdmin amqpAdmin = context.getBean(DEFAULT_RABBIT_ADMIN_BEAN_NAME, RabbitAdmin.class);
                    assertThat(amqpAdmin.getQueueInfo(ThreeListenersBeans.QUEUE_0)).isNotNull();
                    assertThat(amqpAdmin.getQueueInfo(ThreeListenersBeans.QUEUE_1)).isNull();
                    assertThat(amqpAdmin.getQueueInfo(ThreeListenersBeans.QUEUE_2)).isNull();

                    final RabbitAdmin rabbitAdmin1 = context.getBean(
                            ThreeListenersBeans.BROKER_NAME_1 + RABBIT_ADMIN_SUFFIX, RabbitAdmin.class);
                    assertThat(rabbitAdmin1.getQueueInfo(ThreeListenersBeans.QUEUE_0)).isNull();
                    assertThat(rabbitAdmin1.getQueueInfo(ThreeListenersBeans.QUEUE_1)).isNotNull();
                    assertThat(rabbitAdmin1.getQueueInfo(ThreeListenersBeans.QUEUE_2)).isNull();

                    final RabbitAdmin rabbitAdmin2 = context.getBean(
                            ThreeListenersBeans.BROKER_NAME_2 + RABBIT_ADMIN_SUFFIX, RabbitAdmin.class);
                    assertThat(rabbitAdmin2.getQueueInfo(ThreeListenersBeans.QUEUE_0)).isNull();
                    assertThat(rabbitAdmin2.getQueueInfo(ThreeListenersBeans.QUEUE_1)).isNull();
                    assertThat(rabbitAdmin2.getQueueInfo(ThreeListenersBeans.QUEUE_2)).isNotNull();
                });
    }

    @Component
    @EnableRabbit
    private static class ThreeListenersBeans {

        public static final String EXCHANGE_0 = "exchange0";

        public static final String ROUTING_KEY_0 = "routingKey0";

        public static final String QUEUE_0 = "queue0";

        public static final String BROKER_NAME_1 = "broker1";

        public static final String EXCHANGE_1 = "exchange1";

        public static final String ROUTING_KEY_1 = "routingKey1";

        public static final String QUEUE_1 = "queue1";

        public static final String BROKER_NAME_2 = "broker2";

        public static final String EXCHANGE_2 = "exchange2";

        public static final String ROUTING_KEY_2 = "routingKey2";

        public static final String QUEUE_2 = "queue2";

        @RabbitListener(bindings = @QueueBinding(exchange = @Exchange(EXCHANGE_0), value = @Queue(QUEUE_0),
                key = ROUTING_KEY_0))
        void listenBroker0(final String message) {
        }

        @RabbitListener(containerFactory = BROKER_NAME_1, bindings = @QueueBinding(exchange = @Exchange(EXCHANGE_1),
                value = @Queue(QUEUE_1), key = ROUTING_KEY_1))
        void listenBroker1(final String message) {
        }

        @RabbitListener(containerFactory = BROKER_NAME_2, bindings = @QueueBinding(exchange = @Exchange(EXCHANGE_2),
                value = @Queue(QUEUE_2), key = ROUTING_KEY_2))
        void listenBroker2(final String message) {
        }
    }
}
