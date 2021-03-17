package org.springframework.amqp.rabbit.annotation;

import static org.springframework.amqp.rabbit.config.RabbitListenerConfigUtils.RABBIT_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME;
import static org.springframework.boot.autoconfigure.amqp.MultiRabbitConstants.MULTI_RABBIT_ENABLED_PROPERTY;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * An {@link ImportBeanDefinitionRegistrar} class that registers a
 * {@link ExtendedMultiRabbitListenerAnnotationBeanPostProcessor} (overwriting the regular
 * {@link RabbitListenerAnnotationBeanPostProcessor}) bean capable of processing Spring's @{@link RabbitListener}
 * annotation.
 *
 * @author Wander Costa
 * @see ExtendedMultiRabbitListenerAnnotationBeanPostProcessor
 * @see RabbitListenerAnnotationBeanPostProcessor
 * @see org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry
 * @see EnableRabbit
 */
public class MultiRabbitBootstrapConfiguration implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Environment environment;

    @Override
    public void registerBeanDefinitions(final AnnotationMetadata importingClassMetadata,
                                        final BeanDefinitionRegistry registry) {
        if (isMultiRabbitEnabled()) {
            if (registry.containsBeanDefinition(RABBIT_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME)) {
                registry.removeBeanDefinition(RABBIT_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME);
            }
            registry.registerBeanDefinition(RABBIT_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME,
                    new RootBeanDefinition(ExtendedMultiRabbitListenerAnnotationBeanPostProcessor.class));
        }
    }

    private boolean isMultiRabbitEnabled() {
        final String isMultiEnabledStr = this.environment.getProperty(MULTI_RABBIT_ENABLED_PROPERTY);
        return Boolean.parseBoolean(isMultiEnabledStr);
    }

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }
}
