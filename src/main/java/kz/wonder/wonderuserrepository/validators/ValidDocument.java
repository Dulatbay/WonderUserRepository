package kz.wonder.wonderuserrepository.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {DocumentValidator.class})
public @interface ValidDocument {
    String message() default "Only PDF format is allowed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
