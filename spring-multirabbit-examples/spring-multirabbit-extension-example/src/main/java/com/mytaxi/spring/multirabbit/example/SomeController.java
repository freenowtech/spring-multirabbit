package com.mytaxi.spring.multirabbit.example;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.amqp.ConnectionFactoryContextWrapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.util.StringUtils.isEmpty;

@RestController
class SomeController
{

    private static final String CONNECTION_PREFIX = "connectionName";
    private static final String EXCHANGE_NAME = "sampleExchange";
    private static final String ROUTING_KEY = "sampleRoutingKey";

    private final RabbitTemplate rabbitTemplate;
    private final ConnectionFactoryContextWrapper contextWrapper;


    SomeController(RabbitTemplate rabbitTemplate, ConnectionFactoryContextWrapper contextWrapper)
    {
        this.rabbitTemplate = rabbitTemplate;
        this.contextWrapper = contextWrapper;
    }


    /**
     * An example of the use of RabbitTemplate, changing between different Rabbit connections.
     *
     * @param message The message to be sent to the Rabbit server.
     * @param id      The id of the connection as per defined in the configuration.
     */
    @PostMapping
    void sendMessage(@RequestBody String message, String id)
    {
        String idWithPrefix = !isEmpty(id) ? CONNECTION_PREFIX + id : null;
        contextWrapper.run(idWithPrefix, () -> {
            String exchange = EXCHANGE_NAME + emptyIfNull(id);
            String routingKey = ROUTING_KEY + emptyIfNull(id);

            // Regular use of RabbitTemplate
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
        });
    }


    private String emptyIfNull(String id)
    {
        return id != null ? id : "";
    }

}