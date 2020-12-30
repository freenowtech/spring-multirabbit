package org.springframework.amqp.rabbit.annotation;

import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

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
        implements ApplicationContextAware, BeanFactoryAware {

    private static final String NO_ADMIN_BEAN_ERROR = "Bean '%s' for RabbitAdmin not found.";

    private ApplicationContext applicationContext;
    private BeanFactory beanFactory;

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
        RabbitAdmin rabbitAdmin = getRabbitAdminBean(rabbitListener);

        // Enhance Exchanges
        applicationContext.getBeansOfType(AbstractExchange.class).values().stream()
                          .filter(this::isNotProcessed)
                          .filter(e -> shouldBeProcessed(e, rabbitListener))
                          .forEach(exchange -> exchange.setAdminsThatShouldDeclare(rabbitAdmin != null ? rabbitAdmin : this));

        // Enhance Queues
        applicationContext.getBeansOfType(Queue.class).values().stream()
                          .filter(this::isNotProcessed)
                          .filter(q -> shouldBeProcessed(q, rabbitListener))
                          .forEach(queue -> queue.setAdminsThatShouldDeclare(rabbitAdmin != null ? rabbitAdmin : this));

        // Enhance Bindings
        applicationContext.getBeansOfType(Binding.class).values().stream()
                          .filter(this::isNotProcessed)
                          .filter(b -> shouldBeProcessed(b, rabbitListener))
                          .forEach(binding -> binding.setAdminsThatShouldDeclare(rabbitAdmin != null ? rabbitAdmin : this));
    }

    /**
     * Returns the RabbitAdmin bean of the requested name or the default one.
     */
    private RabbitAdmin getRabbitAdminBean(final RabbitListener rabbitListener) {
        String name = RabbitAdminNameResolver.resolve(rabbitListener);
        RabbitAdmin rabbitAdmin = beanFactory.getBean(name, RabbitAdmin.class);
        if(rabbitAdmin == null) {
            throw new IllegalStateException(String.format(NO_ADMIN_BEAN_ERROR, name));
        }
        return rabbitAdmin;
    }

    /**
     * Verifies the presence of an instance of RabbitAdmin or this object, as fallback.
     */
    private boolean isNotProcessed(final Declarable declarable) {
        return declarable.getDeclaringAdmins() == null
               || (declarable.getDeclaringAdmins().stream().noneMatch(item -> item == this)
                   && declarable.getDeclaringAdmins().stream().noneMatch(item -> item instanceof RabbitAdmin));
    }

    private boolean shouldBeProcessed(final AbstractExchange exchange, final RabbitListener rabbitListener) {
        return extractExchangeName(rabbitListener).contains(exchange.getName());
    }

    private Set<String> extractExchangeName(RabbitListener rabbitListener) {
        if(rabbitListener.bindings().length > 0) {
            return extractExchangeNameFromBindings(rabbitListener);
        }
        return Collections.emptySet();
    }

    private Set<String> extractExchangeNameFromBindings(RabbitListener rabbitListener) {
        return Arrays.stream(rabbitListener.bindings())
                     .filter(b -> b.declare().equals("true"))
                     .map(b -> b.exchange().name())
                     .collect(Collectors.toSet());
    }

    private boolean shouldBeProcessed(final Queue queue, final RabbitListener rabbitListener) {
        return extractQueueName(rabbitListener).contains(queue.getName());
    }

    private Set<String> extractQueueName(RabbitListener rabbitListener) {
        if(rabbitListener.bindings().length > 0) {
            return extractQueueNameFromBindings(rabbitListener);
        }
        if(rabbitListener.queuesToDeclare().length > 0) {
            return extractQueueNameFromQueues(rabbitListener);
        }
        return Collections.emptySet();
    }

    private Set<String> extractQueueNameFromBindings(RabbitListener rabbitListener) {
        return Arrays.stream(rabbitListener.bindings())
                     .filter(b -> b.declare().equals("true"))
                     .map(b -> b.value().name())
                     .collect(Collectors.toSet());
    }

    private Set<String> extractQueueNameFromQueues(RabbitListener rabbitListener) {
        return Arrays.stream(rabbitListener.queuesToDeclare())
                     .map(org.springframework.amqp.rabbit.annotation.Queue::name)
                     .collect(Collectors.toSet());
    }

    private boolean shouldBeProcessed(final Binding binding, final RabbitListener rabbitListener) {
        return Arrays.stream(rabbitListener.bindings())
                     .filter(b -> b.declare().equals("true"))
                     .filter(b -> b.exchange().name().equals(binding.getExchange()))
                     .filter(b -> b.value().name().equals(binding.getDestination()))
                     .anyMatch(b -> Arrays.stream(b.key()).anyMatch(k -> k.equals(binding.getRoutingKey())));
    }


    @Override
    public void setBeanFactory(final BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        super.setBeanFactory(beanFactory);
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
