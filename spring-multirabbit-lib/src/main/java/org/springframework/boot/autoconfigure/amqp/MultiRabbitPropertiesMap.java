package org.springframework.boot.autoconfigure.amqp;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static java.util.stream.Collectors.toSet;

/**
 * Class to encapsulate configuration for multiple Rabbit connections.
 */
@SingleDefaultRabbit
@ConfigurationProperties("spring.multirabbitmq")
public class MultiRabbitPropertiesMap extends HashMap<String, ExtendedRabbitProperties>
{

    private static final Gson GSON = new Gson();


    /**
     * Hacking on Properties processing from SprinbBoot 1.x that does not instantiate the object
     * but an implementation of a Map representing the object.
     *
     * @return The really parsed set of entries.
     */
    @Override
    public Set<Entry<String, ExtendedRabbitProperties>> entrySet()
    {
        return super.entrySet().stream()
            .map(this::parseToRealInstance)
            .collect(toSet());
    }


    /**
     * Returns the entry of {@link ExtendedRabbitProperties} in case it's available as a Map or
     * the provided instance otherwise.
     */
    private Entry<String, ExtendedRabbitProperties> parseToRealInstance(Entry<String, ExtendedRabbitProperties> entry)
    {
        Object value = entry.getValue();
        if (value instanceof Map)
        {
            String json = GSON.toJson(value);
            ExtendedRabbitProperties newValue = GSON.fromJson(json, ExtendedRabbitProperties.class);
            return new SimpleEntry(entry.getKey(), newValue);
        }
        return entry;
    }


}