package lv.llu.science.bees.webapi.domain.deviceLog;

import lv.llu.science.bees.webapi.utils.CurrentUser;
import lv.llu.science.bees.webapi.utils.TimeMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import static lv.llu.science.bees.webapi.domain.datain.DataInMappingResult.Ok;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class DeviceLogService {

    private final CurrentUser currentUser;
    private final MongoTemplate mongoTemplate;
    private final TimeMachine timeMachine;

    @Autowired
    public DeviceLogService(CurrentUser currentUser, MongoTemplate mongoTemplate, TimeMachine timeMachine) {
        this.currentUser = currentUser;
        this.mongoTemplate = mongoTemplate;
        this.timeMachine = timeMachine;
    }

    @PreAuthorize("@user.isDevice()")
    public void addDeviceLogRecord(DeviceLogRecord record) {
        if (record.getTs() == null) {
            record.setTs(timeMachine.zonedNow());
        }

        if (record.getLevel() == null || record.getLevel().equals("")) {
            record.setLevel("info");
        }

        Query query = new Query(where("clientId").is(currentUser.getUsername()));
        Sort tsSort = Sort.by("ts").descending();

        Update update = new Update().push("lastLogs")
                .sort(tsSort)
                .slice(100)
                .each(record);

        mongoTemplate.upsert(query, update, DeviceLog.class);
    }

    @PreAuthorize("@user.isDevice()")
    public void addDataInLogRecord(DataInLogRecord record) {
        Query query = new Query(where("clientId").is(currentUser.getUsername()));
        Sort tsSort = Sort.by("ts").descending();

        Update update = new Update().push("lastEvents")
                .sort(tsSort)
                .slice(20)
                .each(record);

        if (record.getResult() != Ok) {
            update.push("lastErrors")
                    .sort(tsSort)
                    .slice(10)
                    .each(record);
        }

        mongoTemplate.upsert(query, update, DeviceLog.class);
    }
}
