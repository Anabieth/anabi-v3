package lv.llu.science.bees.webapi.domain.mapping

import feign.RetryableException
import lv.llu.science.bees.webapi.domain.nodes.Node
import lv.llu.science.bees.webapi.domain.nodes.NodeRepository
import lv.llu.science.bees.webapi.dwh.DwhClient
import lv.llu.science.bees.webapi.utils.ModelMapperEx
import lv.llu.science.bees.webapi.utils.NotFoundException
import org.springframework.security.access.AccessDeniedException
import spock.lang.Specification

class MappingServiceSpec extends Specification {
    def repository = Mock(MappingRepository)
    def nodeRepository = Mock(NodeRepository)
    def dwhClient = Mock(DwhClient)
    def service = new MappingService(repository, new ModelMapperEx(), nodeRepository, dwhClient)

    def "should get node mappings"() {
        when:
            def beans = service.getNodeMappings('123')
        then:
            1 * repository.findAllByNodeId('123') >> [
                    new SourceMapping(sourceId: 'src-1', valueKey: 'aaa'),
                    new SourceMapping(sourceId: 'src-2', valueKey: 'bbb')
            ]
            beans.collect { it.sourceId } == ['src-1', 'src-2']
            beans.collect { it.valueKey } == ['aaa', 'bbb']
    }

    def "should update node mappings"() {
        given:
            def newMappingBeans = [
                    new SourceMappingBean(sourceId: 'src-1', valueKey: 'key-1a'),
                    new SourceMappingBean(sourceId: 'src-2', valueKey: 'key-2a'),
                    new SourceMappingBean(sourceId: 'src-new', valueKey: 'key-new')
            ]

            def oldMapping = [
                    mapping('src-1', 'key-1'),
                    mapping('src-2', 'key-2'),
                    mapping('src-old', 'key-old')
            ]
        when:
            service.updateNodeMappings('123', newMappingBeans)
        then:
            1 * repository.findAllByNodeId('123') >> oldMapping
            1 * repository.delete(mapping('src-old', 'key-old'))
            1 * repository.save(mapping('src-1', 'key-1a'))
            1 * repository.save(mapping('src-2', 'key-2a'))
            1 * repository.insert(mapping('src-new', 'key-new'))
            1 * repository.findAllByNodeId('123') >> []
    }

    private static SourceMapping mapping(String src, String key, String node = '123') {
        new SourceMapping(sourceId: src, valueKey: key, nodeId: node)
    }

    def "should check if sourceId not used"() {
        when:
            def result = service.checkIsSourceIdUsed('node-123', 'src-1')
        then:
            1 * repository.findById('src-1') >> Optional.empty()
            result == [isUsed: false]
    }

    def "should check if sourceId is used in the same node"() {
        when:
            def result = service.checkIsSourceIdUsed('node-123', 'src-1')
        then:
            1 * repository.findById('src-1') >> Optional.of(mapping('src-1', 'key-1', 'node-123'))
            result == [isUsed: false]
    }

    def "should check if sourceId is used in other node"() {
        when:
            def result = service.checkIsSourceIdUsed('node-123', 'src-1')
        then:
            1 * repository.findById('src-1') >> Optional.of(mapping('src-1', 'key-1', 'node-234'))
            1 * nodeRepository.getNode('node-234') >> new Node(id: 'node-234', name: 'Other node')
            result == [isUsed: true, nodeId: 'node-234', nodeName: 'Other node']
    }

    def "should check if sourceId is used in other user's node"() {
        when:
            def result = service.checkIsSourceIdUsed('node-123', 'src-1')
        then:
            1 * repository.findById('src-1') >> Optional.of(mapping('src-1', 'key-1', 'node-234'))
            1 * nodeRepository.getNode('node-234') >> { throw new AccessDeniedException("Other user's node") }
            result == [isUsed: true]
    }

    def "should get mapping by source id"() {
        when:
            service.getMapping('src-123')
        then:
            1 * repository.findById('src-123') >> Optional.of(mapping('src-123', 'test'))

        when:
            service.getMapping('src-234')
        then:
            1 * repository.findById('src-234') >> Optional.empty()
            thrown(NotFoundException)
    }

    def "should get supported mapping value keys"() {
        given:
            def keys = ['a', 'b', 'c']
        when: 'DWH returns keys'
            def result = service.getSupportedValueKeys('node-123')
        then:
            1 * dwhClient.getSupportedValueKeys() >> keys
            result == keys

        when: 'DWH is offline'
            def result2 = service.getSupportedValueKeys('node-123')
        then:
            1 * dwhClient.getSupportedValueKeys() >> { throw new RetryableException('offline', null) }
            result2 == []
    }
}
