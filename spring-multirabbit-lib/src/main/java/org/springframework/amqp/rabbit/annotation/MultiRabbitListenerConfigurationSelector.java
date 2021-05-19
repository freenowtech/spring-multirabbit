package org.springframework.amqp.rabbit.annotation;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;

/**
 * A {@link DeferredImportSelector} implementation with the lowest order to import a
 * {@link MultiRabbitBootstrapConfiguration} as late as possible.
 */
@Order
public class MultiRabbitListenerConfigurationSelector implements DeferredImportSelector {

    @Override
    public String[] selectImports(final AnnotationMetadata importingClassMetadata) {
        return new String[] {MultiRabbitBootstrapConfiguration.class.getName()};
    }
}
