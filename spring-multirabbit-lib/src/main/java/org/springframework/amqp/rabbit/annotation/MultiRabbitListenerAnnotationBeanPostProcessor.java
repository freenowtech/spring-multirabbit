package org.springframework.amqp.rabbit.annotation;

import java.lang.reflect.Method;
import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
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
    implements ApplicationContextAware, BeanFactoryAware
{

    private static final String NO_ADMIN_BEAN_ERROR = "Bean '%s' for RabbitAdmin not found.";

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
        RabbitAdmin rabbitAdmin = getRabbitAdminBean(rabbitListener);

        // Enhance Exchanges
        applicationContext.getBeansOfType(AbstractExchange.class).values().stream()
            .filter(this::isNotProcessed)
            .forEach(exchange -> exchange.setAdminsThatShouldDeclare(rabbitAdmin != null ? rabbitAdmin : this));

        // Enhance Queues
        applicationContext.getBeansOfType(Queue.class).values().stream()
            .filter(this::isNotProcessed)
            .forEach(queue -> queue.setAdminsThatShouldDeclare(rabbitAdmin != null ? rabbitAdmin : this));

        // Enhance Bindings
        applicationContext.getBeansOfType(Binding.class).values().stream()
            .filter(this::isNotProcessed)
            .forEach(binding -> binding.setAdminsThatShouldDeclare(rabbitAdmin != null ? rabbitAdmin : this));
    }


    /**
     * Returns the RabbitAdmin bean of the requested name or the default one.
     */
    private RabbitAdmin getRabbitAdminBean(RabbitListener rabbitListener)
    {
        String name = RabbitAdminNameResolver.resolve(rabbitListener);
        RabbitAdmin rabbitAdmin = beanFactory.getBean(name, RabbitAdmin.class);
        if (rabbitAdmin == null)
        {
            throw new IllegalStateException(String.format(NO_ADMIN_BEAN_ERROR, name));
        }
        return rabbitAdmin;
    }


    /**
     * Verifies the presence of an instance of RabbitAdmin or this object, as fallback.
     */
    private boolean isNotProcessed(Declarable declarable)
    {
        return declarable.getDeclaringAdmins() == null
            || (declarable.getDeclaringAdmins().stream().noneMatch(item -> item == this)
            && declarable.getDeclaringAdmins().stream().noneMatch(item -> item instanceof RabbitAdmin));
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