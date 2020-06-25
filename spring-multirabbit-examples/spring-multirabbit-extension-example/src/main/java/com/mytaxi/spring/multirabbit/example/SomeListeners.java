package com.mytaxi.spring.multirabbit.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
class SomeListeners {

    private static final Logger LOG = LoggerFactory.getLogger(SomeListeners.class);

    static final String CONNECTION_A = "connectionNameA";
    static final String CONNECTION_B = "connectionNameB";
    static final String EXTENDED_CONNECTION_A = "extendedConnectionNameA";
    static final String EXTENDED_CONNECTION_B = "extendedConnectionNameB";

    private static final String SAMPLE_EXCHANGE = "sampleExchange";
    private static final String SAMPLE_ROUTING_KEY = "sampleRoutingKey";
    private static final String SAMPLE_QUEUE = "sampleQueue";

    private static final String SAMPLE_EXCHANGE_A = SAMPLE_EXCHANGE + "A";
    private static final String SAMPLE_ROUTING_KEY_A = SAMPLE_ROUTING_KEY + "A";
    private static final String SAMPLE_QUEUE_A = SAMPLE_QUEUE + "A";

    private static final String SAMPLE_EXCHANGE_B = SAMPLE_EXCHANGE + "B";
    private static final String SAMPLE_ROUTING_KEY_B = SAMPLE_ROUTING_KEY + "B";
    private static final String SAMPLE_QUEUE_B = SAMPLE_QUEUE + "B";

    private static final String SAMPLE_EXT_EXCHANGE = "sampleExtendedExchange";
    private static final String SAMPLE_EXT_ROUTING_KEY = "sampleExtendedRoutingKey";
    private static final String SAMPLE_EXT_QUEUE = "sampleExtendedQueue";

    private static final String SAMPLE_EXT_EXCHANGE_A = SAMPLE_EXT_EXCHANGE + "A";
    private static final String SAMPLE_EXT_ROUTING_KEY_A = SAMPLE_EXT_ROUTING_KEY + "A";
    private static final String SAMPLE_EXT_QUEUE_A = SAMPLE_EXT_QUEUE + "A";

    private static final String SAMPLE_EXT_EXCHANGE_B = SAMPLE_EXT_EXCHANGE + "B";
    private static final String SAMPLE_EXT_ROUTING_KEY_B = SAMPLE_EXT_ROUTING_KEY + "B";
    private static final String SAMPLE_EXT_QUEUE_B = SAMPLE_EXT_QUEUE + "B";

    /**
     * First listener, listening to the default Rabbit context (connection provided from one of the configurations of
     * 'spring.multirabbitmq' and defined as 'defaultConnection=true' or from the default 'spring.rabbitmq'
     * configuration.
     *
     * @param message the message received.
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(SAMPLE_QUEUE),
            exchange = @Exchange(SAMPLE_EXCHANGE),
            key = SAMPLE_ROUTING_KEY))
    void listen(final String message) {
        LOG.info("Default Listener: {}", message);
    }

    /**
     * Second listener, listening to the specific context 'connectionNameA', which is provided from the configuration
     * 'spring.multirabbitmq.connections.connectionNameA'.
     *
     * @param message the message received.
     */
    @RabbitListener(containerFactory = CONNECTION_A,
            bindings = @QueueBinding(
                    value = @Queue(SAMPLE_QUEUE_A),
                    exchange = @Exchange(SAMPLE_EXCHANGE_A),
                    key = SAMPLE_ROUTING_KEY_A))
    void listenConnectionNameA(final String message) {
        LOG.info("Listener 'connectionNameA': {}", message);
    }

    /**
     * Third listener, listening to the specific context 'connectionNameB', which is provided from the configuration
     * 'spring.multirabbitmq.connections.connectionNameB'.
     *
     * @param message the message received.
     */
    @RabbitListener(containerFactory = CONNECTION_B,
            bindings = @QueueBinding(
                    value = @Queue(SAMPLE_QUEUE_B),
                    exchange = @Exchange(SAMPLE_EXCHANGE_B),
                    key = SAMPLE_ROUTING_KEY_B))
    public void listenConnectionNameB(final String message) {
        LOG.info("Listener 'connectionNameB': {}", message);
    }

    /**
     * Third listener, listening to the specific context 'connectionNameB', which is provided from the configuration
     * 'spring.multirabbitmq.connections.connectionNameB'.
     *
     * @param message the message received.
     */
    @RabbitListener(containerFactory = EXTENDED_CONNECTION_A, bindings = @QueueBinding(
            value = @Queue(SAMPLE_EXT_QUEUE_A),
            exchange = @Exchange(SAMPLE_EXT_EXCHANGE_A),
            key = SAMPLE_EXT_ROUTING_KEY_A))
    public void listenExtendedConnectionNameA(final String message) {
        LOG.info("Listener 'extendedA': {}", message);
    }

    /**
     * Third listener, listening to the specific context 'connectionNameB', which is provided from the configuration
     * 'spring.multirabbitmq.connections.connectionNameB'.
     *
     * @param message the message received.
     */
    @RabbitListener(containerFactory = EXTENDED_CONNECTION_B, bindings = @QueueBinding(
            value = @Queue(SAMPLE_EXT_QUEUE_B),
            exchange = @Exchange(SAMPLE_EXT_EXCHANGE_B),
            key = SAMPLE_EXT_ROUTING_KEY_B))
    public void listenExtendedConnectionNameB(final String message) {
        LOG.info("Listener 'extendedB': {}", message);
    }
}
