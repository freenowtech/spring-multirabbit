package springframework.boot.autoconfigure.amqp;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SimpleResourceHolder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.MultiRabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.MultiRabbitConnectionFactoryWrapper;
import org.springframework.boot.autoconfigure.amqp.MultiRabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@EnableRabbit
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExternalConfigurationTest.Config.class)
public class ExternalConfigurationTest
{

    private static final String CONNECTION_KEY = "externalConnectionKey";
    private static final ConnectionFactory CONNECTION_FACTORY = mock(ConnectionFactory.class);
    private static final SimpleRabbitListenerContainerFactory CONTAINER_FACTORY = mock(SimpleRabbitListenerContainerFactory.class);
    private static final RabbitAdmin RABBIT_ADMIN = mock(RabbitAdmin.class);
    private static final ConnectionFactory DEFAULT_CONNECTION_FACTORY = mock(ConnectionFactory.class);

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private MultiRabbitProperties multiRabbitProperties;


    @After
    public void after()
    {
        // For the sake of simplicity, the mocks are static so as to be shared between classes.
        // Thus, they need to reset after using, to avoid interferences on the next test.
        reset(CONNECTION_FACTORY, CONTAINER_FACTORY, RABBIT_ADMIN);
    }


    @Test
    public void shouldResolveDefaultExternalConnectionFactory()
    {
        connectionFactory.getVirtualHost();
        verify(DEFAULT_CONNECTION_FACTORY).getVirtualHost();
    }


    @Test
    public void shouldResolveExternalConnectionFactory()
    {
        SimpleResourceHolder.bind(connectionFactory, CONNECTION_KEY);
        connectionFactory.getVirtualHost();
        SimpleResourceHolder.unbind(connectionFactory);
        verify(CONNECTION_FACTORY).getVirtualHost();
    }


    @Test
    public void shouldResolveExistentConnectionFactoriesFromMulti()
    {
        multiRabbitProperties.getConnections().keySet().forEach(key -> {
            SimpleResourceHolder.bind(connectionFactory, key);
            connectionFactory.getVirtualHost();
            SimpleResourceHolder.unbind(connectionFactory);
        });
        assertMocksNotTouched();
    }


    private void assertMocksNotTouched()
    {
        verify(CONNECTION_FACTORY, never()).getVirtualHost();
        verifyZeroInteractions(CONTAINER_FACTORY);
        verifyZeroInteractions(RABBIT_ADMIN);
    }


    @Configuration
    @Import(MultiRabbitAutoConfiguration.class)
    static class Config
    {

        @Bean
        @ConditionalOnClass(MultiRabbitConnectionFactoryWrapper.class)
        static MultiRabbitConnectionFactoryWrapper externalWrapper()
        {
            MultiRabbitConnectionFactoryWrapper wrapper = new MultiRabbitConnectionFactoryWrapper();
            wrapper.addConnectionFactory(CONNECTION_KEY, CONNECTION_FACTORY, CONTAINER_FACTORY, RABBIT_ADMIN);
            wrapper.setDefaultConnectionFactory(DEFAULT_CONNECTION_FACTORY);
            return wrapper;
        }

    }

}
