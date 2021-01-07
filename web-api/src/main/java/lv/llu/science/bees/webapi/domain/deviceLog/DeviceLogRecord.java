package lv.llu.science.bees.webapi.domain.deviceLog;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class DeviceLogRecord {
    private ZonedDateTime ts;
    private String level;
    private String message;
}
