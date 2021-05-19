package org.springframework.boot.autoconfigure.amqp;

import org.springframework.amqp.rabbit.config.AbstractRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

/**
 * A wrapper of structures for RabbitMQ connections backed by a {@link HashMap}. This class is intended to offer an easy
 * way to integrate with MultiRabbit by allowing an external library to plug in and provide factories.
 *
 * @author Wander Costa
 */
public class MultiRabbitConnectionFactoryWrapper {

    private final Map<String, Entry> entries = new HashMap<>();
    private final Map<String, Entry> immutableEntries = Collections.unmodifiableMap(entries);

    private ConnectionFactory defaultConnectionFactory;

    /**
     * Copies the entries from another {@link MultiRabbitConnectionFactoryWrapper}, replacing exiting keys (if any).
     *
     * @param fromWrapper The wrapper to copy from.
     */
    void putEntriesFrom(final MultiRabbitConnectionFactoryWrapper fromWrapper) {
        if (fromWrapper != null) {
            fromWrapper.getEntries().forEach(entries::put);
        }
    }

    /**
     * Sets the default {@code connectionFactory}.
     *
     * @param connectionFactory The {@link ConnectionFactory}.
     */
    public void setDefaultConnectionFactory(final ConnectionFactory connectionFactory) {
        this.defaultConnectionFactory = connectionFactory;
    }

    /**
     * Returns the default {@link ConnectionFactory} of null if none is set.
     *
     * @return the default {@link ConnectionFactory} of null if none is set.
     */
    ConnectionFactory getDefaultConnectionFactory() {
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
    public void addConnectionFactory(final String key, final ConnectionFactory connectionFactory) {
        addConnectionFactory(key, connectionFactory, null, null);
    }

    /**
     * Adds a {@link ConnectionFactory} associated to a ContainerFactory.
     *
     * @param key               The key for the structures.
     * @param connectionFactory The {@link ConnectionFactory}.
     * @param containerFactory  The related {@link AbstractRabbitListenerContainerFactory}.
     */
    public void addConnectionFactory(final String key,
                                     final ConnectionFactory connectionFactory,
                                     final AbstractRabbitListenerContainerFactory<?> containerFactory) {
        addConnectionFactory(key, connectionFactory, containerFactory, null);
    }

    /**
     * Adds a {@link ConnectionFactory} associated to a ContainerFactory and a RabbitAdmin.
     *
     * @param key                The key for the structures.
     * @param connectionFactory  The {@link ConnectionFactory}.
     * @param containerFactory   The related
     *                           {@link org.springframework.amqp.rabbit.config.AbstractRabbitListenerContainerFactory}.
     * @param rabbitAdmin        The related {@link RabbitAdmin}.
     */
    public void addConnectionFactory(final String key,
                                     final ConnectionFactory connectionFactory,
                                     final AbstractRabbitListenerContainerFactory<?> containerFactory,
                                     final RabbitAdmin rabbitAdmin) {
        hasText(key, "Key may not be null or empty");
        entries.put(key, new Entry(connectionFactory, containerFactory, rabbitAdmin));
    }

    /**
     * Returns an immutable map of entries.
     *
     * @return a map of entries.
     */
    public Map<String, Entry> getEntries() {
        return immutableEntries;
    }

    /**
     * Returns the {@link Map} of {@link ConnectionFactory}s.
     *
     * @return the {@link Map} of {@link ConnectionFactory}s.
     */
    Map<Object, ConnectionFactory> getConnectionFactories() {
        return immutableEntries.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().getConnectionFactory()));
    }

    /**
     * An {@link Entry} of {@link MultiRabbitConnectionFactoryWrapper}.
     */
    static final class Entry {

        private final ConnectionFactory connectionFactory;
        private final AbstractRabbitListenerContainerFactory<?> containerFactory;
        private final RabbitAdmin rabbitAdmin;

        /**
         * Returns an entry containing the triple for the wrapper.
         *
         * @param connectionFactory The related {@link ConnectionFactory}.
         * @param containerFactory  The related {@link AbstractRabbitListenerContainerFactory}.
         * @param rabbitAdmin       The related {@link RabbitAdmin}.
         */
        private Entry(final ConnectionFactory connectionFactory,
                      final AbstractRabbitListenerContainerFactory<?> containerFactory,
                      final RabbitAdmin rabbitAdmin) {
            notNull(connectionFactory, "ConnectionFactory may not be null");
            if (containerFactory != null) {
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
        ConnectionFactory getConnectionFactory() {
            return connectionFactory;
        }

        /**
         * Returns the {@link AbstractRabbitListenerContainerFactory} of the entry.
         *
         * @return the {@link AbstractRabbitListenerContainerFactory} of the entry.
         */
        AbstractRabbitListenerContainerFactory<?> getContainerFactory() {
            return containerFactory;
        }

        /**
         * Returns the {@link RabbitAdmin} of the entry.
         *
         * @return the {@link RabbitAdmin} of the entry.
         */
        RabbitAdmin getRabbitAdmin() {
            return rabbitAdmin;
        }
    }
}
