package org.springframework.boot.autoconfigure.amqp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.RoutingConnectionFactory;
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

class ExternalConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(MultiRabbitAutoConfiguration.class, RabbitAutoConfiguration.class,
                    ExternalConfig.class));

    @Test
    @DisplayName("should not find beans from external integration when disabled")
    void shouldEnsureMultiRabbitNotLoadedWhenDisabled() {
        final String broker1 = "broker1";
        final String broker2 = "broker2";
        this.contextRunner
                .withPropertyValues("spring.multirabbitmq.enabled=false")
                .withPropertyValues("spring.multirabbitmq.connections." + broker1 + ".port=5673")
                .withPropertyValues("spring.multirabbitmq.connections." + broker2 + ".port=5674")
                .run((context) -> {
                    final RabbitAdmin defaultRabbitAdmin = context.getBean(
                            MultiRabbitConstants.DEFAULT_RABBIT_ADMIN_BEAN_NAME, RabbitAdmin.class);
                    final ThrowingCallable broker1Admin = () -> context.getBean(
                            broker1.concat(MultiRabbitConstants.RABBIT_ADMIN_SUFFIX), RabbitAdmin.class);
                    final ThrowingCallable broker2Admin = () -> context.getBean(
                            broker2.concat(MultiRabbitConstants.RABBIT_ADMIN_SUFFIX), RabbitAdmin.class);
                    final ThrowingCallable externalAdmin = () -> context.getBean(
                            broker2.concat(MultiRabbitConstants.RABBIT_ADMIN_SUFFIX), RabbitAdmin.class);
                    assertThat(defaultRabbitAdmin).isNotNull();
                    assertThatThrownBy(broker1Admin).isInstanceOf(NoSuchBeanDefinitionException.class);
                    assertThatThrownBy(broker2Admin).isInstanceOf(NoSuchBeanDefinitionException.class);
                    assertThatThrownBy(externalAdmin).isInstanceOf(NoSuchBeanDefinitionException.class);

                    final RabbitListenerContainerFactory defaultContainerFactory = context.getBean(
                            MultiRabbitConstants.DEFAULT_CONTAINER_FACTORY_BEAN_NAME,
                            RabbitListenerContainerFactory.class);
                    final ThrowingCallable broker1ContainerFactory = () -> context.getBean(broker1,
                            RabbitListenerContainerFactory.class);
                    final ThrowingCallable broker2ContainerFactory = () -> context.getBean(broker2,
                            RabbitListenerContainerFactory.class);
                    assertThat(defaultContainerFactory).isNotNull();
                    final ThrowingCallable externalContainerFactory = () -> context.getBean(broker2,
                            RabbitListenerContainerFactory.class);
                    assertThatThrownBy(broker1ContainerFactory).isInstanceOf(NoSuchBeanDefinitionException.class);
                    assertThatThrownBy(broker2ContainerFactory).isInstanceOf(NoSuchBeanDefinitionException.class);
                    assertThatThrownBy(externalContainerFactory).isInstanceOf(NoSuchBeanDefinitionException.class);

                    final ConnectionFactory connectionFactory = context.getBean(ConnectionFactory.class);
                    assertThat(connectionFactory).isNotInstanceOf(RoutingConnectionFactory.class);
                });
    }

    @Test
    @DisplayName("should ensure beans from external integration")
    void shouldEnsureBeansFromExternalIntegration() {
        final String broker1 = "broker1";
        final String broker2 = "broker2";
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
                    final RabbitAdmin externalAdmin = context.getBean(
                            ExternalConfig.CONNECTION_KEY.concat(MultiRabbitConstants.RABBIT_ADMIN_SUFFIX),
                            RabbitAdmin.class);
                    assertThat(defaultRabbitAdmin).isNotNull();
                    assertThat(broker1Admin).isNotNull();
                    assertThat(broker2Admin).isNotNull();
                    assertThat(externalAdmin).isSameAs(ExternalConfig.RABBIT_ADMIN);

                    final RabbitListenerContainerFactory defaultContainerFactory = context.getBean(
                            MultiRabbitConstants.DEFAULT_CONTAINER_FACTORY_BEAN_NAME,
                            RabbitListenerContainerFactory.class);
                    final RabbitListenerContainerFactory broker1ContainerFactory = context.getBean(broker1,
                            RabbitListenerContainerFactory.class);
                    final RabbitListenerContainerFactory broker2ContainerFactory = context.getBean(broker2,
                            RabbitListenerContainerFactory.class);
                    final RabbitListenerContainerFactory externalContainerFactory = context.getBean(
                            ExternalConfig.CONNECTION_KEY, RabbitListenerContainerFactory.class);
                    assertThat(defaultContainerFactory).isNotNull();
                    assertThat(broker1ContainerFactory).isNotNull();
                    assertThat(broker2ContainerFactory).isNotNull();
                    assertThat(externalContainerFactory).isSameAs(ExternalConfig.CONTAINER_FACTORY);

                    final ConnectionFactory connectionFactory = context.getBean(ConnectionFactory.class);
                    assertThat(connectionFactory).isInstanceOf(RoutingConnectionFactory.class);
                    final SimpleRoutingConnectionFactory routingConnectionFactory
                            = (SimpleRoutingConnectionFactory) connectionFactory;
                    final ConnectionFactory connectionFactory1 = routingConnectionFactory
                            .getTargetConnectionFactory(broker1);
                    final ConnectionFactory connectionFactory2 = routingConnectionFactory
                            .getTargetConnectionFactory(broker2);
                    final ConnectionFactory externalConnectionFactory = routingConnectionFactory
                            .getTargetConnectionFactory(ExternalConfig.CONNECTION_KEY);
                    assertThat(connectionFactory1).isNotNull();
                    assertThat(connectionFactory2).isNotNull();
                    assertThat(externalConnectionFactory).isSameAs(ExternalConfig.CONNECTION_FACTORY);
                });
    }

    @Configuration
    @Import(MultiRabbitAutoConfiguration.class)
    static class ExternalConfig {

        private static final String CONNECTION_KEY = "externalConnectionKey";
        private static final ConnectionFactory CONNECTION_FACTORY = mock(ConnectionFactory.class);
        private static final SimpleRabbitListenerContainerFactory CONTAINER_FACTORY
                = mock(SimpleRabbitListenerContainerFactory.class);
        private static final RabbitAdmin RABBIT_ADMIN = mock(RabbitAdmin.class);
        private static final ConnectionFactory DEFAULT_CONNECTION_FACTORY = mock(ConnectionFactory.class);

        @Bean
        @ConditionalOnClass(MultiRabbitConnectionFactoryWrapper.class)
        static MultiRabbitConnectionFactoryWrapper externalWrapper() {
            MultiRabbitConnectionFactoryWrapper wrapper = new MultiRabbitConnectionFactoryWrapper();
            wrapper.addConnectionFactory(CONNECTION_KEY, CONNECTION_FACTORY, CONTAINER_FACTORY, RABBIT_ADMIN);
            wrapper.setDefaultConnectionFactory(DEFAULT_CONNECTION_FACTORY);
            return wrapper;
        }
    }
}
