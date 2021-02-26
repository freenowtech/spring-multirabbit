package org.springframework.amqp.rabbit.annotation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.amqp.MultiRabbitConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabbitAdminNameResolverTest {

    private static final String DUMMY_ADMIN = "dummy-admin";
    private static final String DUMMY_CONTAINER_FACTORY = "dummy-container-factory";

    @Mock
    private RabbitListener listener;

    @Test
    void shouldResolveFromAdmin() {
        when(listener.admin()).thenReturn(DUMMY_ADMIN);
        assertEquals(DUMMY_ADMIN, RabbitAdminNameResolver.resolve(listener));

        verify(listener, atLeastOnce()).admin();
        verify(listener, never()).containerFactory();
    }

    @Test
    void shouldResolveFromContainerFactoryWhenNoAdminIsAvailable() {
        when(listener.containerFactory()).thenReturn(DUMMY_CONTAINER_FACTORY);
        String expected = DUMMY_CONTAINER_FACTORY + MultiRabbitConstants.RABBIT_ADMIN_SUFFIX;
        assertEquals(expected, RabbitAdminNameResolver.resolve(listener));

        verify(listener, atLeastOnce()).admin();
        verify(listener, atLeastOnce()).containerFactory();
    }

    @Test
    void shouldResolveFallbackToDefault() {
        assertEquals(MultiRabbitConstants.DEFAULT_RABBIT_ADMIN_BEAN_NAME, RabbitAdminNameResolver.resolve(listener));

        verify(listener, atLeastOnce()).admin();
        verify(listener, atLeastOnce()).containerFactory();
    }
}
