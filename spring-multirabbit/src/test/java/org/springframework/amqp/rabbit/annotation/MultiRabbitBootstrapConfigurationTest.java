package org.springframework.amqp.rabbit.annotation;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.config.RabbitListenerConfigUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.amqp.MultiRabbitConstants;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
class MultiRabbitBootstrapConfigurationTest {

    private MultiRabbitBootstrapConfiguration configuration = new MultiRabbitBootstrapConfiguration();

    @Mock
    private BeanDefinitionRegistry registry;

    @Mock
    private Environment environment;

    @BeforeEach
    void beforeEach() {
        configuration.setEnvironment(environment);
    }

    @Test
    @DisplayName("should process MultiRabbitBootstrapConfiguration if enabled")
    void shouldCreateMultiRabbitListenerAnnotationBeanPostProcessorBean() {
        when(environment.getProperty(MultiRabbitConstants.MULTI_RABBIT_ENABLED_PROPERTY)).thenReturn("true");

        configuration.registerBeanDefinitions(null, registry);

        verify(registry).registerBeanDefinition(
                RabbitListenerConfigUtils.RABBIT_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME,
                new RootBeanDefinition(ExtendedMultiRabbitListenerAnnotationBeanPostProcessor.class));
    }

    @Test
    @DisplayName("should not process MultiRabbitBootstrapConfiguration if disabled")
    void shouldNotCreateMultiRabbitListenerAnnotationBeanPostProcessorBean() {
        configuration.registerBeanDefinitions(null, registry);

        verify(registry, never()).registerBeanDefinition(
                RabbitListenerConfigUtils.RABBIT_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME,
                new RootBeanDefinition(ExtendedMultiRabbitListenerAnnotationBeanPostProcessor.class));
    }
}
