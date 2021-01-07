package lv.llu.science.bees.webapi.domain.nodes.latestValues;

import lombok.Data;

import java.util.List;

@Data
public class LatestValuesBean {
    private String id;
    private List<LatestMeasurementBean> latestMeasurements;
    private List<LatestModelValueBean> latestModelValues;
}
