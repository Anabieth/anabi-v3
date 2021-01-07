package lv.llu.science.bees.webapi.domain.mapping;

import feign.RetryableException;
import lv.llu.science.bees.webapi.domain.nodes.Node;
import lv.llu.science.bees.webapi.domain.nodes.NodeRepository;
import lv.llu.science.bees.webapi.dwh.DwhClient;
import lv.llu.science.bees.webapi.utils.ModelMapperEx;
import lv.llu.science.bees.webapi.utils.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MappingService {
    private final MappingRepository repository;
    private final ModelMapperEx mapper;
    private final NodeRepository nodeRepository;
    private final DwhClient dwhClient;

    @Autowired
    public MappingService(MappingRepository repository, ModelMapperEx mapper, NodeRepository nodeRepository, DwhClient dwhClient) {
        this.repository = repository;
        this.mapper = mapper;
        this.nodeRepository = nodeRepository;
        this.dwhClient = dwhClient;
    }

    @PreAuthorize("@user.hasNodeAccess(#nodeId)")
    public List<SourceMappingBean> getNodeMappings(String nodeId) {
        return mapper.mapList(
                repository.findAllByNodeId(nodeId),
                SourceMappingBean.class);
    }

    @PreAuthorize("@user.hasNodeAccess(#nodeId)")
    public List<String> getSupportedValueKeys(String nodeId) {
        List<String> keys;
        try {
            keys = dwhClient.getSupportedValueKeys();
        } catch (RetryableException ex) {
            keys = new ArrayList<>();
        }
        return keys;
    }

    @PreAuthorize("@user.hasNodeAccess(#nodeId)")
    public List<SourceMappingBean> updateNodeMappings(String nodeId, List<SourceMappingBean> newMapping) {
        List<SourceMapping> existingMapping = repository.findAllByNodeId(nodeId);

        existingMapping.stream()
                .filter(e -> newMapping.stream()
                        .noneMatch(m -> e.getSourceId().equals(m.getSourceId()))
                )
                .forEach(repository::delete);

        for (SourceMappingBean bean : newMapping) {
            Optional<SourceMapping> existing = existingMapping.stream()
                    .filter(e -> e.getSourceId().equals(bean.getSourceId()))
                    .findFirst();

            if (existing.isPresent()) {
                SourceMapping map = existing.get();
                map.setValueKey(bean.getValueKey());
                repository.save(map);
            } else {
                SourceMapping map = new SourceMapping();
                map.setSourceId(bean.getSourceId());
                map.setValueKey(bean.getValueKey());
                map.setNodeId(nodeId);
                repository.insert(map);
            }
        }

        return getNodeMappings(nodeId);
    }

    public Map<String, Object> checkIsSourceIdUsed(String nodeId, String sourceId) {
        HashMap<String, Object> result = new HashMap<>();

        Optional<SourceMapping> mapping = repository.findById(sourceId);
        if (mapping.isPresent() && !nodeId.equals(mapping.get().getNodeId())) {
            result.put("isUsed", true);

            try {
                Node node = nodeRepository.getNode(mapping.get().getNodeId());
                result.put("nodeId", node.getId());
                result.put("nodeName", node.getName());
            } catch (AccessDeniedException ex) {
                // user was not able to get node for given mapping
                // means source ID is used in other user's node
                // just ignore and don't return any details about the node
            }
        } else {
            result.put("isUsed", false);
        }

        return result;
    }

    @PostAuthorize("@user.isAllowedDevice(returnObject.nodeId)")
    public SourceMapping getMapping(String sourceId) {
        return repository.findById(sourceId).orElseThrow(NotFoundException::new);
    }
}
