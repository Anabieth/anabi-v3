package lv.llu.science.bees.webapi.domain.nodes.latestValues;

import com.mongodb.BasicDBObject;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class LatestModelValueBean {
    private String modelCode;
    private ZonedDateTime timestamp;
    private String label;
    private String description;
    private BasicDBObject rawValue;
}
