package org.springframework.boot.autoconfigure.amqp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class MultiRabbitAutoConfigurationTest {

    @Mock
    private ConnectionFactory connectionFactory;

    private MultiRabbitAutoConfiguration config() {
        return new MultiRabbitAutoConfiguration();
    }

    @Test
    void shouldInstantiateDefaultRabbitAdmin() {
        assertTrue(config().amqpAdmin(connectionFactory) instanceof RabbitAdmin);
    }

    @Test
    void shouldInstantiateRabbitConnectionFactoryCreator() {
        assertNotNull(config().rabbitConnectionFactoryCreator());
    }
}
