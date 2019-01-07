package org.springframework.boot.autoconfigure.amqp;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SingleDefaultRabbitValidatorTest
{

    private static final ExtendedRabbitProperties DUMMY_PROPS = extendedProperties();
    private static final ExtendedRabbitProperties DUMMY_DEFAULT_PROPS = extendedProperties(true);


    @Test
    public void shouldValidateNullObject()
    {
        assertTrue(new SingleDefaultRabbitValidator().isValid(null, null));
    }


    @Test
    public void shouldValidateZeroDefaultConnections()
    {
        MultiRabbitPropertiesMap map = new MultiRabbitPropertiesMap();
        map.putAll(ImmutableMap.of("p1", DUMMY_PROPS, "p2", DUMMY_PROPS, "p3", DUMMY_PROPS));

        assertTrue(new SingleDefaultRabbitValidator().isValid(map, null));
    }


    @Test
    public void shouldValidateOneDefaultConnections()
    {
        MultiRabbitPropertiesMap map = new MultiRabbitPropertiesMap();
        map.putAll(ImmutableMap.of("p1", DUMMY_DEFAULT_PROPS, "p2", DUMMY_PROPS, "p3", DUMMY_PROPS));

        assertTrue(new SingleDefaultRabbitValidator().isValid(map, null));
    }


    @Test
    public void shouldNotValidateTwoDefaultConnections()
    {
        MultiRabbitPropertiesMap map = new MultiRabbitPropertiesMap();
        map.putAll(ImmutableMap.of("p1", DUMMY_DEFAULT_PROPS, "p2", DUMMY_DEFAULT_PROPS, "p3", DUMMY_PROPS));

        assertFalse(new SingleDefaultRabbitValidator().isValid(map, null));
    }


    private static ExtendedRabbitProperties extendedProperties()
    {
        return extendedProperties(false);
    }


    private static ExtendedRabbitProperties extendedProperties(boolean isDefault)
    {
        ExtendedRabbitProperties properties = new ExtendedRabbitProperties();
        properties.setDefaultConnection(isDefault);
        return properties;
    }

}