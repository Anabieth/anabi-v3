package lv.llu.science.bees.webapi.domain.nodes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lv.llu.science.bees.webapi.domain.deviceLog.DataInLogRecord;
import lv.llu.science.bees.webapi.domain.deviceLog.DeviceLogRecord;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceBean extends NodeBean {
    private DataInLogRecord lastEvent;
    private DataInLogRecord lastError;
    private DeviceLogRecord lastLog;
}
