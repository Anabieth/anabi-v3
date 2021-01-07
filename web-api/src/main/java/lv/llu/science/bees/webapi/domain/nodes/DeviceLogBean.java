package lv.llu.science.bees.webapi.domain.nodes;

import lombok.Data;
import lv.llu.science.bees.webapi.domain.deviceLog.DataInLogRecord;
import lv.llu.science.bees.webapi.domain.deviceLog.DeviceLogRecord;

import java.util.ArrayList;
import java.util.List;

@Data
public class DeviceLogBean {
    public String name;
    public List<DataInLogRecord> events = new ArrayList<>();
    public List<DataInLogRecord> errors = new ArrayList<>();
    public List<DeviceLogRecord> logs = new ArrayList<>();
}
