package org.springframework.amqp.rabbit.annotation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.config.RabbitListenerConfigUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MultiRabbitBootstrapConfigurationTest {

    private final MultiRabbitBootstrapConfiguration configuration = new MultiRabbitBootstrapConfiguration();

    @Mock
    private BeanDefinitionRegistry registry;

    @Test
    void shouldCreateMultiRabbitListenerAnnotationBeanPostProcessorBean() {
        configuration.registerBeanDefinitions(null, registry);

        verify(registry).registerBeanDefinition(
                RabbitListenerConfigUtils.RABBIT_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME,
                new RootBeanDefinition(MultiRabbitListenerAnnotationBeanPostProcessor.class));
    }
}
