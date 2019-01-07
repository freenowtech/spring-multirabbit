package org.springframework.boot.autoconfigure.amqp;

import java.util.HashMap;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Class to encapsulate configuration for multiple Rabbit connections.
 */
@SingleDefaultRabbit
@ConfigurationProperties("spring.multirabbitmq")
public class MultiRabbitPropertiesMap extends HashMap<String, ExtendedRabbitProperties>
{
}