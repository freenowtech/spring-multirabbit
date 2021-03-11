package org.springframework.amqp.rabbit.annotation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.springframework.amqp.core.AbstractDeclarable;
import org.springframework.amqp.core.Declarable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

/**
 * An extension of {@link RabbitListenerAnnotationBeanPostProcessor} that attaches the processing of beans for
 * Exchanges, Queues, and Bindings after they are created.
 * <p>
 * This processing enables each {@link org.springframework.amqp.rabbit.core.RabbitAdmin} to differentiate which beans
 * corresponds to that specific {@link org.springframework.amqp.rabbit.core.RabbitAdmin}, preventing the server from
 * being populated with non-expected structures from other servers.
 *
 * @author Wander Costa
 * @see RabbitListenerAnnotationBeanPostProcessor
 */
public final class MultiRabbitListenerAnnotationBeanPostProcessor
        extends RabbitListenerAnnotationBeanPostProcessor
        implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    protected void processAmqpListener(final RabbitListener rabbitListener,
                                       final Method method,
                                       final Object bean,
                                       final String beanName) {
        final String rabbitAdmin = RabbitAdminNameResolver.resolve(rabbitListener);
        final RabbitListener rabbitListenerRef = proxyIfAdminNotPresent(rabbitListener, rabbitAdmin);
        super.processAmqpListener(rabbitListenerRef, method, bean, beanName);
        applicationContext.getBeansOfType(AbstractDeclarable.class).values().stream()
                .filter(this::isNotProcessed)
                .forEach(exchange -> exchange.setAdminsThatShouldDeclare(rabbitAdmin));
    }

    private RabbitListener proxyIfAdminNotPresent(final RabbitListener rabbitListener, final String rabbitAdmin) {
        if (StringUtils.hasText(rabbitListener.admin())) {
            return rabbitListener;
        }
        return (RabbitListener) Proxy.newProxyInstance(
                RabbitListener.class.getClassLoader(), new Class<?>[]{RabbitListener.class},
                new RabbitListenerAdminReplacementInvocationHandler(rabbitListener, rabbitAdmin));
    }

    /**
     * Verifies the presence of an instance of RabbitAdmin or this object, as fallback.
     */
    private boolean isNotProcessed(final Declarable declarable) {
        return declarable.getDeclaringAdmins() == null || declarable.getDeclaringAdmins().isEmpty();
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * An {@link InvocationHandler} to provide a replacing admin() parameter of the listener.
     */
    private final class RabbitListenerAdminReplacementInvocationHandler implements InvocationHandler {

        private final RabbitListener target;
        private final String admin;

        private RabbitListenerAdminReplacementInvocationHandler(final RabbitListener target, final String admin) {
            this.target = target;
            this.admin = admin;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args)
                throws InvocationTargetException, IllegalAccessException {
            if (method.getName().equals("admin")) {
                return this.admin;
            }
            return method.invoke(this.target, args);
        }
    }
}
