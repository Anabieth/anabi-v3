package lv.llu.science.bees.webapi.utils.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.Objects;

public class ElementsNotNullValidator implements ConstraintValidator<ElementsNotNull, Collection> {

    @Override
    @SuppressWarnings("unchecked")
    public boolean isValid(Collection value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return value.stream().noneMatch(Objects::isNull);
    }
}
