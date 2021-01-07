package lv.llu.science.bees.webapi.domain.nodes.latestValues;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class LatestMeasurementBean {
    private String type;
    private ZonedDateTime timestamp;
    private Float value;
}
