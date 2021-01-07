package lv.llu.science.bees.webapi.domain.mapping;

import lv.llu.science.bees.webapi.utils.ExMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MappingRepository extends ExMongoRepository<SourceMapping, String> {
    List<SourceMapping> findAllByNodeId(String nodeId);

    void deleteByNodeId(String nodeId);
}
