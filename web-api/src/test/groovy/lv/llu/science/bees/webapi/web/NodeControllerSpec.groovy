package lv.llu.science.bees.webapi.web

import lv.llu.science.bees.webapi.domain.mapping.MappingService
import lv.llu.science.bees.webapi.domain.models.ModelService
import lv.llu.science.bees.webapi.domain.nodes.NodeBean
import lv.llu.science.bees.webapi.domain.nodes.NodeService
import lv.llu.science.bees.webapi.domain.nodes.latestValues.NodeLatestValueService
import spock.lang.Specification

import javax.servlet.http.HttpServletResponse

class NodeControllerSpec extends Specification {
    def nodeService = Mock(NodeService)
    def mappingService = Mock(MappingService)
    def modelService = Mock(ModelService)
    def latestValueService = Mock(NodeLatestValueService)
    def controller = new NodeController(nodeService, mappingService, modelService, latestValueService)

    def "should list nodes"() {
        when:
            controller.listNodes()
        then:
            1 * nodeService.getAllNodes()
    }

    def "should get node details"() {
        when:
            controller.getNodeDetails('123')
        then:
            1 * nodeService.getNodeDetails('123')
    }

    def "should get node mappings"() {
        when:
            controller.getNodeMappings('123')
        then:
            1 * mappingService.getNodeMappings('123')
    }

    def "should supported value keys"() {
        when:
            controller.getSupportedValueKeys('123')
        then:
            1 * mappingService.getSupportedValueKeys('123')
    }

    def "should update node mappings"() {
        given:
            def list = new ArrayList()
        when:
            controller.updateNodeMappings('123', list)
        then:
            1 * mappingService.updateNodeMappings('123', list)
    }

    def "should check is source id used"() {
        when:
            controller.checkIsSourceIdUsed('123', 'temp-123')
        then:
            1 * mappingService.checkIsSourceIdUsed('123', 'temp-123')
    }

    def "should add node"() {
        given:
            def bean = new NodeBean()
        when:
            controller.addNode(bean)
        then:
            1 * nodeService.createNode(bean)
    }

    def "should edit node"() {
        given:
            def bean = new NodeBean()
        when:
            controller.editNode('123', bean)
        then:
            1 * nodeService.editNode('123', bean)
    }

    def "should delete node"() {
        given:
            def resp = Mock(HttpServletResponse)
        when:
            controller.deleteNode('987', resp)
        then:
            1 * nodeService.deleteNode('987')
            1 * resp.setStatus(204)
    }

    def "should get latest values"() {
        when:
            controller.getLatestValues('123')
        then:
            1 * latestValueService.getLatestValues('123')
    }

    def "should get latest measurements"() {
        when:
            controller.getLatestMeasurements('123')
        then:
            1 * latestValueService.getLatestMeasurements('123')
    }

}
