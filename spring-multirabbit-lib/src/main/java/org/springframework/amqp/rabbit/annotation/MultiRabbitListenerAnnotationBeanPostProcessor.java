package org.springframework.amqp.rabbit.annotation;

import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;

/**
 * An extension of {@link RabbitListenerAnnotationBeanPostProcessor} that attaches the processing of beans for
 * Exchanges, Queues, and Bindings after they are created.
 * <p>
 * This processing enables each {@link RabbitAdmin} to differentiate which beans corresponds to that specific
 * {@link RabbitAdmin}, preventing the server from being populated with non-expected structures from other servers.
 *
 * @author Wander Costa
 * @see RabbitListenerAnnotationBeanPostProcessor
 */
public final class MultiRabbitListenerAnnotationBeanPostProcessor
        extends RabbitListenerAnnotationBeanPostProcessor
        implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Override
    protected void processAmqpListener(final RabbitListener rabbitListener,
                                       final Method method,
                                       final Object bean,
                                       final String beanName) {
        super.processAmqpListener(rabbitListener, method, bean, beanName);
        enhanceBeansWithReferenceToRabbitAdmin(rabbitListener);
    }

    /**
     * Enhance beans with related RabbitAdmin, so as to be filtered when being processed by the RabbitAdmin.
     */
    private void enhanceBeansWithReferenceToRabbitAdmin(final RabbitListener rabbitListener) {
        final String rabbitAdmin = RabbitAdminNameResolver.resolve(rabbitListener);

        // Enhance Exchanges
        applicationContext.getBeansOfType(AbstractExchange.class).values().stream()
                .filter(this::isNotProcessed)
                .forEach(exchange -> exchange.setAdminsThatShouldDeclare(rabbitAdmin));

        // Enhance Queues
        applicationContext.getBeansOfType(Queue.class).values().stream()
                .filter(this::isNotProcessed)
                .forEach(queue -> queue.setAdminsThatShouldDeclare(rabbitAdmin));

        // Enhance Bindings
        applicationContext.getBeansOfType(Binding.class).values().stream()
                .filter(this::isNotProcessed)
                .forEach(binding -> binding.setAdminsThatShouldDeclare(rabbitAdmin));
    }

    /**
     * Verifies the presence of an instance of RabbitAdmin or this object, as fallback.
     */
    private boolean isNotProcessed(final Declarable declarable) {
        return declarable.getDeclaringAdmins() == null
                || (declarable.getDeclaringAdmins().stream().noneMatch(item -> item == this)
                && declarable.getDeclaringAdmins().stream().noneMatch(item -> item instanceof RabbitAdmin));
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
