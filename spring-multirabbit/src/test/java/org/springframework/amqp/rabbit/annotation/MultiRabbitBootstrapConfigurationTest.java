package org.springframework.amqp.rabbit.annotation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.config.RabbitListenerConfigUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.env.Environment;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiRabbitBootstrapConfigurationTest {
    @InjectMocks
    private final MultiRabbitBootstrapConfiguration configuration = new MultiRabbitBootstrapConfiguration();

    @Mock
    private BeanDefinitionRegistry registry;

    @Mock
    private Environment environment;

    @Test
    void shouldCreateMultiRabbitListenerAnnotationBeanPostProcessorBean() {
        when(environment.getProperty(RabbitListenerConfigUtils.MULTI_RABBIT_ENABLED_PROPERTY)).thenReturn("true");

        configuration.registerBeanDefinitions(null, registry);

        verify(registry).registerBeanDefinition(
            RabbitListenerConfigUtils.RABBIT_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME,
            new RootBeanDefinition(MultiRabbitListenerAnnotationBeanPostProcessor.class));
    }
}
