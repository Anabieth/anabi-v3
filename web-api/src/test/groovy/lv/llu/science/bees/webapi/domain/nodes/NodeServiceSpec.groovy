package lv.llu.science.bees.webapi.domain.nodes

import lv.llu.science.bees.webapi.domain.deviceLog.DataInLogRecord
import lv.llu.science.bees.webapi.domain.deviceLog.DeviceLog
import lv.llu.science.bees.webapi.domain.deviceLog.DeviceLogRecord
import lv.llu.science.bees.webapi.domain.deviceLog.DeviceLogReposotory
import lv.llu.science.bees.webapi.domain.mapping.MappingRepository
import lv.llu.science.bees.webapi.domain.workspaces.ActiveWorkspace
import lv.llu.science.bees.webapi.utils.CurrentUser
import lv.llu.science.bees.webapi.utils.ModelMapperEx
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification
import spock.lang.Unroll

class NodeServiceSpec extends Specification {
    def repository = Mock(NodeRepository)
    def currentUser = Mock(CurrentUser)
    def mapper = new ModelMapperEx()
    def mongoTemplate = Mock(MongoTemplate)
    def logRepository = Mock(DeviceLogReposotory)
    def mappingRepository = Mock(MappingRepository)
    def activeWorkspace = new ActiveWorkspace(id: 'ws-10')
    def service = new NodeService(repository, currentUser, mapper, mongoTemplate, logRepository, mappingRepository, activeWorkspace)

    def "should get all nodes"() {
        when:
            def beans = service.getAllNodes()
        then:
            1 * repository.findAllByWorkspaceId('ws-10') >> [
                    new Node(id: '123', name: 'B node'),
                    new Node(id: '234', name: 'A node')]
            beans.size() == 2
            beans[0].name == 'A node'
            beans[1].name == 'B node'
    }

    def "should create new node"() {
        given:
            def bean = new NodeBean(name: 'A node', parentId: '987')
            def parentNode = new Node(id: 987, name: 'Parent node', ancestors: ['111', '222'], workspaceId: 'ws-10')
        when:
            def outBean = service.createNode(bean)
        then:
            1 * repository.getNode('987') >> parentNode
            1 * currentUser.getUsername() >> 'testUser'
            1 * repository.save(_) >> { args ->
                def node = (Node) args[0]
                assert node.name == 'A node'
                assert node.workspaceId == 'ws-10'
                assert node.createdBy == 'testUser'
                assert node.parentId == '987'
                assert node.ancestors == ['111', '222', '987']
                node.id = '123'
                return node
            }
            outBean.id == '123'
    }

    def "should not create new node with parent from another workspace"() {
        given:
            def bean = new NodeBean(name: 'A node', parentId: '987')
            def parentNode = new Node(id: 987, name: 'Parent node', ancestors: ['111', '222'], workspaceId: 'ws-99')
        when:
            service.createNode(bean)
        then:
            1 * repository.getNode('987') >> parentNode
            thrown(ResponseStatusException)
    }

    def "should edit node"() {
        given:
            def bean = new NodeBean(name: 'New name', type: 'NEW', parentId: '987', clientId: '')
        when:
            service.editNode('456', bean)
        then:
            1 * repository.getNode('456') >> new Node(name: 'Old name', type: 'OLD', id: '456', parentId: '876')
            1 * repository.save(_) >> { args ->
                def node = (Node) args[0]
                assert node.name == 'New name'
                assert node.type == 'NEW'
                assert node.parentId == '876'
                assert node.clientId == null
                return node
            }
    }

    def "should delete node"() {
        when:
            service.deleteNode('1')
        then:
            1 * repository.getNode('1') >> new Node(id: '1')
            1 * repository.findAllByAncestors('1') >> [new Node(id: '10'), new Node(id: '11')]
            3 * mappingRepository.deleteByNodeId(_)
            3 * repository.delete(_)
    }

    def "should get node details"() {
        when:
            def bean = service.getNodeDetails('123') as NodeDetailsBean
        then:
            1 * repository.getNode('123') >> new Node(id: '123', ancestors: ['12', '1'])
            1 * repository.findAllById(['12', '1']) >> [new Node(id: '12'), new Node(id: '1')]
            1 * repository.findAllByParentId('123') >> [
                    new Node(id: '1234', name: 'child-Z'),
                    new Node(id: '1235', name: 'child-A')]
            bean.id == '123'
            bean.ancestors.collect { it.id } == ['12', '1']
            bean.children.collect { it.id } == ['1235', '1234'] // sorted by name
    }

    def "should get device list"() {
        when:
            def beans = service.getDevices()
        then:
            1 * repository.findAllByWorkspaceIdAndType('ws-10', 'DEVICE') >> [
                    new Node(name: 'dev-Z'),
                    new Node(name: 'dev-A', clientId: 'client-123')
            ]
            1 * logRepository.findById('client-123') >> Optional.of(new DeviceLog(
                    lastEvents: [new DataInLogRecord(sourceId: 'src-1'),
                                 new DataInLogRecord(sourceId: 'src-2')],
                    lastErrors: [],
                    lastLogs: [new DeviceLogRecord(message: 'test message')]
            ))
            beans.collect { it.name } == ['dev-A', 'dev-Z']
            beans[0].lastEvent.sourceId == 'src-1'
            beans[0].lastError == null
            beans[0].lastLog.message == 'test message'
    }

    def "should get device events"() {
        when:
            def bean = service.getDeviceEvents('dev-123')
        then:
            1 * repository.getNameAndClientById('dev-123') >> Optional.of(new Node(name: 'testing device', clientId: 'client-123'))
            1 * logRepository.findById('client-123') >> Optional.of(new DeviceLog(
                    lastEvents: [new DataInLogRecord(sourceId: 'src-1'),
                                 new DataInLogRecord(sourceId: 'src-2')],
                    lastErrors: [],
                    lastLogs: [new DeviceLogRecord(message: 'test message')]
            ))

            bean.name == 'testing device'
            bean.events.collect { it.sourceId } == ['src-1', 'src-2']
            bean.errors == []
            bean.logs.collect { it.message } == ['test message']
    }

    def "should get events for device without clientId"() {
        when:
            def bean = service.getDeviceEvents('dev-123')
        then:
            1 * repository.getNameAndClientById('dev-123') >> Optional.of(new Node(name: 'testing device'))
            0 * logRepository.findById(_)
            bean.name == 'testing device'
            bean.events == []
            bean.errors == []
    }

    @Unroll
    def "should toggle device activity from #currentValue to #expectedValue"() {
        when:
            def result = service.toggleActive('dev-123')
        then:
            1 * repository.findByIdAndType('dev-123', 'DEVICE') >>
                    Optional.of(new Node(name: 'testing device', isActive: currentValue))
            1 * mongoTemplate.updateFirst(*_)
            result == expectedValue
        where:
            currentValue | expectedValue
            null         | false
            false        | true
            true         | false
    }

    def "should get own node for device"() {
        when:
            service.getOwnDevice()
        then:
            1 * currentUser.getUsername() >> 'device-client-id'
            1 * repository.findByClientId('device-client-id') >> Optional.of(new Node())
    }

}
