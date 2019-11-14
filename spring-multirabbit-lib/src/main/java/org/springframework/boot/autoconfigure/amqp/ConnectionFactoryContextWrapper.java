package org.springframework.boot.autoconfigure.amqp;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SimpleResourceHolder;

import java.util.concurrent.Callable;

import static org.springframework.util.StringUtils.hasText;

/**
 * Helper class to handle ConnectionFactory context binding and unbinding when executing instructions.
 */
public class ConnectionFactoryContextWrapper {

    private final ConnectionFactory connectionFactory;

    /**
     * Returns a new ConectionFactoryContextWrapper that will bind the given {@link ConnectionFactory}.
     *
     * @param connectionFactory The {@link ConnectionFactory} to be bound to.
     */
    public ConnectionFactoryContextWrapper(final ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Executes a {@link Callable} binding to the default {@link ConnectionFactory} and finally unbinding it.
     *
     * @param callable the {@link Callable} object to be executed.
     * @param <T>      the return type.
     * @return the result of the {@link Callable}.
     * @throws Exception when an Exception is thrown by the {@link Callable}.
     */
    public <T> T call(final Callable<T> callable) throws Exception {
        return call(null, callable);
    }

    /**
     * Executes a {@link Callable} binding the given {@link ConnectionFactory} and finally unbinding it.
     *
     * @param contextName the name of the context. In null, empty or blank, default context is bound.
     * @param callable    the {@link Callable} object to be executed.
     * @param <T>         the return type.
     * @return the result of the {@link Callable}.
     * @throws Exception when an Exception is thrown by the {@link Callable}.
     */
    public <T> T call(final String contextName, final Callable<T> callable) throws Exception {
        try {
            bind(contextName);
            return callable.call();
        } finally {
            unbind(contextName);
        }
    }

    /**
     * Executes a {@link Runnable} binding to the default {@link ConnectionFactory} and finally unbinding it.
     *
     * @param runnable the {@link Runnable} object to be executed.
     * @throws RuntimeException when a RuntimeException is thrown by the {@link Runnable}.
     */
    public void run(final Runnable runnable) {
        run(null, runnable);
    }

    /**
     * Executes a {@link Runnable} binding the given {@link ConnectionFactory} and finally unbinding it.
     *
     * @param contextName the name of the context. In null, empty or blank, default context is bound.
     * @param runnable    the {@link Runnable} object to be executed.
     * @throws RuntimeException when a RuntimeException is thrown by the {@link Runnable}.
     */
    public void run(final String contextName, final Runnable runnable) {
        try {
            bind(contextName);
            runnable.run();
        } finally {
            unbind(contextName);
        }
    }

    /**
     * Binds the context.
     */
    private void bind(final String contextName) {
        if (hasText(contextName)) {
            SimpleResourceHolder.bind(connectionFactory, contextName);
        }
    }

    /**
     * Unbinds the context.
     */
    private void unbind(final String contextName) {
        if (hasText(contextName)) {
            SimpleResourceHolder.unbind(connectionFactory);
        }
    }

}
