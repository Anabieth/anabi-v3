package lv.llu.science.bees.webapi.utils.validators;

import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AnyNotNullValidator implements ConstraintValidator<AnyNotNull, Object> {
    private List<String> fields;

    @Override
    public void initialize(AnyNotNull annotation) {
        fields = Arrays.asList(annotation.fields());
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return fields.stream()
                .map(f -> new BeanWrapperImpl(value).getPropertyValue(f))
                .anyMatch(Objects::nonNull);
    }
}
