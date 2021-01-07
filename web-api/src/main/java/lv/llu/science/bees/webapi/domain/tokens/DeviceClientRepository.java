package lv.llu.science.bees.webapi.domain.tokens;

import lv.llu.science.bees.webapi.utils.ExMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceClientRepository extends ExMongoRepository<DeviceClient, String> {
    Optional<DeviceClient> findByIdAndSecret(String id, String secret);
}
