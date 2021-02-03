package springframework.boot.autoconfigure.amqp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.MultiRabbitListenerAnnotationBeanPostProcessor;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.ConnectionFactoryContextWrapper;
import org.springframework.boot.autoconfigure.amqp.MultiRabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.MultiRabbitConstants;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@EnableRabbit
@RunWith(SpringRunner.class)
@SuppressWarnings("EmptyMethod")
@SpringBootTest(classes = MultiRabbitAutoConfiguration.class)
public class BeansValidationTest {

    private static final String CONNECTION_A = "connectionNameA";
    private static final String CONNECTION_B = "connectionNameB";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private RabbitListenerAnnotationBeanPostProcessor rabbitListenerAnnotationBeanPostProcessor;

    @Test
    public void shouldResolveSimpleRoutingConnectionFactoryBean() {
        assertTrue(connectionFactory instanceof SimpleRoutingConnectionFactory);
    }

    @Test
    public void shouldResolveContainerFactoryBeans() {
        final List<String> beans = Arrays.asList(applicationContext
                .getBeanNamesForType(SimpleRabbitListenerContainerFactory.class));
        assertTrue(beans.containsAll(Arrays.asList("rabbitListenerContainerFactory", CONNECTION_A, CONNECTION_B)));
        beans.forEach(bean -> assertNotNull(applicationContext
                .getBean(bean, SimpleRabbitListenerContainerFactory.class)));
    }

    @Test
    public void shouldResolveConnectionFactoryContextWrapper() {
        assertNotNull(applicationContext.getBean(ConnectionFactoryContextWrapper.class));
    }

    @Test
    public void shouldResolveRabbitAdminBeans() {
        final List<String> beans = Arrays.asList(applicationContext.getBeanNamesForType(RabbitAdmin.class));
        assertTrue(beans.containsAll(Arrays.asList(
                MultiRabbitConstants.DEFAULT_RABBIT_ADMIN_BEAN_NAME,
                CONNECTION_A + MultiRabbitConstants.RABBIT_ADMIN_SUFFIX,
                CONNECTION_B + MultiRabbitConstants.RABBIT_ADMIN_SUFFIX)));
        beans.forEach(bean -> assertNotNull(applicationContext.getBean(bean, RabbitAdmin.class)));
    }

    @Test
    public void shouldResolveExtendedRabbitListenerAnnotationBeanPostProcessor() {
        assertTrue(rabbitListenerAnnotationBeanPostProcessor
                instanceof MultiRabbitListenerAnnotationBeanPostProcessor);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("queue"),
            exchange = @Exchange("exchange"),
            key = "key"))
    void listen() {
    }

    @RabbitListener(containerFactory = CONNECTION_A, bindings = @QueueBinding(
            value = @Queue("queue"),
            exchange = @Exchange("exchange"),
            key = "key"))
    void listenConnectionNameA() {
    }

    @RabbitListener(containerFactory = CONNECTION_B, bindings = @QueueBinding(
            value = @Queue("queue"),
            exchange = @Exchange("exchange"),
            key = "key"))
    void listenConnectionNameB() {
    }
}
