package lv.llu.science.bees.webapi.domain.datain;

import lombok.Data;
import lv.llu.science.bees.webapi.dwh.DwhValueBean;
import lv.llu.science.bees.webapi.utils.validators.ElementsNotNull;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class DataInBean {
    @NotNull
    private String sourceId;
    @NotNull
    @Valid
    @NotEmpty
    @ElementsNotNull
    private List<DwhValueBean> values;
    private Integer tint;
}
