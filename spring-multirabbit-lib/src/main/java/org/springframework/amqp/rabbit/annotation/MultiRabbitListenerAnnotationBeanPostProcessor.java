package org.springframework.amqp.rabbit.annotation;

import java.lang.reflect.Method;
import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * An extension of {@link RabbitListenerAnnotationBeanPostProcessor} that attaches the processing of beans
 * for Exchanges, Queues, and Bindings after they are created.
 * <p>
 * This processing enables each {@link RabbitAdmin} to differentiate which beans corresponds to that specific
 * {@link RabbitAdmin}, preventing the server from being populated with non-expected structures from other
 * servers.
 *
 * @author Wander Costa
 *
 * @see RabbitListenerAnnotationBeanPostProcessor
 */
public class MultiRabbitListenerAnnotationBeanPostProcessor
    extends RabbitListenerAnnotationBeanPostProcessor
    implements ApplicationContextAware
{

    private ApplicationContext applicationContext;
    private BeanFactory beanFactory;


    @Override
    protected void processAmqpListener(RabbitListener rabbitListener, Method method, Object bean, String beanName)
    {
        super.processAmqpListener(rabbitListener, method, bean, beanName);
        enhanceBeansWithReferenceToRabbitAdmin(rabbitListener);
    }


    /**
     * Enhance beans with related RabbitAdmin, so as to be filtered when being processed by the RabbitAdmin.
     */
    private void enhanceBeansWithReferenceToRabbitAdmin(RabbitListener rabbitListener)
    {
        RabbitAdmin rabbitAdmin = resolveRabbitAdminBean(rabbitListener);

        // Enhance Exchanges
        applicationContext.getBeansOfType(AbstractExchange.class, false, false)
            .values().stream()
            .filter(this::isNotProcessed)
            .forEach(exchange -> exchange.setAdminsThatShouldDeclare(rabbitAdmin));

        // Enhance Queues
        applicationContext.getBeansOfType(Queue.class, false, false)
            .values().stream()
            .filter(this::isNotProcessed)
            .forEach(queue -> queue.setAdminsThatShouldDeclare(rabbitAdmin));

        // Enhance Bindings
        applicationContext.getBeansOfType(Binding.class, false, false)
            .values().stream()
            .filter(this::isNotProcessed)
            .forEach(binding -> binding.setAdminsThatShouldDeclare(rabbitAdmin));
    }


    /**
     * Returns the RabbitAdmin bean of the requested name or the default one.
     */
    private RabbitAdmin resolveRabbitAdminBean(RabbitListener rabbitListener)
    {
        String name = RabbitAdminNameResolver.resolve(rabbitListener);
        return beanFactory.getBean(name, RabbitAdmin.class);
    }


    /**
     * Verifies the presence of an instance of RabbitAdmin or this object, as fallback.
     */
    private boolean isNotProcessed(Declarable declarable)
    {
    	return declarable.getDeclaringAdmins().stream()
				.noneMatch(item -> item instanceof String || item instanceof RabbitAdmin);
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory)
    {
        this.beanFactory = beanFactory;
        super.setBeanFactory(beanFactory);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }

}