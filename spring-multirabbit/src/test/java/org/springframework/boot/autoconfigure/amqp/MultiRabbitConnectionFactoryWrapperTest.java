package org.springframework.boot.autoconfigure.amqp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class MultiRabbitConnectionFactoryWrapperTest {

    private static final String DUMMY_KEY = "dummy-key";

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private SimpleRabbitListenerContainerFactory containerFactory;

    @Mock
    private RabbitAdmin rabbitAdmin;

    private MultiRabbitConnectionFactoryWrapper wrapper() {
        return new MultiRabbitConnectionFactoryWrapper();
    }

    @Test
    void shouldGetDefaultConnectionFactory() {
        MultiRabbitConnectionFactoryWrapper wrapper = wrapper();
        wrapper.setDefaultConnectionFactory(connectionFactory);

        assertSame(connectionFactory, wrapper.getDefaultConnectionFactory());
    }

    @Test
    void shouldSetNullDefaultConnectionFactory() {
        MultiRabbitConnectionFactoryWrapper wrapper = wrapper();
        wrapper.setDefaultConnectionFactory(null);

        assertNull(wrapper.getDefaultConnectionFactory());
    }

    @Test
    void shouldAddConnectionFactory() {
        MultiRabbitConnectionFactoryWrapper wrapper = wrapper();
        wrapper.addConnectionFactory(DUMMY_KEY, connectionFactory);

        assertSame(connectionFactory, wrapper.getEntries().get(DUMMY_KEY).getConnectionFactory());
        assertNull(wrapper.getEntries().get(DUMMY_KEY).getContainerFactory());
        assertNull(wrapper.getEntries().get(DUMMY_KEY).getRabbitAdmin());
    }

    @Test
    void shouldAddConnectionFactoryWithContainerFactory() {
        MultiRabbitConnectionFactoryWrapper wrapper = wrapper();
        wrapper.addConnectionFactory(DUMMY_KEY, connectionFactory, containerFactory);

        assertSame(connectionFactory, wrapper.getEntries().get(DUMMY_KEY).getConnectionFactory());
        assertSame(containerFactory, wrapper.getEntries().get(DUMMY_KEY).getContainerFactory());
        assertNull(wrapper.getEntries().get(DUMMY_KEY).getRabbitAdmin());
    }

    @Test
    void shouldAddConnectionFactoryWithContainerFactoryAndRabbitAdmin() {
        MultiRabbitConnectionFactoryWrapper wrapper = wrapper();
        wrapper.addConnectionFactory(DUMMY_KEY, connectionFactory, containerFactory, rabbitAdmin);

        assertSame(connectionFactory, wrapper.getEntries().get(DUMMY_KEY).getConnectionFactory());
        assertSame(containerFactory, wrapper.getEntries().get(DUMMY_KEY).getContainerFactory());
        assertSame(rabbitAdmin, wrapper.getEntries().get(DUMMY_KEY).getRabbitAdmin());
    }

    @Test
    void shouldNotAddNullConnectionFactory() {
        final Executable executable =
                () -> wrapper().addConnectionFactory(DUMMY_KEY, null, containerFactory, rabbitAdmin);
        assertThrows(IllegalArgumentException.class, executable, "ConnectionFactory may not be null");
    }

    @Test
    void shouldNotAddConnectionFactoryWithEmptyKey() {
        final Executable executable =
                () -> wrapper().addConnectionFactory("", connectionFactory, containerFactory, rabbitAdmin);
        assertThrows(IllegalArgumentException.class, executable, "Key may not be null or empty");
    }
}
