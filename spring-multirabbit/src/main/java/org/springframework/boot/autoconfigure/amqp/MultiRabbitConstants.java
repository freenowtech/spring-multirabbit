package org.springframework.boot.autoconfigure.amqp;

/**
 * Class to share related constants.
 */
public final class MultiRabbitConstants {

    public static final String CONNECTION_FACTORY_BEAN_NAME = "multiRabbitConnectionFactory";
    public static final String CONNECTION_FACTORY_CREATOR_BEAN_NAME = "rabbitConnectionFactoryCreator";
    public static final String DEFAULT_RABBIT_ADMIN_BEAN_NAME = "amqpAdmin";
    public static final String RABBIT_ADMIN_SUFFIX = "-admin";
    public static final String DEFAULT_CONTAINER_FACTORY_BEAN_NAME = "rabbitListenerContainerFactory";
    public static final String MULTI_RABBIT_ENABLED_PROPERTY = "spring.multirabbitmq.enabled";

    private MultiRabbitConstants() {
    }

}
