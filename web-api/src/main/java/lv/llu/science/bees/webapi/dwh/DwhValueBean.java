package lv.llu.science.bees.webapi.dwh;

import lombok.Data;
import lv.llu.science.bees.webapi.utils.validators.AnyNotNull;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@AnyNotNull(fields = {"value", "values"})
public class DwhValueBean {
    @NotNull
    private ZonedDateTime ts;
    private Float value;
    private List<Float> values;
}
