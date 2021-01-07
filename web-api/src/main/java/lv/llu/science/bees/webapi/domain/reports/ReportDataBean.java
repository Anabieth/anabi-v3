package lv.llu.science.bees.webapi.domain.reports;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReportDataBean extends ReportBean {
    private List<LabeledValues> data = new ArrayList<>();
}
