package org.springframework.boot.autoconfigure.amqp;

import java.util.concurrent.Callable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionFactoryContextWrapperTest
{

    private static final String DUMMY_CONTEXT_NAME = "dummy-context-name";
    private static final String DUMMY_RETURN = "dummy-return";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private Runnable runnable;

    @Mock
    private Callable<String> callable;


    private ConnectionFactoryContextWrapper wrapper()
    {
        return new ConnectionFactoryContextWrapper(connectionFactory);
    }


    @Test
    public void shouldCall() throws Exception
    {
        when(callable.call()).thenReturn(DUMMY_RETURN);

        String result = wrapper().call(DUMMY_CONTEXT_NAME, callable);

        verify(callable).call();
        assertEquals(DUMMY_RETURN, result);
    }


    @Test
    public void shouldNotSuppressExceptionWhenCalling() throws Exception
    {
        exception.expect(RuntimeException.class);
        exception.expectMessage("dummy-exception");

        when(callable.call()).thenThrow(new RuntimeException("dummy-exception"));

        wrapper().call(DUMMY_CONTEXT_NAME, callable);
    }


    @Test
    public void shouldRun() throws Exception
    {
        wrapper().run(DUMMY_CONTEXT_NAME, runnable);

        verify(runnable).run();
    }

}