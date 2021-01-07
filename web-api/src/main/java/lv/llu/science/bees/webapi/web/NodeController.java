package lv.llu.science.bees.webapi.web;

import lv.llu.science.bees.webapi.domain.mapping.MappingService;
import lv.llu.science.bees.webapi.domain.mapping.SourceMappingBean;
import lv.llu.science.bees.webapi.domain.models.ModelService;
import lv.llu.science.bees.webapi.domain.nodes.NodeBean;
import lv.llu.science.bees.webapi.domain.nodes.NodeService;
import lv.llu.science.bees.webapi.domain.nodes.latestValues.LatestValuesBean;
import lv.llu.science.bees.webapi.domain.nodes.latestValues.NodeLatestValueService;
import lv.llu.science.bees.webapi.dwh.DwhValueBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/nodes")
public class NodeController {

    private final NodeService nodeService;
    private final MappingService mappingService;
    private final ModelService modelService;
    private final NodeLatestValueService latestValueService;

    @Autowired
    public NodeController(NodeService service, MappingService mappingService, ModelService modelService, NodeLatestValueService latestValueService) {
        this.nodeService = service;
        this.mappingService = mappingService;
        this.modelService = modelService;
        this.latestValueService = latestValueService;
    }

    @GetMapping
    public List<NodeBean> listNodes() {
        return nodeService.getAllNodes();
    }

    @GetMapping(path = "/{id}")
    public NodeBean getNodeDetails(@PathVariable String id) {
        return nodeService.getNodeDetails(id);
    }

    @GetMapping(path = "/{id}/mapping")
    public List<SourceMappingBean> getNodeMappings(@PathVariable String id) {
        return mappingService.getNodeMappings(id);
    }

    @PutMapping(path = "/{id}/mapping")
    public List<SourceMappingBean> updateNodeMappings(@PathVariable String id,
                                                      @RequestBody ArrayList<SourceMappingBean> mapping) {
        return mappingService.updateNodeMappings(id, mapping);
    }

    @PostMapping(path = "/{nodeId}/mapping/isUsed")
    public Map<String, Object> checkIsSourceIdUsed(@PathVariable String nodeId,
                                                   @RequestBody String sourceId) {
        return mappingService.checkIsSourceIdUsed(nodeId, sourceId);
    }

    @GetMapping(path = "/{nodeId}/mapping/valueKeys")
    public List<String> getSupportedValueKeys(@PathVariable String nodeId) {
        return mappingService.getSupportedValueKeys(nodeId);
    }

    @PostMapping
    public NodeBean addNode(@RequestBody NodeBean bean) {
        return nodeService.createNode(bean);
    }

    @PutMapping(path = "/{id}")
    public NodeBean editNode(@PathVariable String id, @RequestBody NodeBean bean) {
        return nodeService.editNode(id, bean);
    }

    @DeleteMapping(path = "/{id}")
    public void deleteNode(@PathVariable String id, HttpServletResponse response) {
        nodeService.deleteNode(id);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @GetMapping(path = "/{id}/latestValues")
    public LatestValuesBean getLatestValues(@PathVariable String id) {
        return latestValueService.getLatestValues(id);
    }

    @GetMapping(path = "/{id}/latestMeasurements")
    public Map<String, List<DwhValueBean>> getLatestMeasurements(@PathVariable String id) {
        return latestValueService.getLatestMeasurements(id);
    }
}
