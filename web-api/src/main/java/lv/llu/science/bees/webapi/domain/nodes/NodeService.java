package lv.llu.science.bees.webapi.domain.nodes;

import lv.llu.science.bees.webapi.domain.deviceLog.DeviceLogReposotory;
import lv.llu.science.bees.webapi.domain.mapping.MappingRepository;
import lv.llu.science.bees.webapi.domain.workspaces.ActiveWorkspace;
import lv.llu.science.bees.webapi.utils.CurrentUser;
import lv.llu.science.bees.webapi.utils.ModelMapperEx;
import lv.llu.science.bees.webapi.utils.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.util.StringUtils.hasText;

@Service
public class NodeService {

    private final NodeRepository repository;
    private final CurrentUser currentUser;
    private final ModelMapperEx mapper;
    private final MongoTemplate mongoTemplate;
    private final DeviceLogReposotory logRepository;
    private final MappingRepository mappingRepository;
    private final ActiveWorkspace activeWorkspace;

    private final Comparator<Node> nodeNameComparator = comparing(Node::getName, nullsLast(naturalOrder()));

    @Autowired
    public NodeService(NodeRepository repository, CurrentUser currentUser, ModelMapperEx mapper,
                       MongoTemplate mongoTemplate, DeviceLogReposotory logRepository, MappingRepository mappingRepository,
                       ActiveWorkspace activeWorkspace) {
        this.repository = repository;
        this.currentUser = currentUser;
        this.mapper = mapper;
        this.mongoTemplate = mongoTemplate;
        this.logRepository = logRepository;
        this.mappingRepository = mappingRepository;
        this.activeWorkspace = activeWorkspace;
    }

    @PreAuthorize("@activeWorkspace.id != null")
    public List<NodeBean> getAllNodes() {
        List<Node> nodes = repository.findAllByWorkspaceId(activeWorkspace.getId());
        nodes.sort(nodeNameComparator);
        return mapper.mapList(nodes, NodeBean.class);
    }

    @PreAuthorize("@activeWorkspace.id != null and (#bean.parentId == null or @user.hasNodeAccess(#bean.parentId))")
    public NodeBean createNode(NodeBean bean) {
        Node node = mapper.map(bean, Node.class);
        if (node.getParentId() != null) {
            Node parent = repository.getNode(node.getParentId());
            if (!activeWorkspace.getId().equals(parent.getWorkspaceId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Trying to create node with parent from another workspace");
            }
            node.getAncestors().addAll(parent.getAncestors());
            node.getAncestors().add(parent.getId());
        }
        node.setCreatedBy(currentUser.getUsername());
        node.setWorkspaceId(activeWorkspace.getId());
        Node saved = repository.save(node);
        return mapper.map(saved, NodeBean.class);
    }

    @PreAuthorize("@user.hasNodeAccess(#id)")
    public NodeBean editNode(String id, NodeBean bean) {
        Node node = repository.getNode(id);
        node.setName(bean.getName());
        node.setType(bean.getType());
        node.setLocation(bean.getLocation());
        node.setClientId(hasText(bean.getClientId()) ? bean.getClientId() : null);
        node.setIsActive(bean.getIsActive());
        if (currentUser.isHwEngineer()) {
            node.setHwConfigId(bean.getHwConfigId());
        }
        Node saved = repository.save(node);
        return mapper.map(saved, NodeBean.class);
    }

    @PreAuthorize("@user.hasNodeAccess(#id)")
    public void deleteNode(String id) {
        Node node = repository.getNode(id);
        repository.findAllByAncestors(node.getId())
                .forEach(this::deleteNodeAndMappings);
        deleteNodeAndMappings(node);
    }

    private void deleteNodeAndMappings(Node node) {
        mappingRepository.deleteByNodeId(node.getId());
        repository.delete(node);
    }

    @PreAuthorize("@user.hasNodeAccess(#id)")
    public NodeBean getNodeDetails(String id) {
        Node node = repository.getNode(id);
        NodeDetailsBean bean = new NodeDetailsBean();
        bean.setId(node.getId());
        bean.setName(node.getName());
        bean.setType(node.getType());
        bean.setLocation(node.getLocation());
        bean.setClientId(node.getClientId());
        bean.setIsActive(node.getIsActive());
        bean.setHwConfigId(node.getHwConfigId());

        repository.findAllById(node.getAncestors())
                .forEach(n -> {
                    NodeBean b = new NodeBean();
                    b.setId(n.getId());
                    b.setName(n.getName());
                    bean.getAncestors().add(b);
                });

        repository.findAllByParentId(node.getId())
                .stream()
                .sorted(nodeNameComparator)
                .forEach(n -> {
                    NodeBean b = new NodeBean();
                    b.setId(n.getId());
                    b.setName(n.getName());
                    bean.getChildren().add(b);
                });
        return bean;
    }

    @PreAuthorize("@activeWorkspace.id != null")
    public List<DeviceBean> getDevices() {
        return repository.findAllByWorkspaceIdAndType(activeWorkspace.getId(), "DEVICE").stream()
                .sorted(nodeNameComparator)
                .map(node -> {
                    DeviceBean bean = new DeviceBean();
                    bean.setId(node.getId());
                    bean.setName(node.getName());
                    bean.setLocation(node.getLocation());
                    bean.setClientId(node.getClientId());
                    bean.setIsActive(node.getIsActive());

                    Optional.ofNullable(node.getClientId())
                            .flatMap(logRepository::findById)
                            .ifPresent(log -> {
                                bean.setLastEvent(log.getLastEvents().stream().findFirst().orElse(null));
                                bean.setLastError(log.getLastErrors().stream().findFirst().orElse(null));
                                bean.setLastLog(log.getLastLogs().stream().findFirst().orElse(null));
                            });

                    return bean;
                }).collect(toList());
    }

    @PreAuthorize("@activeWorkspace.id != null && @user.isHwEngineer()")
    public List<DeviceBean> getDevicesByHwConfig(String hwConfigId) {
        return repository.findAllByWorkspaceIdAndType(activeWorkspace.getId(), "DEVICE").stream()
                .filter(node -> hwConfigId.equals(node.getHwConfigId()))
                .sorted(nodeNameComparator)
                .map(node -> {
                    DeviceBean bean = new DeviceBean();
                    bean.setId(node.getId());
                    bean.setName(node.getName());
                    return bean;
                }).collect(toList());
    }

    @PreAuthorize("@user.hasNodeAccess(#nodeId)")
    public DeviceLogBean getDeviceEvents(String nodeId) {
        Node node = repository.getNameAndClientById(nodeId).orElseThrow(NotFoundException::new);
        DeviceLogBean bean = new DeviceLogBean();
        bean.setName(node.getName());
        if (node.getClientId() != null) {
            logRepository.findById(node.getClientId())
                    .ifPresent(log -> {
                        bean.setErrors(log.getLastErrors());
                        bean.setEvents(log.getLastEvents());
                        bean.setLogs(log.getLastLogs());
                    });
        }
        return bean;
    }

    @PreAuthorize("@user.hasNodeAccess(#id)")
    public Boolean toggleActive(String id) {
        Node device = repository.findByIdAndType(id, "DEVICE").orElseThrow(NotFoundException::new);
        device.setIsActive(device.getIsActive() != null && !device.getIsActive());

        mongoTemplate.updateFirst(new Query(where("id").is(device.getId())),
                new Update().set("isActive", device.getIsActive()),
                Node.class);

        return device.getIsActive();
    }

    @PreAuthorize("@user.isDevice()")
    public Node getOwnDevice() {
        return repository.findByClientId(currentUser.getUsername()).orElseThrow(NotFoundException::new);
    }
}
