package lv.llu.science.bees.webapi.domain.models;

import lv.llu.science.bees.webapi.utils.ExMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRepository extends ExMongoRepository<ModelDefinition, String> {
    List<ModelDefinition> findAllByWorkspaceId(String workspaceId);
}
