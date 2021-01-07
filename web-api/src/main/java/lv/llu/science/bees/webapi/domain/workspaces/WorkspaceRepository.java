package lv.llu.science.bees.webapi.domain.workspaces;

import lv.llu.science.bees.webapi.utils.ExMongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkspaceRepository extends ExMongoRepository<Workspace, String> {

    @Query("{ $or: [{ 'owner': ?0}, { 'invitedUsers': ?0}] }")
    List<Workspace> findByUser(String userName);
}
