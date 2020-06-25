package com.mytaxi.spring.multirabbit.example

import mu.KLogging
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class SomeListeners {

    companion object : KLogging() {
        const val CONNECTION_A: String = "connectionNameA"
        const val CONNECTION_B: String = "connectionNameB"

        const val SAMPLE_EXCHANGE: String = "sampleExchange"
        const val SAMPLE_ROUTING_KEY: String = "sampleRoutingKey"
        const val SAMPLE_QUEUE: String = "sampleQueue"

        const val SAMPLE_EXCHANGE_A: String = SAMPLE_EXCHANGE + "A"
        const val SAMPLE_ROUTING_KEY_A: String = SAMPLE_ROUTING_KEY + "A"
        const val SAMPLE_QUEUE_A: String = SAMPLE_QUEUE + "A"

        const val SAMPLE_EXCHANGE_B: String = SAMPLE_EXCHANGE + "B"
        const val SAMPLE_ROUTING_KEY_B: String = SAMPLE_ROUTING_KEY + "B"
        const val SAMPLE_QUEUE_B: String = SAMPLE_QUEUE + "B"
    }

    /**
     * First listener, listening to the default Rabbit context (connection provided from one of the configurations of
     * 'spring.multirabbitmq' and defined as 'defaultConnection=true' or from the default 'spring.rabbitmq' configuration.
     *
     * @param message the message received.
     */
    @RabbitListener(bindings = [(QueueBinding(
            value = Queue(SAMPLE_QUEUE),
            exchange = Exchange(SAMPLE_EXCHANGE),
            key = arrayOf(SAMPLE_ROUTING_KEY)))])
    fun listen(message: String) {
        logger.info("Default Listener: {}", message)
    }

    /**
     * Second listener, listening to the specific context 'connectionNameA', which is provided from the configuration
     * 'spring.multirabbitmq.connectionNameA'.
     *
     * @param message the message received.
     */
    @RabbitListener(containerFactory = CONNECTION_A, bindings = [(QueueBinding(
            value = Queue(SAMPLE_QUEUE_A),
            exchange = Exchange(SAMPLE_EXCHANGE_A),
            key = arrayOf(SAMPLE_ROUTING_KEY_A)))])
    fun listenConnectionNameA(message: String) {
        logger.info("Listener 'connectionNameA': {}", message)
    }

    /**
     * Third listener, listening to the specific context 'connectionNameB', which is provided from the configuration
     * 'spring.multirabbitmq.connectionNameB'.
     *
     * @param message the message received.
     */
    @RabbitListener(containerFactory = CONNECTION_B, bindings = [(QueueBinding(
            value = Queue(SAMPLE_QUEUE_B),
            exchange = Exchange(SAMPLE_EXCHANGE_B),
            key = arrayOf(SAMPLE_ROUTING_KEY_B)))])
    fun listenConnectionNameB(message: String) {
        logger.info("Listener 'connectionNameB': {}", message)
    }
}
