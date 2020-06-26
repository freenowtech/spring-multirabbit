package com.mytaxi.spring.multirabbit.example

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

@SpringBootTest
@RunWith(SpringRunner::class)
class ConnectionFactoriesTest {

    @Autowired
    lateinit var connectionFactory: ConnectionFactory

    @Value("\${spring.rabbitmq.port}")
    private val portDefaultConnection: Int = 0

    @Value("\${spring.multirabbitmq.connectionNameA.port}")
    private val portConnectionA: Int = 0

    @Value("\${spring.multirabbitmq.connectionNameB.port}")
    private val portConnectionB: Int = 0

    @Test
    fun shouldLoadDefaultConnectionFactory() {
        assertNotNull(connectionFactory)
        assertEquals(portDefaultConnection, connectionFactory.port)
    }

    @Test
    fun shouldLoadSecondaryConnectionFactoryA() {
        val routingConnectionFactory = connectionFactory as SimpleRoutingConnectionFactory?
        val connectionFactoryA = routingConnectionFactory!!
                .getTargetConnectionFactory(SomeListeners.CONNECTION_A)
        assertNotNull(connectionFactoryA)
        assertEquals(portConnectionA, connectionFactoryA.port)
    }

    @Test
    fun shouldLoadSecondaryConnectionFactoryB() {
        val routingConnectionFactory = connectionFactory as SimpleRoutingConnectionFactory?
        val connectionFactoryB = routingConnectionFactory!!
                .getTargetConnectionFactory(SomeListeners.CONNECTION_B)
        assertNotNull(connectionFactoryB)
        assertEquals(portConnectionB, connectionFactoryB.port)
    }
}
