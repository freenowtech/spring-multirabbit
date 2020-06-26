package org.springframework.boot.autoconfigure.amqp;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;

/**
 * Class to encapsulate configuration for multiple Rabbit connections.
 */
@SingleDefaultRabbit
@ConfigurationProperties("spring.multirabbitmq")
public class MultiRabbitPropertiesMap extends HashMap<String, ExtendedRabbitProperties> {
}
