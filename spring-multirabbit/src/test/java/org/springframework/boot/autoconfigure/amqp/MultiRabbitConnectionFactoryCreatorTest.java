package org.springframework.boot.autoconfigure.amqp;

import com.rabbitmq.client.impl.CredentialsProvider;
import com.rabbitmq.client.impl.CredentialsRefreshService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionNameStrategy;
import org.springframework.amqp.rabbit.connection.RoutingConnectionFactory;
import org.springframework.amqp.rabbit.connection.SimpleResourceHolder;
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiRabbitConnectionFactoryCreatorTest {

    private static final String DUMMY_KEY = "dummy-key";

    @Mock
    private ConfigurableListableBeanFactory beanFactory;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ObjectProvider<ConnectionNameStrategy> connectionNameStrategy;

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private ObjectProvider<CredentialsProvider> credentialsProvider;

    @Mock
    private ObjectProvider<CredentialsRefreshService> credentialsRefreshService;

    @Mock
    private ConnectionFactory connectionFactory0;

    @Mock
    private ConnectionFactory connectionFactory1;

    @Mock
    private SimpleRabbitListenerContainerFactory containerFactory;

    @Mock
    private RabbitAdmin rabbitAdmin;

    @Mock
    private RabbitProperties rabbitProperties;

    @Mock
    private RabbitProperties secondaryRabbitProperties;

    @Mock
    private MultiRabbitProperties multiRabbitProperties;

    @Mock
    private RabbitAutoConfiguration.RabbitConnectionFactoryCreator springFactoryCreator;

    private MultiRabbitAutoConfiguration.MultiRabbitConnectionFactoryCreator creator() {
        final MultiRabbitAutoConfiguration.MultiRabbitConnectionFactoryCreator config
                = new MultiRabbitAutoConfiguration.MultiRabbitConnectionFactoryCreator(springFactoryCreator);
        config.setBeanFactory(beanFactory);
        config.setApplicationContext(applicationContext);
        return config;
    }

    @Test
    void shouldInstantiateExternalEmptyWrapper() {
        final MultiRabbitConnectionFactoryWrapper emptyWrapper = creator().externalEmptyWrapper();
        assertTrue(emptyWrapper.getEntries().isEmpty());
        assertNull(emptyWrapper.getDefaultConnectionFactory());
    }

    @Test
    void shouldInstantiateRoutingConnectionFactory() throws Exception {
        final MultiRabbitConnectionFactoryWrapper externalWrapper = new MultiRabbitConnectionFactoryWrapper();
        externalWrapper.setDefaultConnectionFactory(connectionFactory0);
        externalWrapper.addConnectionFactory(DUMMY_KEY, connectionFactory1, containerFactory, rabbitAdmin);

        when(springFactoryCreator.rabbitConnectionFactory(rabbitProperties, resourceLoader, credentialsProvider,
                credentialsRefreshService, connectionNameStrategy)).thenReturn(new CachingConnectionFactory());

        assertTrue(creator().routingConnectionFactory(rabbitProperties, multiRabbitProperties, externalWrapper,
                resourceLoader,
                credentialsProvider, credentialsRefreshService, connectionNameStrategy)
                instanceof RoutingConnectionFactory);
    }

    @Test
    void shouldInstantiateRoutingConnectionFactoryWithDefaultAndMultipleConnections() throws Exception {
        final MultiRabbitConnectionFactoryWrapper externalWrapper = new MultiRabbitConnectionFactoryWrapper();
        externalWrapper.setDefaultConnectionFactory(connectionFactory0);
        externalWrapper.addConnectionFactory(DUMMY_KEY, connectionFactory1, containerFactory, rabbitAdmin);

        final ConnectionFactory routingConnectionFactory = creator().routingConnectionFactory(rabbitProperties,
                multiRabbitProperties, externalWrapper, resourceLoader, credentialsProvider, credentialsRefreshService,
                connectionNameStrategy);

        assertTrue(routingConnectionFactory instanceof SimpleRoutingConnectionFactory);
        verify(beanFactory).registerSingleton(DUMMY_KEY, containerFactory);
        verify(beanFactory).registerSingleton(
            eq(DUMMY_KEY + MultiRabbitConstants.RABBIT_ADMIN_SUFFIX), any(RabbitAdmin.class)
        );
        verifyNoMoreInteractions(beanFactory);
    }

    @Test
    void shouldInstantiateRoutingConnectionFactoryWithOnlyDefaultConnectionFactory() throws Exception {
        final MultiRabbitConnectionFactoryWrapper externalWrapper = new MultiRabbitConnectionFactoryWrapper();
        externalWrapper.setDefaultConnectionFactory(connectionFactory0);

        final ConnectionFactory routingConnectionFactory = creator().routingConnectionFactory(rabbitProperties,
                multiRabbitProperties, externalWrapper, resourceLoader, credentialsProvider, credentialsRefreshService,
                connectionNameStrategy);

        assertTrue(routingConnectionFactory instanceof SimpleRoutingConnectionFactory);
        verifyNoMoreInteractions(beanFactory);
    }

    @Test
    void shouldInstantiateRoutingConnectionFactoryWithOnlyMultipleConnectionFactories() throws Exception {
        final MultiRabbitConnectionFactoryWrapper externalWrapper = new MultiRabbitConnectionFactoryWrapper();
        externalWrapper.setDefaultConnectionFactory(connectionFactory0);
        externalWrapper.addConnectionFactory(DUMMY_KEY, connectionFactory1, containerFactory, rabbitAdmin);

        when(springFactoryCreator.rabbitConnectionFactory(rabbitProperties, resourceLoader, credentialsProvider,
                credentialsRefreshService, connectionNameStrategy)).thenReturn(new CachingConnectionFactory());

        final ConnectionFactory routingConnectionFactory = creator().routingConnectionFactory(rabbitProperties,
                multiRabbitProperties, externalWrapper, resourceLoader, credentialsProvider, credentialsRefreshService,
                connectionNameStrategy);

        assertTrue(routingConnectionFactory instanceof SimpleRoutingConnectionFactory);
        verify(beanFactory).registerSingleton(DUMMY_KEY, containerFactory);
        verify(beanFactory).registerSingleton(
            eq(DUMMY_KEY + MultiRabbitConstants.RABBIT_ADMIN_SUFFIX), any(RabbitAdmin.class)
        );
        verifyNoMoreInteractions(beanFactory);
    }

    @Test
    void shouldReachDefaultConnectionFactoryWhenNotBound() throws Exception {
        final MultiRabbitConnectionFactoryWrapper externalWrapper = new MultiRabbitConnectionFactoryWrapper();
        externalWrapper.setDefaultConnectionFactory(connectionFactory0);
        externalWrapper.addConnectionFactory(DUMMY_KEY, connectionFactory1, containerFactory, rabbitAdmin);

        creator().routingConnectionFactory(rabbitProperties, multiRabbitProperties, externalWrapper, resourceLoader,
                credentialsProvider, credentialsRefreshService, connectionNameStrategy).getVirtualHost();

        verify(connectionFactory0).getVirtualHost();
        verify(connectionFactory1, never()).getVirtualHost();
    }

    @Test
    void shouldBindAndReachMultiConnectionFactory() throws Exception {
        final MultiRabbitConnectionFactoryWrapper externalWrapper = new MultiRabbitConnectionFactoryWrapper();
        externalWrapper.setDefaultConnectionFactory(connectionFactory0);
        externalWrapper.addConnectionFactory(DUMMY_KEY, connectionFactory1, containerFactory, rabbitAdmin);

        ConnectionFactory routingConnectionFactory = creator().routingConnectionFactory(rabbitProperties,
                multiRabbitProperties, externalWrapper, resourceLoader, credentialsProvider, credentialsRefreshService,
                connectionNameStrategy);

        SimpleResourceHolder.bind(routingConnectionFactory, DUMMY_KEY);
        routingConnectionFactory.getVirtualHost();
        SimpleResourceHolder.unbind(routingConnectionFactory);

        verify(connectionFactory0, never()).getVirtualHost();
        verify(connectionFactory1).getVirtualHost();
    }

    @Test
    void shouldInstantiateMultiRabbitConnectionFactoryWrapperWithDefaultConnection() throws Exception {
        final MultiRabbitConnectionFactoryWrapper externalWrapper = new MultiRabbitConnectionFactoryWrapper();

        when(springFactoryCreator.rabbitConnectionFactory(rabbitProperties, resourceLoader, credentialsProvider,
                credentialsRefreshService, connectionNameStrategy)).thenReturn(new CachingConnectionFactory());

        assertNotNull(creator().routingConnectionFactory(rabbitProperties, null, externalWrapper,
                resourceLoader, credentialsProvider, credentialsRefreshService, connectionNameStrategy));
    }

    @Test
    void shouldInstantiateMultiRabbitConnectionFactoryWrapperWithMultipleConnections() throws Exception {
        final MultiRabbitConnectionFactoryWrapper externalWrapper = new MultiRabbitConnectionFactoryWrapper();

        when(springFactoryCreator.rabbitConnectionFactory(any(RabbitProperties.class), eq(resourceLoader),
                eq(credentialsProvider), eq(credentialsRefreshService), eq(connectionNameStrategy)))
                .thenReturn(new CachingConnectionFactory());

        MultiRabbitProperties multiRabbitProperties = new MultiRabbitProperties();
        multiRabbitProperties.getConnections().put(DUMMY_KEY, secondaryRabbitProperties);
        multiRabbitProperties.setDefaultConnection(DUMMY_KEY);

        creator().routingConnectionFactory(null, multiRabbitProperties, externalWrapper, resourceLoader,
                credentialsProvider, credentialsRefreshService, connectionNameStrategy);

        verify(springFactoryCreator).rabbitConnectionFactory(secondaryRabbitProperties, resourceLoader,
                credentialsProvider, credentialsRefreshService, connectionNameStrategy);
    }

    @Test
    void shouldInstantiateMultiRabbitConnectionFactoryWrapperWithDefaultAndMultipleConnections() throws Exception {
        final MultiRabbitConnectionFactoryWrapper externalWrapper = new MultiRabbitConnectionFactoryWrapper();

        when(springFactoryCreator.rabbitConnectionFactory(any(RabbitProperties.class), eq(resourceLoader),
                eq(credentialsProvider), eq(credentialsRefreshService), eq(connectionNameStrategy)))
                .thenReturn(new CachingConnectionFactory());

        MultiRabbitProperties multiRabbitProperties = new MultiRabbitProperties();
        multiRabbitProperties.getConnections().put(DUMMY_KEY, secondaryRabbitProperties);

        creator().routingConnectionFactory(rabbitProperties, multiRabbitProperties, externalWrapper, resourceLoader,
                credentialsProvider, credentialsRefreshService, connectionNameStrategy);

        verify(springFactoryCreator).rabbitConnectionFactory(rabbitProperties, resourceLoader, credentialsProvider,
                credentialsRefreshService, connectionNameStrategy);
        verify(springFactoryCreator).rabbitConnectionFactory(secondaryRabbitProperties, resourceLoader,
                credentialsProvider, credentialsRefreshService, connectionNameStrategy);
    }

    @Test
    void shouldEncapsulateExceptionWhenFailingToCreateBean() throws Exception {
        final MultiRabbitConnectionFactoryWrapper externalWrapper = new MultiRabbitConnectionFactoryWrapper();

        when(springFactoryCreator.rabbitConnectionFactory(any(RabbitProperties.class), eq(resourceLoader),
                eq(credentialsProvider), eq(credentialsRefreshService), eq(connectionNameStrategy)))
                .thenThrow(new Exception("mocked-exception"));

        MultiRabbitProperties multiRabbitProperties = new MultiRabbitProperties();
        multiRabbitProperties.getConnections().put(DUMMY_KEY, secondaryRabbitProperties);

        final Executable executable = () -> creator().routingConnectionFactory(rabbitProperties, multiRabbitProperties,
                externalWrapper, resourceLoader, credentialsProvider, credentialsRefreshService,
                connectionNameStrategy);

        assertThrows(Exception.class, executable, "mocked-exception");
    }
}
