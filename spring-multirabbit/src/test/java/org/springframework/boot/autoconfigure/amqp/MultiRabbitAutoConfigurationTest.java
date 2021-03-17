package org.springframework.boot.autoconfigure.amqp;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.ExtendedMultiRabbitListenerAnnotationBeanPostProcessor;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.RabbitListenerConfigUtils;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.RoutingConnectionFactory;
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.amqp.MultiRabbitAutoConfiguration.MultiRabbitConnectionFactoryCreator;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link MultiRabbitAutoConfiguration}.
 *
 * @author Wander Costa
 */
class MultiRabbitAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(MultiRabbitAutoConfiguration.class, RabbitAutoConfiguration.class));

    @Test
    @DisplayName("should not find MultiRabbit beans when disabled")
    void shouldEnsureMultiRabbitNotLoadedWhenDisabled() {
        final String broker1 = ThreeListenersBeans.BROKER_NAME_1;
        final String broker2 = ThreeListenersBeans.BROKER_NAME_2;
        this.contextRunner
                .withPropertyValues("spring.multirabbitmq.enabled=false")
                .withPropertyValues("spring.multirabbitmq.connections.broker1.port=5673")
                .withPropertyValues("spring.multirabbitmq.connections.broker2.port=5674")
                .run((context) -> {
                    final RabbitAdmin defaultRabbitAdmin = context.getBean(
                            MultiRabbitConstants.DEFAULT_RABBIT_ADMIN_BEAN_NAME, RabbitAdmin.class);
                    final ThrowingCallable broker1Admin = () -> context.getBean(
                            broker1.concat(MultiRabbitConstants.RABBIT_ADMIN_SUFFIX), RabbitAdmin.class);
                    final ThrowingCallable broker2Admin = () -> context.getBean(
                            broker2.concat(MultiRabbitConstants.RABBIT_ADMIN_SUFFIX), RabbitAdmin.class);
                    assertThat(defaultRabbitAdmin).isNotNull();
                    assertThatThrownBy(broker1Admin).isInstanceOf(NoSuchBeanDefinitionException.class);
                    assertThatThrownBy(broker2Admin).isInstanceOf(NoSuchBeanDefinitionException.class);

                    final RabbitListenerContainerFactory defaultContainerFactory = context.getBean(
                            MultiRabbitConstants.DEFAULT_CONTAINER_FACTORY_BEAN_NAME,
                            RabbitListenerContainerFactory.class);
                    final ThrowingCallable broker1ContainerFactory = () -> context.getBean(broker1,
                            RabbitListenerContainerFactory.class);
                    final ThrowingCallable broker2ContainerFactory = () -> context.getBean(broker2,
                            RabbitListenerContainerFactory.class);
                    assertThat(defaultContainerFactory).isNotNull();
                    assertThatThrownBy(broker1ContainerFactory).isInstanceOf(NoSuchBeanDefinitionException.class);
                    assertThatThrownBy(broker2ContainerFactory).isInstanceOf(NoSuchBeanDefinitionException.class);

                    final ConnectionFactory connectionFactory = context.getBean(ConnectionFactory.class);
                    assertThat(connectionFactory).isNotInstanceOf(RoutingConnectionFactory.class);

                    final Object annotationBPP = context
                            .getBean(RabbitListenerConfigUtils.RABBIT_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME);
                    assertThat(annotationBPP)
                            .isNotInstanceOf(ExtendedMultiRabbitListenerAnnotationBeanPostProcessor.class);

                    final ThrowingCallable connectionFactoryCreator = () -> context
                            .getBean(MultiRabbitConstants.CONNECTION_FACTORY_CREATOR_BEAN_NAME,
                                    RabbitAutoConfiguration.RabbitConnectionFactoryCreator.class);
                    assertThatThrownBy(connectionFactoryCreator).isInstanceOf(NoSuchBeanDefinitionException.class);

                    final ThrowingCallable multiRabbitConnectionFactoryCreator = () -> context
                            .getBean(MultiRabbitConnectionFactoryCreator.class);
                    assertThatThrownBy(multiRabbitConnectionFactoryCreator)
                            .isInstanceOf(NoSuchBeanDefinitionException.class);

                    final ThrowingCallable contextWrapper = () -> context
                            .getBean(MultiRabbitConnectionFactoryWrapper.class);
                    assertThatThrownBy(contextWrapper).isInstanceOf(NoSuchBeanDefinitionException.class);
                });
    }

    @Test
    @DisplayName("should ensure MultiRabbit beans from AutoConfiguration")
    void shouldEnsureAutoConfigurationBeans() {
        final String broker1 = ThreeListenersBeans.BROKER_NAME_1;
        final String broker2 = ThreeListenersBeans.BROKER_NAME_2;
        this.contextRunner
                .withPropertyValues("spring.multirabbitmq.enabled=true")
                .withPropertyValues("spring.multirabbitmq.connections." + broker1 + ".port=5673")
                .withPropertyValues("spring.multirabbitmq.connections." + broker2 + ".port=5674")
                .run((context) -> {
                    final RabbitAdmin defaultRabbitAdmin = context.getBean(
                            MultiRabbitConstants.DEFAULT_RABBIT_ADMIN_BEAN_NAME, RabbitAdmin.class);
                    final RabbitAdmin broker1Admin = context.getBean(
                            broker1.concat(MultiRabbitConstants.RABBIT_ADMIN_SUFFIX), RabbitAdmin.class);
                    final RabbitAdmin broker2Admin = context.getBean(
                            broker2.concat(MultiRabbitConstants.RABBIT_ADMIN_SUFFIX), RabbitAdmin.class);
                    assertThat(defaultRabbitAdmin).isNotNull();
                    assertThat(broker1Admin).isNotNull();
                    assertThat(broker2Admin).isNotNull();

                    final RabbitListenerContainerFactory defaultContainerFactory = context.getBean(
                            MultiRabbitConstants.DEFAULT_CONTAINER_FACTORY_BEAN_NAME,
                            RabbitListenerContainerFactory.class);
                    final RabbitListenerContainerFactory broker1ContainerFactory = context.getBean(broker1,
                            RabbitListenerContainerFactory.class);
                    final RabbitListenerContainerFactory broker2ContainerFactory = context.getBean(broker2,
                            RabbitListenerContainerFactory.class);
                    assertThat(defaultContainerFactory).isNotNull();
                    assertThat(broker1ContainerFactory).isNotNull();
                    assertThat(broker2ContainerFactory).isNotNull();

                    final ConnectionFactory connectionFactory = context.getBean(ConnectionFactory.class);
                    assertThat(connectionFactory).isInstanceOf(RoutingConnectionFactory.class);
                    final SimpleRoutingConnectionFactory routingConnectionFactory
                            = (SimpleRoutingConnectionFactory) connectionFactory;
                    final ConnectionFactory connectionFactory1 = routingConnectionFactory
                            .getTargetConnectionFactory(broker1);
                    final ConnectionFactory connectionFactory2 = routingConnectionFactory
                            .getTargetConnectionFactory(broker2);
                    assertThat(routingConnectionFactory.getPort()).isEqualTo(5672);
                    assertThat(connectionFactory1).isNotNull();
                    assertThat(connectionFactory1.getPort()).isEqualTo(5673);
                    assertThat(connectionFactory2).isNotNull();
                    assertThat(connectionFactory2.getPort()).isEqualTo(5674);

                    final RabbitAutoConfiguration.RabbitConnectionFactoryCreator connectionFactoryCreator = context
                            .getBean(MultiRabbitConstants.CONNECTION_FACTORY_CREATOR_BEAN_NAME,
                                    RabbitAutoConfiguration.RabbitConnectionFactoryCreator.class);
                    assertThat(connectionFactoryCreator).isNotNull();

                    final MultiRabbitConnectionFactoryCreator multiRabbitConnectionFactoryCreator = context
                            .getBean(MultiRabbitConnectionFactoryCreator.class);
                    assertThat(multiRabbitConnectionFactoryCreator).isNotNull();

                    final MultiRabbitConnectionFactoryWrapper contextWrapper = context
                            .getBean(MultiRabbitConnectionFactoryWrapper.class);
                    assertThat(contextWrapper).isNotNull();
                });
    }

    // TODO https://github.com/freenowtech/spring-multirabbit/issues/49
    @Test
    @DisplayName("should ensure MultiRabbit AnnotationBeanPostProcessor")
    void shouldEnsureBPP() {
        final String broker1 = ThreeListenersBeans.BROKER_NAME_1;
        final String broker2 = ThreeListenersBeans.BROKER_NAME_2;
        this.contextRunner
                .withPropertyValues("spring.multirabbitmq.enabled=true")
                .withPropertyValues("spring.multirabbitmq.connections." + broker1 + ".port=5673")
                .withPropertyValues("spring.multirabbitmq.connections." + broker2 + ".port=5674")
                .withBean(ThreeListenersBeans.class)
                .run((context) -> {
                    final Object annotationBPP = context
                            .getBean(RabbitListenerConfigUtils.RABBIT_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME);
                    assertThat(annotationBPP)
                            .isInstanceOf(ExtendedMultiRabbitListenerAnnotationBeanPostProcessor.class);
                });
    }

    // TODO https://github.com/freenowtech/spring-multirabbit/issues/49
    @Test
    @DisplayName("should ensure MultiRabbit declarables from Listeners")
    void shouldEnsureListenersDeclarables() {
        final String broker1 = ThreeListenersBeans.BROKER_NAME_1;
        final String broker2 = ThreeListenersBeans.BROKER_NAME_2;
        this.contextRunner
                .withPropertyValues("spring.multirabbitmq.enabled=true")
                .withPropertyValues("spring.multirabbitmq.connections." + broker1 + ".port=5673")
                .withPropertyValues("spring.multirabbitmq.connections." + broker2 + ".port=5674")
                .withBean(ThreeListenersBeans.class)
                .run((context) -> {
                    assertQueues(context);
                    assertBindings(context);
                    assertExchanges(context);
                });
    }

    @Test
    @DisplayName("should fail to initialize listeners when MultiRabbit is disabled")
    void shouldFailToInitializeListenersWhenDisabled() {
        final ThrowingCallable callable = () -> new AnnotationConfigApplicationContext(ThreeListenersBeans.class,
                RabbitAutoConfiguration.class, MultiRabbitAutoConfiguration.class);
        assertThatThrownBy(callable).isInstanceOf(BeanCreationException.class);
    }

    private void assertExchanges(final AssertableApplicationContext context) {
        final Map<String, DirectExchange> exchanges = mapToExchangeName(context
                .getBeansOfType(DirectExchange.class).values());
        assertThat(exchanges.keySet()).containsExactlyInAnyOrder(
                ThreeListenersBeans.EXCHANGE_0,
                ThreeListenersBeans.EXCHANGE_1,
                ThreeListenersBeans.EXCHANGE_2);
        assertAdmin(exchanges.get(ThreeListenersBeans.EXCHANGE_0),
                MultiRabbitConstants.DEFAULT_RABBIT_ADMIN_BEAN_NAME);
        assertAdmin(exchanges.get(ThreeListenersBeans.EXCHANGE_1),
                ThreeListenersBeans.BROKER_NAME_1.concat(MultiRabbitConstants.RABBIT_ADMIN_SUFFIX));
        assertAdmin(exchanges.get(ThreeListenersBeans.EXCHANGE_2),
                ThreeListenersBeans.BROKER_NAME_2.concat(MultiRabbitConstants.RABBIT_ADMIN_SUFFIX));
    }

    private void assertBindings(final AssertableApplicationContext context) {
        final Map<String, Binding> bindings = mapToRoutingKey(context.getBeansOfType(Binding.class).values());
        assertThat(bindings.keySet()).containsExactlyInAnyOrder(
                ThreeListenersBeans.ROUTING_KEY_0,
                ThreeListenersBeans.ROUTING_KEY_1,
                ThreeListenersBeans.ROUTING_KEY_2);
        assertAdmin(bindings.get(ThreeListenersBeans.ROUTING_KEY_0),
                MultiRabbitConstants.DEFAULT_RABBIT_ADMIN_BEAN_NAME);
        assertAdmin(bindings.get(ThreeListenersBeans.ROUTING_KEY_1),
                ThreeListenersBeans.BROKER_NAME_1.concat(MultiRabbitConstants.RABBIT_ADMIN_SUFFIX));
        assertAdmin(bindings.get(ThreeListenersBeans.ROUTING_KEY_2),
                ThreeListenersBeans.BROKER_NAME_2.concat(MultiRabbitConstants.RABBIT_ADMIN_SUFFIX));
    }

    private void assertQueues(final AssertableApplicationContext context) {
        final Map<String, org.springframework.amqp.core.Queue> queues = mapToQueueName(context
                .getBeansOfType(org.springframework.amqp.core.Queue.class).values());
        assertThat(queues.keySet()).containsExactlyInAnyOrder(
                ThreeListenersBeans.QUEUE_0,
                ThreeListenersBeans.QUEUE_1,
                ThreeListenersBeans.QUEUE_2);
        assertAdmin(queues.get(ThreeListenersBeans.QUEUE_0),
                MultiRabbitConstants.DEFAULT_RABBIT_ADMIN_BEAN_NAME);
        assertAdmin(queues.get(ThreeListenersBeans.QUEUE_1),
                ThreeListenersBeans.BROKER_NAME_1.concat(MultiRabbitConstants.RABBIT_ADMIN_SUFFIX));
        assertAdmin(queues.get(ThreeListenersBeans.QUEUE_2),
                ThreeListenersBeans.BROKER_NAME_2.concat(MultiRabbitConstants.RABBIT_ADMIN_SUFFIX));
    }

    private void assertAdmin(final Declarable declarable, final String admin) {
        assertThat((Collection<String>) declarable.getDeclaringAdmins()).contains(admin);
    }

    private Map<String, DirectExchange> mapToExchangeName(final Collection<DirectExchange> exchanges) {
        return exchanges.stream().collect(Collectors.toMap(AbstractExchange::getName, v -> v));
    }

    private Map<String, org.springframework.amqp.core.Queue> mapToQueueName(
            final Collection<org.springframework.amqp.core.Queue> queues) {
        return queues.stream().collect(Collectors.toMap(org.springframework.amqp.core.Queue::getName, v -> v));

    }

    private Map<String, Binding> mapToRoutingKey(final Collection<Binding> bindings) {
        return bindings.stream().collect(Collectors.toMap(Binding::getRoutingKey, v -> v));
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
