package org.springframework.boot.autoconfigure.amqp;

import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;

/**
 * Configuration properties for multiple Rabbit.
 *
 * @author Wander Costa
 * @see RabbitProperties
 */
@ConfigurationProperties(prefix = "spring.multirabbitmq")
public class MultiRabbitProperties
{

    /**
     * The name of the default connection, which will not require context binding
     * to be used.
     */
    private String defaultConnection;

    /**
     * The {@link Map} of {@link RabbitProperties} to initialize connections.
     */
    private Map<String, RabbitProperties> connections = new HashMap<>();


    @Nullable
    public String getDefaultConnection()
    {
        return defaultConnection;
    }


    public void setDefaultConnection(@Nullable String defaultConnection)
    {
        this.defaultConnection = defaultConnection;
    }


    @NotNull
    public Map<String, RabbitProperties> getConnections()
    {
        return connections;
    }


    public void setConnections(@Nullable Map<String, RabbitProperties> connections)
    {
        this.connections = connections != null ? connections : new HashMap<>();
    }
}