package org.springframework.boot.autoconfigure.amqp;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.annotation.MultiRabbitBootstrapConfiguration;
import org.springframework.amqp.rabbit.config.AbstractRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionNameStrategy;
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * Class responsible for auto-configuring the necessary beans to enable multiple RabbitMQ servers.
 *
 * @author Wander Costa
 */
@Configuration
@ConditionalOnClass({RabbitTemplate.class, Channel.class})
@EnableConfigurationProperties({RabbitProperties.class, MultiRabbitProperties.class})
@Import({MultiRabbitBootstrapConfiguration.class, RabbitAnnotationDrivenConfiguration.class})
public class MultiRabbitAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiRabbitAutoConfiguration.class);

    /**
     * Creates an {@link AmqpAdmin}.
     *
     * @param connectionFactory The {@link ConnectionFactory} to be associated to.
     * @return an {@link AmqpAdmin}.
     */
    @Bean(MultiRabbitConstants.DEFAULT_RABBIT_ADMIN_BEAN_NAME)
    @Primary
    @ConditionalOnSingleCandidate(ConnectionFactory.class)
    @ConditionalOnProperty(prefix = "spring.rabbitmq", name = "dynamic", matchIfMissing = true)
    public AmqpAdmin amqpAdmin(final ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    /**
     * Returns a {@link RabbitAutoConfiguration.RabbitConnectionFactoryCreator}.
     *
     * @return a {@link RabbitAutoConfiguration.RabbitConnectionFactoryCreator}.
     */
    @Primary
    @Bean(MultiRabbitConstants.CONNECTION_FACTORY_CREATOR_BEAN_NAME)
    public RabbitAutoConfiguration.RabbitConnectionFactoryCreator rabbitConnectionFactoryCreator() {
        return new RabbitAutoConfiguration.RabbitConnectionFactoryCreator();
    }

    /**
     * Class to set up multirabbit beans.
     */
    @Configuration
    @DependsOn(MultiRabbitConstants.CONNECTION_FACTORY_CREATOR_BEAN_NAME)
    protected static class MultiRabbitConnectionFactoryCreator implements BeanFactoryAware, ApplicationContextAware {

        private ConfigurableListableBeanFactory beanFactory;
        private ApplicationContext applicationContext;
        private final RabbitAutoConfiguration.RabbitConnectionFactoryCreator springFactoryCreator;
        private final ObjectProvider<ConnectionNameStrategy> connectionNameStrategy;

        /**
         * Creates a new MultiRabbitConnectionFactoryCreator for instantiation of beans.
         *
         * @param springFactoryCreator   The RabbitConnectionFactoryCreator.
         * @param connectionNameStrategy The connection name strategy.
         */
        MultiRabbitConnectionFactoryCreator(
                final RabbitAutoConfiguration.RabbitConnectionFactoryCreator springFactoryCreator,
                final ObjectProvider<ConnectionNameStrategy> connectionNameStrategy) {
            this.springFactoryCreator = springFactoryCreator;
            this.connectionNameStrategy = connectionNameStrategy;
        }

        /**
         * Creates the bean of the wrapper.
         *
         * @param connectionFactory The connection factory.
         * @return The wrapper.
         */
        @Bean
        public ConnectionFactoryContextWrapper contextWrapper(final ConnectionFactory connectionFactory) {
            return new ConnectionFactoryContextWrapper(connectionFactory);
        }

        /**
         * Returns the empty wrapper if non is provided.
         *
         * @return  the empty wrapper if non is provided.
         */
        @Bean
        @ConditionalOnMissingBean
        public MultiRabbitConnectionFactoryWrapper externalEmptyWrapper() {
            return new MultiRabbitConnectionFactoryWrapper();
        }

        /**
         * Returns the routing connection factory populated with the connection factories provided from configuration.
         *
         * @param rabbitProperties      The default rabbit properties.
         * @param multiRabbitProperties The additional rabbit properties.
         * @param externalWrapper       The external wrapper for integration.
         * @return The routing connection factory.
         */
        @Primary
        @Bean(MultiRabbitConstants.CONNECTION_FACTORY_BEAN_NAME)
        public ConnectionFactory routingConnectionFactory(
                final RabbitProperties rabbitProperties,
                final MultiRabbitProperties multiRabbitProperties,
                final MultiRabbitConnectionFactoryWrapper externalWrapper) {
            final MultiRabbitConnectionFactoryWrapper internalWrapper
                    = instantiateConnectionFactories(rabbitProperties, multiRabbitProperties);
            final MultiRabbitConnectionFactoryWrapper aggregatedWrapper
                    = aggregateConnectionFactoryWrappers(internalWrapper, externalWrapper);

            aggregatedWrapper.getContainerFactories().forEach(this::registerContainerFactoryBean);
            aggregatedWrapper.getRabbitAdmins().forEach(this::registerRabbitAdminBean);

            final SimpleRoutingConnectionFactory connectionFactory = new SimpleRoutingConnectionFactory();
            connectionFactory.setTargetConnectionFactories(aggregatedWrapper.getConnectionFactories());
            connectionFactory.setDefaultTargetConnectionFactory(aggregatedWrapper.getDefaultConnectionFactory());
            return connectionFactory;
        }

        /**
         * Returns an aggregated view of two {@link MultiRabbitConnectionFactoryWrapper}, in which
         * {@code externalWrapper} has higher precedence and will be preferred in case of clash of keys
         * or in the presence of the default connection factory.
         */
        private MultiRabbitConnectionFactoryWrapper aggregateConnectionFactoryWrappers(
                final MultiRabbitConnectionFactoryWrapper internalWrapper,
                final MultiRabbitConnectionFactoryWrapper externalWrapper) {
            final MultiRabbitConnectionFactoryWrapper aggregatedWrapper = new MultiRabbitConnectionFactoryWrapper();
            copyConnectionSets(aggregatedWrapper, internalWrapper);
            copyConnectionSets(aggregatedWrapper, externalWrapper);

            aggregatedWrapper.setDefaultConnectionFactory(externalWrapper.getDefaultConnectionFactory() != null
                    ? externalWrapper.getDefaultConnectionFactory()
                    : internalWrapper.getDefaultConnectionFactory());
            return aggregatedWrapper;
        }

        /**
         * Copies the connection sets from a source wrapper to the aggregated wrapper.
         */
        private void copyConnectionSets(
                final MultiRabbitConnectionFactoryWrapper aggregatedWrapper,
                final MultiRabbitConnectionFactoryWrapper sourceWrapper) {
            sourceWrapper.getConnectionFactories().forEach((key, value) -> aggregatedWrapper.addConnectionFactory(
                    String.valueOf(key),
                    sourceWrapper.getConnectionFactories().get(key),
                    sourceWrapper.getContainerFactories().get(key),
                    sourceWrapper.getRabbitAdmins().get(key)));
        }

        /**
         * Returns an internal wrapper with connection factories initialized.
         */
        private MultiRabbitConnectionFactoryWrapper instantiateConnectionFactories(
                final RabbitProperties rabbitProperties,
                final MultiRabbitProperties multiRabbitProperties) {
            final MultiRabbitConnectionFactoryWrapper wrapper = new MultiRabbitConnectionFactoryWrapper();

            final Map<String, RabbitProperties> propertiesMap = multiRabbitProperties != null
                    ? multiRabbitProperties.getConnections()
                    : Collections.emptyMap();

            propertiesMap.forEach((key, value) -> {
                CachingConnectionFactory connectionFactory = instantiateConnectionFactory(value);
                SimpleRabbitListenerContainerFactory containerFactory = newContainerFactory(connectionFactory);
                RabbitAdmin rabbitAdmin = newRabbitAdmin(connectionFactory);
                wrapper.addConnectionFactory(key, connectionFactory, containerFactory, rabbitAdmin);
            });

            final String defaultConnectionFactoryKey = multiRabbitProperties != null
                    ? multiRabbitProperties.getDefaultConnection()
                    : null;

            if (StringUtils.hasText(defaultConnectionFactoryKey)
                    && !multiRabbitProperties.getConnections().containsKey(defaultConnectionFactoryKey)) {
                String msg = String.format("MultiRabbitMQ broker '%s' set as default does "
                        + "not exist in configuration", defaultConnectionFactoryKey);
                LOGGER.error(msg);
                throw new IllegalArgumentException(msg);
            }

            final ConnectionFactory defaultConnectionFactory = StringUtils.hasText(defaultConnectionFactoryKey)
                    ? wrapper.getConnectionFactories().get(defaultConnectionFactoryKey)
                    : instantiateConnectionFactory(rabbitProperties);
            wrapper.setDefaultConnectionFactory(defaultConnectionFactory);

            return wrapper;
        }

        /**
         * Registers the ContainerFactory bean.
         */
        private SimpleRabbitListenerContainerFactory newContainerFactory(final ConnectionFactory connectionFactory) {
            SimpleRabbitListenerContainerFactory containerFactory = new SimpleRabbitListenerContainerFactory();
            containerFactory.setConnectionFactory(connectionFactory);
            return containerFactory;
        }

        /**
         * Register the RabbitAdmin bean (to enable context changing with Rabbit annotations).
         */
        private RabbitAdmin newRabbitAdmin(final ConnectionFactory connectionFactory) {
            RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
            rabbitAdmin.setApplicationContext(applicationContext);
            rabbitAdmin.afterPropertiesSet();
            return rabbitAdmin;
        }

        /**
         * Method to call default Spring factory creator and suppress a possible Exception into a RuntimeException. The
         * suppression of the Exception will not affect the flow, since Spring will still stop its initialization in the
         * event of any RuntimeException.
         */
        private CachingConnectionFactory instantiateConnectionFactory(final RabbitProperties rabbitProperties) {
            try {
                return springFactoryCreator.rabbitConnectionFactory(rabbitProperties, connectionNameStrategy);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        /**
         * Registers the ContainerFactory bean.
         */
        private void registerContainerFactoryBean(final String name,
                                                  final AbstractRabbitListenerContainerFactory containerFactory) {
            beanFactory.registerSingleton(name, containerFactory);
        }

        /**
         * Register the RabbitAdmin bean (to enable context changing with Rabbit annotations).
         */
        private void registerRabbitAdminBean(final String name, final RabbitAdmin rabbitAdmin) {
            rabbitAdmin.setApplicationContext(applicationContext);
            rabbitAdmin.afterPropertiesSet();
            beanFactory.registerSingleton(name + MultiRabbitConstants.RABBIT_ADMIN_SUFFIX, rabbitAdmin);
        }

        @Override
        public void setBeanFactory(final BeanFactory beanFactory) {
            this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
        }

        @Override
        public void setApplicationContext(final ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }
    }
}
