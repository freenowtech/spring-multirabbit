package org.springframework.boot.autoconfigure.amqp;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

@RunWith(MockitoJUnitRunner.class)
public class MultiRabbitConnectionFactoryWrapperTest
{

    private static final String DUMMY_KEY = "dummy-key";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private SimpleRabbitListenerContainerFactory containerFactory;

    @Mock
    private RabbitAdmin rabbitAdmin;


    public MultiRabbitConnectionFactoryWrapper wrapper()
    {
        return new MultiRabbitConnectionFactoryWrapper();
    }


    @Test
    public void shouldGetDefaultConnectionFactory()
    {
        MultiRabbitConnectionFactoryWrapper wrapper = wrapper();
        wrapper.setDefaultConnectionFactory(connectionFactory);

        assertSame(connectionFactory, wrapper.getDefaultConnectionFactory());
    }


    @Test
    public void shouldSetNullDefaultConnectionFactory()
    {
        MultiRabbitConnectionFactoryWrapper wrapper = wrapper();
        wrapper.setDefaultConnectionFactory(null);

        assertNull(wrapper.getDefaultConnectionFactory());
    }


    @Test
    public void shouldAddConnectionFactory()
    {
        MultiRabbitConnectionFactoryWrapper wrapper = wrapper();
        wrapper.addConnectionFactory(DUMMY_KEY, connectionFactory);

        assertSame(connectionFactory, wrapper.getConnectionFactories().get(DUMMY_KEY));
        assertNull(wrapper.getContainerFactories().get(DUMMY_KEY));
        assertNull(wrapper.getRabbitAdmins().get(DUMMY_KEY));
    }


    @Test
    public void shouldAddConnectionFactoryWithContainerFactory()
    {
        MultiRabbitConnectionFactoryWrapper wrapper = wrapper();
        wrapper.addConnectionFactory(DUMMY_KEY, connectionFactory, containerFactory);

        assertSame(connectionFactory, wrapper.getConnectionFactories().get(DUMMY_KEY));
        assertSame(containerFactory, wrapper.getContainerFactories().get(DUMMY_KEY));
        assertNull(wrapper.getRabbitAdmins().get(DUMMY_KEY));
    }


    @Test
    public void shouldAddConnectionFactoryWithContainerFactoryAndRabbitAdmin()
    {
        MultiRabbitConnectionFactoryWrapper wrapper = wrapper();
        wrapper.addConnectionFactory(DUMMY_KEY, connectionFactory, containerFactory, rabbitAdmin);

        assertSame(connectionFactory, wrapper.getConnectionFactories().get(DUMMY_KEY));
        assertSame(containerFactory, wrapper.getContainerFactories().get(DUMMY_KEY));
        assertSame(rabbitAdmin, wrapper.getRabbitAdmins().get(DUMMY_KEY));
    }


    @Test
    public void shouldNotAddNullConnectionFactory()
    {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("ConnectionFactory may not be null");
        wrapper().addConnectionFactory(DUMMY_KEY, null, containerFactory, rabbitAdmin);
    }


    @Test
    public void shouldNotAddConnectionFactoryWithEmptyKey()
    {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Key may not be null or empty");
        wrapper().addConnectionFactory("", connectionFactory, containerFactory, rabbitAdmin);
    }

}