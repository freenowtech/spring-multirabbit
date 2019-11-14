package com.mytaxi.spring.multirabbit.example

import org.springframework.amqp.rabbit.connection.SimpleResourceHolder
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.boot.autoconfigure.amqp.ConnectionFactoryContextWrapper
import org.springframework.util.StringUtils.isEmpty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SomeController(var rabbitTemplate: RabbitTemplate,
                     var contextWrapper: ConnectionFactoryContextWrapper) {

    companion object {
        const val CONNECTION_PREFIX = "connectionName"
        const val EXCHANGE_NAME = "sampleExchange"
        const val ROUTING_KEY = "sampleRoutingKey"
    }

    /**
     * An example of the use of RabbitTemplate, changing between different Rabbit connections.
     *
     * @param message The message to be sent to the Rabbit server.
     * @param id      The id of the connection as per defined in the configuration.
     */
    @PostMapping
    fun sendMessage(@RequestBody message: String, id: String?, useContextWrapper: Boolean) {
        if (useContextWrapper) {
            sendMessageUsingContextWrapper(message, id)
        } else {
            sendMessageTheDefaultWay(message, id)
        }
    }

    /**
     * Sends a message using the default Spring implementation.
     */
    private fun sendMessageTheDefaultWay(message: String, id: String?) {
        // Binding to the right context of Rabbit ConnectionFactory
        if (!isEmpty(id)) {
            SimpleResourceHolder.bind(rabbitTemplate.connectionFactory, CONNECTION_PREFIX + emptyIfNull(id))
        }

        val exchange = EXCHANGE_NAME + emptyIfNull(id)
        val routingKey = ROUTING_KEY + emptyIfNull(id)

        // Regular use of RabbitTemplate
        rabbitTemplate.convertAndSend(exchange, routingKey, message)

        // Unbinding the context of Rabbit ConnectionFactory
        if (!isEmpty(id)) {
            SimpleResourceHolder.unbind(rabbitTemplate.connectionFactory)
        }
    }

    /**
     * Sends a message using the context wrapper.
     */
    private fun sendMessageUsingContextWrapper(message: String, id: String?) {
        val idWithPrefix = if (!isEmpty(id)) CONNECTION_PREFIX + id else null
        contextWrapper.run(idWithPrefix, {
            val exchange = EXCHANGE_NAME + emptyIfNull(id)
            val routingKey = ROUTING_KEY + emptyIfNull(id)

            // Regular use of RabbitTemplate
            rabbitTemplate.convertAndSend(exchange, routingKey, message)
        })
    }

    private fun emptyIfNull(id: String?): String {
        return id ?: ""
    }
}
