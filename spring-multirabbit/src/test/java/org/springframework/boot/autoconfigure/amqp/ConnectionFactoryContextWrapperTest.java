package org.springframework.boot.autoconfigure.amqp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectionFactoryContextWrapperTest {

    private static final String DUMMY_CONTEXT_NAME = "dummy-context-name";
    private static final String DUMMY_RETURN = "dummy-return";

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private Runnable runnable;

    @Mock
    private Callable<String> callable;

    private ConnectionFactoryContextWrapper wrapper() {
        return new ConnectionFactoryContextWrapper(connectionFactory);
    }

    @Test
    void shouldCall() throws Exception {
        when(callable.call()).thenReturn(DUMMY_RETURN);

        String result = wrapper().call(DUMMY_CONTEXT_NAME, callable);

        verify(callable).call();
        assertEquals(DUMMY_RETURN, result);
    }

    @Test
    void shouldNotSuppressExceptionWhenCalling() throws Exception {
        final RuntimeException ex = new RuntimeException("dummy-exception");
        when(callable.call()).thenThrow(ex);

        assertThrows(ex.getClass(), () -> wrapper().call(DUMMY_CONTEXT_NAME, callable), ex.getMessage());
    }

    @Test
    void shouldRun() throws Exception {
        wrapper().run(DUMMY_CONTEXT_NAME, runnable);

        verify(runnable).run();
    }
}
