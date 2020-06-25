package com.mytaxi.spring.multirabbit.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ConnectionFactoriesTest {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Value("${spring.rabbitmq.port}")
    private int portDefaultConnection;

    @Value("${spring.multirabbitmq.connections.connectionNameA.port}")
    private int portConnectionA;

    @Value("${spring.multirabbitmq.connections.connectionNameB.port}")
    private int portConnectionB;

    @Test
    public void shouldLoadDefaultConnectionFactory() {
        assertNotNull(connectionFactory);
        assertEquals(portDefaultConnection, connectionFactory.getPort());
    }

    @Test
    public void shouldLoadSecondaryConnectionFactoryA() {
        final SimpleRoutingConnectionFactory routingConnectionFactory
            = (SimpleRoutingConnectionFactory) connectionFactory;
        final ConnectionFactory connectionFactoryA = routingConnectionFactory
            .getTargetConnectionFactory(SomeListeners.CONNECTION_A);
        assertNotNull(connectionFactoryA);
        assertEquals(portConnectionA, connectionFactoryA.getPort());
    }

    @Test
    public void shouldLoadSecondaryConnectionFactoryB() {
        final SimpleRoutingConnectionFactory routingConnectionFactory
            = (SimpleRoutingConnectionFactory) connectionFactory;
        final ConnectionFactory connectionFactoryB = routingConnectionFactory
            .getTargetConnectionFactory(SomeListeners.CONNECTION_B);
        assertNotNull(connectionFactoryB);
        assertEquals(portConnectionB, connectionFactoryB.getPort());
    }
}
