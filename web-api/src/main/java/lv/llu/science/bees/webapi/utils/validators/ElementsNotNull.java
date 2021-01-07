package lv.llu.science.bees.webapi.utils.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ElementsNotNullValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ElementsNotNull {
    String message() default "null elements in collection";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
