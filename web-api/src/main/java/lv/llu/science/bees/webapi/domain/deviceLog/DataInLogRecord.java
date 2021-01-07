package lv.llu.science.bees.webapi.domain.deviceLog;

import lombok.Data;
import lv.llu.science.bees.webapi.domain.datain.DataInMappingResult;

import java.time.ZonedDateTime;

@Data
public class DataInLogRecord {
    private ZonedDateTime ts;
    private String sourceId;
    private DataInMappingResult result;
    private String type;
    private String nodeId;
    private String nodeName;
}
