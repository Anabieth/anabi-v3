package lv.llu.science.bees.webapi.domain.deviceLog;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
@Data
public class DeviceLog {
    @Id
    private String clientId;

    private List<DataInLogRecord> lastEvents = new ArrayList<>();
    private List<DataInLogRecord> lastErrors = new ArrayList<>();
    private List<DeviceLogRecord> lastLogs = new ArrayList<>();
}
