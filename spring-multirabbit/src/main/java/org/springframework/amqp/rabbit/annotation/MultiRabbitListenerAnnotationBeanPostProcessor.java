package org.springframework.amqp.rabbit.annotation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import org.springframework.amqp.core.Declarable;
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
        extends RabbitListenerAnnotationBeanPostProcessor {

    @Override
    protected Collection<Declarable> processAmqpListener(final RabbitListener rabbitListener,
                                                         final Method method,
                                                         final Object bean,
                                                         final String beanName) {
        final String rabbitAdmin = RabbitAdminNameResolver.resolve(rabbitListener);
        final RabbitListener rabbitListenerRef = proxyIfAdminNotPresent(rabbitListener, rabbitAdmin);
        final Collection<Declarable> declarables = super.processAmqpListener(rabbitListenerRef, method, bean, beanName);
        declarables.forEach(declarable -> declarable.setAdminsThatShouldDeclare(rabbitAdmin));
        return declarables;
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
