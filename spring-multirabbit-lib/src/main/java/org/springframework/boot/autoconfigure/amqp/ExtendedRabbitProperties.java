package org.springframework.boot.autoconfigure.amqp;

/**
 * Extended version of {@link RabbitProperties} providing necessary additional attributes.
 */
class ExtendedRabbitProperties extends RabbitProperties
{

    /**
     * Flag that determines the default connection.
     */
    private boolean defaultConnection = false;


    boolean isDefaultConnection()
    {
        return defaultConnection;
    }


    public void setDefaultConnection(boolean isDefault)
    {
        this.defaultConnection = isDefault;
    }

}