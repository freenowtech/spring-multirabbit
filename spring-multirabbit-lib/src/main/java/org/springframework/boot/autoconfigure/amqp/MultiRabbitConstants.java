package org.springframework.boot.autoconfigure.amqp;

/**
 * Class to share related constants.
 */
public final class MultiRabbitConstants {

    static final String CONNECTION_FACTORY_BEAN_NAME = "multiRabbitConnectionFactory";
    static final String CONNECTION_FACTORY_CREATOR_BEAN_NAME = "rabbitConnectionFactoryCreator";
    public static final String DEFAULT_RABBIT_ADMIN_BEAN_NAME = "defaultRabbitAdmin";
    public static final String RABBIT_ADMIN_SUFFIX = "-admin";
    public static final String DEFAULT_CONTAINER_FACTORY_BEAN_NAME = "rabbitListenerContainerFactory";

    private MultiRabbitConstants() {
    }

}
