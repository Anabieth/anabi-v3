package lv.llu.science.bees.webapi.domain.nodes;

import lv.llu.science.bees.webapi.utils.ExMongoRepository;
import lv.llu.science.bees.webapi.utils.NotFoundException;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NodeRepository extends ExMongoRepository<Node, String> {
    List<Node> findAllByWorkspaceId(String workspaceId);

    List<Node> findAllByParentId(String parentId);

    List<Node> findAllByAncestors(String ancestorId);

    @Query(value = "{'_id': ?0}", fields = "{ 'workspaceId': 1}")
    Optional<Node> findWorkspaceById(String id);

    @Query(value = "{'_id':?0}", fields = "{ 'name': 1, 'clientId': 1}")
    Optional<Node> getNameAndClientById(String id);

    List<Node> findAllByWorkspaceIdAndType(String workspaceId, String type);

    Optional<Node> findByIdAndType(String id, String type);

    @Query(value = "{workspaceId: ?0, type:'DEVICE', clientId: ?1, isActive: true}")
    Optional<Node> findAllowedDeviceInWorkspace(String workspaceId, String clientId);

    Long countByWorkspaceId(String id);

    Optional<Node> findByClientId(String clientId);

    @PreAuthorize("@user.hasNodeAccess(#id) or @user.isAllowedDevice(#id)")
    default Node getNode(String id) {
        return this.findById(id).orElseThrow(NotFoundException::new);
    }
}
