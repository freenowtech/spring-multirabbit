package org.springframework.boot.autoconfigure.amqp;

import org.springframework.validation.annotation.Validated;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to validate if a {@link MultiRabbitPropertiesMap} is properly configured with none or only one default
 * connections.
 */
@Validated
@Constraint(validatedBy = SingleDefaultRabbitValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@interface SingleDefaultRabbit {

    String message() default "There are multiple RabbitMQ connections set as default. None or only one is allowed.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
