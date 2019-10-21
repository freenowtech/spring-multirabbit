package org.springframework.boot.autoconfigure.amqp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class MultiRabbitAutoConfigurationTest
{

    @Mock
    private ConnectionFactory connectionFactory;


    private MultiRabbitAutoConfiguration config()
    {
        return new MultiRabbitAutoConfiguration();
    }


    @Test
    public void shouldInstantiateDefaultRabbitAdmin()
    {
        assertTrue(config().amqpAdmin(connectionFactory) instanceof RabbitAdmin);
    }


    @Test
    public void shouldInstantiateRabbitConnectionFactoryCreator()
    {
        assertTrue(config().rabbitConnectionFactoryCreator() instanceof RabbitAutoConfiguration.RabbitConnectionFactoryCreator);
    }

}