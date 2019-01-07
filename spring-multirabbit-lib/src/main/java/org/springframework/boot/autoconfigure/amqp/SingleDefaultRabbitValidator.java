package org.springframework.boot.autoconfigure.amqp;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator for {@link MultiRabbitPropertiesMap} to guarantee no more than one Rabbit connections is defined as default.
 */
class SingleDefaultRabbitValidator implements ConstraintValidator<SingleDefaultRabbit, MultiRabbitPropertiesMap>
{

    /**
     * Returns true if at most one {@link ExtendedRabbitProperties} is found with default=true.
     */
    @Override
    public boolean isValid(MultiRabbitPropertiesMap map, ConstraintValidatorContext context)
    {
        return map == null || map.entrySet().stream().filter(entry -> entry.getValue().isDefaultConnection()).count() <= 1;
    }

}
