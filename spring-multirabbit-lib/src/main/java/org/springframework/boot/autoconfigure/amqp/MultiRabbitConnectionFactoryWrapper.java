package org.springframework.boot.autoconfigure.amqp;

import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.rabbit.config.AbstractRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import static java.util.stream.Collectors.toMap;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

/**
 * A wrapper of structures for RabbitMQ connections backed by a {@link HashMap}.
 */
public class MultiRabbitConnectionFactoryWrapper
{

    private final Map<String, Entry> entries = new HashMap<>();
    private ConnectionFactory defaultConnectionFactory;


    /**
     * Sets the default {@code connectionFactory}.
     *
     * @param connectionFactory The {@link ConnectionFactory}.
     */
    public void setDefaultConnectionFactory(ConnectionFactory connectionFactory)
    {
        this.defaultConnectionFactory = connectionFactory;
    }


    /**
     * Returns the default {@link ConnectionFactory} of null if none is set.
     *
     * @return the default {@link ConnectionFactory} of null if none is set.
     */
    ConnectionFactory getDefaultConnectionFactory()
    {
        return defaultConnectionFactory;
    }


    /**
     * Adds a {@link ConnectionFactory}.
     * <p>
     * Mandatory parameters are {@code key} and {@code connectionFactory}, since they cannot be null.
     *
     * @param key               The key for the structures.
     * @param connectionFactory The {@link ConnectionFactory}.
     */
    public void addConnectionFactory(String key, ConnectionFactory connectionFactory)
    {
        addConnectionFactory(key, connectionFactory, null, null);
    }


    /**
     * Adds a {@link ConnectionFactory} associated to a ContainerFactory.
     *
     * @param key               The key for the structures.
     * @param connectionFactory The {@link ConnectionFactory}.
     * @param containerFactory  The related {@link AbstractRabbitListenerContainerFactory}.
     */
    public void addConnectionFactory(
        String key,
        ConnectionFactory connectionFactory,
        AbstractRabbitListenerContainerFactory containerFactory)
    {
        addConnectionFactory(key, connectionFactory, containerFactory, null);
    }


    /**
     * Adds a {@link ConnectionFactory} associated to a ContainerFactory and a RabbitAdmin.
     *
     * @param key               The key for the structures.
     * @param connectionFactory The {@link ConnectionFactory}.
     * @param containerFactory  The related {@link SimpleRabbitListenerContainerFactory}.
     * @param rabbitAdmin       The related {@link RabbitAdmin}.
     */
    public void addConnectionFactory(
        String key,
        ConnectionFactory connectionFactory,
        AbstractRabbitListenerContainerFactory containerFactory,
        RabbitAdmin rabbitAdmin)
    {
        hasText(key, "Key may not be null or empty");
        entries.put(key, new Entry(connectionFactory, containerFactory, rabbitAdmin));
    }


    /**
     * Returns the {@link Map} of {@link ConnectionFactory}s.
     *
     * @return the {@link Map} of {@link ConnectionFactory}s.
     */
    Map<Object, ConnectionFactory> getConnectionFactories()
    {
        return entries.entrySet().stream()
            .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().getConnectionFactory()));
    }


    /**
     * Returns the {@link Map} of {@link AbstractRabbitListenerContainerFactory}s.
     *
     * @return the {@link Map} of {@link AbstractRabbitListenerContainerFactory}s.
     */
    Map<String, AbstractRabbitListenerContainerFactory> getContainerFactories()
    {
        return entries.entrySet().stream()
            .filter(entry -> entry.getValue().getContainerFactory() != null)
            .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().getContainerFactory()));
    }


    /**
     * Returns the {@link Map} of {@link RabbitAdmin}s except the one set with key null.
     *
     * @return the {@link Map} of {@link RabbitAdmin}s except the one set with key null.
     */
    Map<String, RabbitAdmin> getRabbitAdmins()
    {
        return entries.entrySet().stream()
            .filter(entry -> entry.getValue().getRabbitAdmin() != null)
            .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().getRabbitAdmin()));
    }


    /**
     * An {@link Entry} of {@link MultiRabbitConnectionFactoryWrapper}.
     */
    static class Entry
    {

        private final ConnectionFactory connectionFactory;
        private final AbstractRabbitListenerContainerFactory containerFactory;
        private final RabbitAdmin rabbitAdmin;


        Entry(
            ConnectionFactory connectionFactory,
            AbstractRabbitListenerContainerFactory containerFactory,
            RabbitAdmin rabbitAdmin)
        {
            notNull(connectionFactory, "ConnectionFactory may not be null");
            if (containerFactory != null)
            {
                containerFactory.setConnectionFactory(connectionFactory);
            }
            this.connectionFactory = connectionFactory;
            this.containerFactory = containerFactory;
            this.rabbitAdmin = rabbitAdmin;
        }


        /**
         * Returns the {@link ConnectionFactory} of the entry.
         *
         * @return the {@link ConnectionFactory} of the entry.
         */
        ConnectionFactory getConnectionFactory()
        {
            return connectionFactory;
        }


        /**
         * Returns the {@link AbstractRabbitListenerContainerFactory} of the entry.
         *
         * @return the {@link AbstractRabbitListenerContainerFactory} of the entry.
         */
        AbstractRabbitListenerContainerFactory getContainerFactory()
        {
            return containerFactory;
        }


        /**
         * Returns the {@link RabbitAdmin} of the entry.
         *
         * @return the {@link RabbitAdmin} of the entry.
         */
        RabbitAdmin getRabbitAdmin()
        {
            return rabbitAdmin;
        }
    }

}
