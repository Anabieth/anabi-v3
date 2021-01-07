package lv.llu.science.bees.webapi.domain.deviceLog;

import lv.llu.science.bees.webapi.utils.ExMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceLogReposotory extends ExMongoRepository<DeviceLog, String> {
}
