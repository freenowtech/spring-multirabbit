package org.springframework.boot.autoconfigure.amqp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;

import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Configuration properties for multiple Rabbit.
 *
 * @author Wander Costa
 * @see RabbitProperties
 */
@ConfigurationProperties(prefix = "spring.multirabbitmq")
public class MultiRabbitProperties {

    /**
     * Enables Multiple Rabbit.
     */
    private boolean enabled = false;

    /**
     * The name of the default connection, which will not require context binding
     * to be used.
     */
    private String defaultConnection;

    /**
     * The {@link Map} of {@link RabbitProperties} to initialize connections.
     */
    private Map<String, RabbitProperties> connections = new HashMap<>();

    /**
     * Returns the default {@link RabbitProperties}.
     *
     * @return the default {@link RabbitProperties}.
     */
    @Nullable
    public String getDefaultConnection() {
        return defaultConnection;
    }

    /**
     * Defines the {@link RabbitProperties} of the default connection.
     *
     * @param defaultConnection The {@link RabbitProperties}.
     */
    public void setDefaultConnection(@Nullable final String defaultConnection) {
        this.defaultConnection = defaultConnection;
    }

    /**
     * Returns the {@link Map} of additional {@link RabbitProperties}.
     *
     * @return the {@link Map} of additional {@link RabbitProperties}.
     */
    @NotNull
    public Map<String, RabbitProperties> getConnections() {
        return connections;
    }

    /**
     * Defines the {@link Map} of additional {@link RabbitProperties}, falling back to a new {@link HashMap} of null is
     * provided.
     *
     * @param connections The {@link Map} of additional {@link RabbitProperties}.
     */
    public void setConnections(@Nullable final Map<String, RabbitProperties> connections) {
        this.connections = Optional.ofNullable(connections).orElse(new HashMap<>());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }
}
