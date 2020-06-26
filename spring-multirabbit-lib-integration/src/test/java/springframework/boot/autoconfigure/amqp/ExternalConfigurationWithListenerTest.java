package springframework.boot.autoconfigure.amqp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.MultiRabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.MultiRabbitConnectionFactoryWrapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ExternalConfigurationWithListenerTest {

    private static final String CONNECTION_KEY = "externalConnectionKey";
    private static final int DEFAULT_CF_PORT = 10897;
    private static final int EXTERNAL_CF_PORT = 20209;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void shouldLoadDefaultConnectionFactory() {
        final ConnectionFactory connectionFactory
            = applicationContext.getBean(SimpleRoutingConnectionFactory.class);
        assertEquals(DEFAULT_CF_PORT, connectionFactory.getPort());
    }

    @Test
    public void shouldLoadExternalConnectionFactory() {
        final SimpleRoutingConnectionFactory connectionFactory
            = applicationContext.getBean(SimpleRoutingConnectionFactory.class);
        final ConnectionFactory externalConnectionFactory
            = connectionFactory.getTargetConnectionFactory(CONNECTION_KEY);
        assertNotNull(externalConnectionFactory);
        assertEquals(EXTERNAL_CF_PORT, externalConnectionFactory.getPort());
    }

    @SpringBootApplication
    @Import({MultiRabbitAutoConfiguration.class})
    public static class SingleListenerApp {

        @RabbitListener
        public void onMessageDefaultConnectionFactory(final String event) {
        }

        @RabbitListener(containerFactory = CONNECTION_KEY)
        public void onMessageExternalConnectionFactory(final String event) {
        }

        @Bean
        @ConditionalOnClass(MultiRabbitConnectionFactoryWrapper.class)
        static MultiRabbitConnectionFactoryWrapper externalWrapper() {
            final ConnectionFactory defaultConnectionFactory = new CachingConnectionFactory(DEFAULT_CF_PORT);

            final ConnectionFactory connectionFactory = new CachingConnectionFactory(EXTERNAL_CF_PORT);
            final SimpleRabbitListenerContainerFactory containerFactory =
                new SimpleRabbitListenerContainerFactory();
            final RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);

            final MultiRabbitConnectionFactoryWrapper wrapper = new MultiRabbitConnectionFactoryWrapper();
            wrapper.addConnectionFactory(CONNECTION_KEY, connectionFactory, containerFactory, rabbitAdmin);
            wrapper.setDefaultConnectionFactory(defaultConnectionFactory);
            return wrapper;
        }
    }
}
