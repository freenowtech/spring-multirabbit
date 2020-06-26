package org.springframework.amqp.rabbit.annotation;

import org.springframework.amqp.rabbit.config.RabbitListenerConfigUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * An {@link ImportBeanDefinitionRegistrar} class that registers
 * a {@link MultiRabbitListenerAnnotationBeanPostProcessor} (overwriting the regular
 * {@link RabbitListenerAnnotationBeanPostProcessor}) bean capable of processing
 * Spring's @{@link RabbitListener} annotation.
 *
 * @author Wander Costa
 * @see MultiRabbitListenerAnnotationBeanPostProcessor
 * @see RabbitListenerAnnotationBeanPostProcessor
 * @see org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry
 * @see EnableRabbit
 */
public class MultiRabbitBootstrapConfiguration implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(final AnnotationMetadata importingClassMetadata,
                                        final BeanDefinitionRegistry registry) {
        registry.registerBeanDefinition(RabbitListenerConfigUtils.RABBIT_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME,
                new RootBeanDefinition(MultiRabbitListenerAnnotationBeanPostProcessor.class));
    }
}
