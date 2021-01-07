package lv.llu.science.bees.webapi.domain.configs;

import lv.llu.science.bees.webapi.utils.ExMongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigRepository extends ExMongoRepository<Config, String> {
    @Query(value = "{ isDefault: true }")
    Optional<Config> getDefault();
}
