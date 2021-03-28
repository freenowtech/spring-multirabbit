package com.freenow.multirabbit.example.extension;

import org.springframework.amqp.rabbit.config.AbstractRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.autoconfigure.amqp.MultiRabbitConnectionFactoryWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExtendedConfiguration {

    static final String EXTENDED_CONNECTION_A = "extendedConnectionNameA";
    static final String EXTENDED_CONNECTION_B = "extendedConnectionNameB";
    static final String EXTENDED_CONNECTION_C = "extendedConnectionNameC";

    static final String EXTENDED_CONNECTION_C_ADMIN_ALIAS_1 = "anotherAdminBeanName";
    static final String EXTENDED_CONNECTION_C_ADMIN_ALIAS_2 = "anotherOtherAdminBeanName";
    static final int CONNECTION_TIMEOUT = 50;

    @Bean
    public MultiRabbitConnectionFactoryWrapper externalWrapper() {
        final ConnectionFactory connA = newConnectionFactory();
        final ConnectionFactory connB = newConnectionFactory();
        final ConnectionFactory connC = newConnectionFactory();
        final MultiRabbitConnectionFactoryWrapper wrapper = new MultiRabbitConnectionFactoryWrapper();
        final RabbitAdmin sharedAdmin = newRabbitAdmin(connA);
        wrapper.addConnectionFactory(EXTENDED_CONNECTION_A, connA, newContainerFactory(connA), sharedAdmin);
        wrapper.addConnectionFactory(EXTENDED_CONNECTION_B, connB, newContainerFactory(connB), newRabbitAdmin(connB));
        wrapper.addConnectionFactory(EXTENDED_CONNECTION_C, connC, newContainerFactory(connC), sharedAdmin,
                EXTENDED_CONNECTION_C_ADMIN_ALIAS_1, EXTENDED_CONNECTION_C_ADMIN_ALIAS_2);
        wrapper.setDefaultConnectionFactory(connA);
        return wrapper;
    }

    private ConnectionFactory newConnectionFactory() {
        final CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setConnectionTimeout(CONNECTION_TIMEOUT);
        return connectionFactory;
    }

    private AbstractRabbitListenerContainerFactory newContainerFactory(final ConnectionFactory connectionFactory) {
        final SimpleRabbitListenerContainerFactory container = new SimpleRabbitListenerContainerFactory();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    private RabbitAdmin newRabbitAdmin(final ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
