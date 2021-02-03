package springframework.boot.autoconfigure.amqp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.amqp.MultiRabbitAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * {@link MultiRabbitAutoConfiguration} is normally triggered before the processing of the Listeners by the
 * {@link org.springframework.amqp.rabbit.annotation.MultiRabbitListenerAnnotationBeanPostProcessor}. However, this does
 * not happen whenever there is no injection of {@link org.springframework.amqp.rabbit.connection.ConnectionFactory}.
 * This test makes sure to test MultiRabbit without the injection of a RabbitTemplate as a workaround for the
 * initialization.
 */
@EnableRabbit
@RunWith(SpringRunner.class)
@SuppressWarnings("EmptyMethod")
@SpringBootTest(classes = MultiRabbitAutoConfiguration.class)
public class AutoConfigInitializationTest {

    @Test
    public void shouldStartContextWithoutConnectionFactory() {
    }

    @Component
    @EnableRabbit
    private static class ListenerBeans {

        @RabbitListener(bindings = @QueueBinding(
                exchange = @Exchange("exchange0"),
                value = @Queue(),
                key = "routingKey0"))
        void listenBroker0(final String message) {
        }

        @RabbitListener(containerFactory = "broker1", bindings = @QueueBinding(
                exchange = @Exchange("exchange1"),
                value = @Queue(),
                key = "routingKey1"))
        void listenBroker1(final String message) {
        }

        @RabbitListener(containerFactory = "broker2", bindings = @QueueBinding(
                exchange = @Exchange("exchange2"),
                value = @Queue(),
                key = "routingKey2"))
        void listenBroker2(final String message) {
        }
    }

    /**
     * Configuration to provide 3 brokers.
     */
    @Configuration
    @PropertySource("classpath:application-three-brokers.properties")
    public static class ThreeBrokersConfig {
    }
}
