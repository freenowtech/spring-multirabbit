package com.mytaxi.spring.multirabbit.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ExtendedConnectionFactoriesTest {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Test
    public void shouldLoadExtendedConnectionFactoryA() {
        final SimpleRoutingConnectionFactory routingConnectionFactory
            = (SimpleRoutingConnectionFactory) connectionFactory;
        final ConnectionFactory extendedConnectionFactoryA = routingConnectionFactory
            .getTargetConnectionFactory(SomeListeners.EXTENDED_CONNECTION_A);
        assertNotNull(extendedConnectionFactoryA);
    }

    @Test
    public void shouldLoadExtendedConnectionFactoryB() {
        final SimpleRoutingConnectionFactory routingConnectionFactory
            = (SimpleRoutingConnectionFactory) connectionFactory;
        final ConnectionFactory extendedConnectionFactoryB = routingConnectionFactory
            .getTargetConnectionFactory(SomeListeners.EXTENDED_CONNECTION_B);
        assertNotNull(extendedConnectionFactoryB);
    }
}
