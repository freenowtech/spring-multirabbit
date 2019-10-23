package org.springframework.boot.autoconfigure.amqp;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MultiRabbitConnectionFactoryCreatorTest
{

    private static final String DUMMY_KEY = "dummy-key";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private ConfigurableListableBeanFactory beanFactory;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ObjectProvider<ConnectionNameStrategy> connectionNameStrategy;

    @Mock
    private MultiRabbitConnectionFactoryWrapper wrapper;

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


    private MultiRabbitAutoConfiguration.MultiRabbitConnectionFactoryCreator creator()
    {
        MultiRabbitAutoConfiguration.MultiRabbitConnectionFactoryCreator config
            = new MultiRabbitAutoConfiguration.MultiRabbitConnectionFactoryCreator(springFactoryCreator, connectionNameStrategy);
        config.setBeanFactory(beanFactory);
        config.setApplicationContext(applicationContext);
        return config;
    }


    @Test
    public void shouldInstantiateExternalEmptyWrapper()
    {
        MultiRabbitConnectionFactoryWrapper emptyWrapper = creator().externalEmptyWrapper();
        assertTrue(emptyWrapper.getConnectionFactories().isEmpty());
        assertNull(emptyWrapper.getDefaultConnectionFactory());
    }


    @Test
    public void shouldInstantiateRoutingConnectionFactory()
    {
        assertTrue(creator().routingConnectionFactory(rabbitProperties, multiRabbitProperties, wrapper) instanceof RoutingConnectionFactory);
    }


    @Test
    public void shouldInstantiateRoutingConnectionFactoryWithDefaultAndMultipleConnections()
    {
        when(wrapper.getDefaultConnectionFactory()).thenReturn(connectionFactory0);
        when(wrapper.getConnectionFactories()).thenReturn(singletonMap(DUMMY_KEY, connectionFactory1));
        when(wrapper.getContainerFactories()).thenReturn(singletonMap(DUMMY_KEY, containerFactory));
        when(wrapper.getRabbitAdmins()).thenReturn(singletonMap(DUMMY_KEY, rabbitAdmin));

        ConnectionFactory routingConnectionFactory = creator().routingConnectionFactory(rabbitProperties, multiRabbitProperties, wrapper);

        assertTrue(routingConnectionFactory instanceof SimpleRoutingConnectionFactory);
        verify(beanFactory).registerSingleton(DUMMY_KEY, containerFactory);
        verify(beanFactory).registerSingleton(DUMMY_KEY + MultiRabbitConstants.RABBIT_ADMIN_SUFFIX, rabbitAdmin);
        verifyNoMoreInteractions(beanFactory);
    }


    @Test
    public void shouldInstantiateRoutingConnectionFactoryWithOnlyDefaultConnectionFactory()
    {
        when(wrapper.getDefaultConnectionFactory()).thenReturn(connectionFactory0);

        ConnectionFactory routingConnectionFactory = creator().routingConnectionFactory(rabbitProperties, multiRabbitProperties, wrapper);

        assertTrue(routingConnectionFactory instanceof SimpleRoutingConnectionFactory);
        verifyZeroInteractions(beanFactory);
    }


    @Test
    public void shouldInstantiateRoutingConnectionFactoryWithOnlyMultipleConnectionFactories()
    {
        when(wrapper.getConnectionFactories()).thenReturn(singletonMap(DUMMY_KEY, connectionFactory0));
        when(wrapper.getContainerFactories()).thenReturn(singletonMap(DUMMY_KEY, containerFactory));
        when(wrapper.getRabbitAdmins()).thenReturn(singletonMap(DUMMY_KEY, rabbitAdmin));

        ConnectionFactory routingConnectionFactory = creator().routingConnectionFactory(rabbitProperties, multiRabbitProperties, wrapper);

        assertTrue(routingConnectionFactory instanceof SimpleRoutingConnectionFactory);
        verify(beanFactory).registerSingleton(DUMMY_KEY, containerFactory);
        verify(beanFactory).registerSingleton(DUMMY_KEY + MultiRabbitConstants.RABBIT_ADMIN_SUFFIX, rabbitAdmin);
        verifyNoMoreInteractions(beanFactory);
    }


    @Test
    public void shouldReachDefaultConnectionFactoryWhenNotBound()
    {
        when(wrapper.getDefaultConnectionFactory()).thenReturn(connectionFactory0);
        when(wrapper.getConnectionFactories()).thenReturn(singletonMap(DUMMY_KEY, connectionFactory1));
        when(wrapper.getContainerFactories()).thenReturn(singletonMap(DUMMY_KEY, containerFactory));
        when(wrapper.getRabbitAdmins()).thenReturn(singletonMap(DUMMY_KEY, rabbitAdmin));

        creator().routingConnectionFactory(rabbitProperties, multiRabbitProperties, wrapper).getVirtualHost();

        verify(connectionFactory0).getVirtualHost();
        verifyZeroInteractions(connectionFactory1);
    }


    @Test
    public void shouldBindAndReachMultiConnectionFactory()
    {
        when(wrapper.getDefaultConnectionFactory()).thenReturn(connectionFactory0);
        when(wrapper.getConnectionFactories()).thenReturn(singletonMap(DUMMY_KEY, connectionFactory1));
        when(wrapper.getContainerFactories()).thenReturn(singletonMap(DUMMY_KEY, containerFactory));
        when(wrapper.getRabbitAdmins()).thenReturn(singletonMap(DUMMY_KEY, rabbitAdmin));

        ConnectionFactory routingConnectionFactory = creator().routingConnectionFactory(rabbitProperties, multiRabbitProperties, wrapper);

        SimpleResourceHolder.bind(routingConnectionFactory, DUMMY_KEY);
        routingConnectionFactory.getVirtualHost();
        SimpleResourceHolder.unbind(routingConnectionFactory);

        verifyZeroInteractions(connectionFactory0);
        verify(connectionFactory1).getVirtualHost();
    }


    @Test
    public void shouldInstantiateMultiRabbitConnectionFactoryWrapperWithDefaultConnection()
    {
        assertNotNull(creator().routingConnectionFactory(rabbitProperties, null, wrapper));
    }


    @Test
    public void shouldInstantiateMultiRabbitConnectionFactoryWrapperWithMultipleConnections() throws Exception
    {
        when(springFactoryCreator.rabbitConnectionFactory(any(RabbitProperties.class), eq(connectionNameStrategy)))
            .thenReturn(new CachingConnectionFactory());

        MultiRabbitProperties multiRabbitProperties = new MultiRabbitProperties();
        multiRabbitProperties.getConnections().put(DUMMY_KEY, secondaryRabbitProperties);

        creator().routingConnectionFactory(null, multiRabbitProperties, wrapper);

        verify(springFactoryCreator).rabbitConnectionFactory(secondaryRabbitProperties, connectionNameStrategy);
    }


    @Test
    public void shouldInstantiateMultiRabbitConnectionFactoryWrapperWithDefaultAndMultipleConnections() throws Exception
    {
        when(springFactoryCreator.rabbitConnectionFactory(any(RabbitProperties.class), eq(connectionNameStrategy)))
            .thenReturn(new CachingConnectionFactory());

        MultiRabbitProperties multiRabbitProperties = new MultiRabbitProperties();
        multiRabbitProperties.getConnections().put(DUMMY_KEY, secondaryRabbitProperties);

        creator().routingConnectionFactory(rabbitProperties, multiRabbitProperties, wrapper);

        verify(springFactoryCreator).rabbitConnectionFactory(rabbitProperties, connectionNameStrategy);
        verify(springFactoryCreator).rabbitConnectionFactory(secondaryRabbitProperties, connectionNameStrategy);
    }


    @Test
    public void shouldEncapsulateExceptionWhenFailingToCreateBean() throws Exception
    {
        exception.expect(RuntimeException.class);
        exception.expectMessage("mocked-exception");

        when(springFactoryCreator.rabbitConnectionFactory(any(RabbitProperties.class), eq(connectionNameStrategy)))
            .thenThrow(new Exception("mocked-exception"));

        MultiRabbitProperties multiRabbitProperties = new MultiRabbitProperties();
        multiRabbitProperties.getConnections().put(DUMMY_KEY, secondaryRabbitProperties);

        creator().routingConnectionFactory(rabbitProperties, multiRabbitProperties, wrapper);
    }

}