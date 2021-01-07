package lv.llu.science.bees.webapi.domain.models

import lv.llu.science.bees.webapi.domain.nodes.Node
import lv.llu.science.bees.webapi.domain.nodes.NodeRepository
import lv.llu.science.bees.webapi.domain.workspaces.ActiveWorkspace
import lv.llu.science.bees.webapi.dwh.DwhClient
import lv.llu.science.bees.webapi.utils.CurrentUser
import lv.llu.science.bees.webapi.utils.ModelMapperEx
import lv.llu.science.bees.webapi.utils.NotFoundException
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

class ModelServiceSpec extends Specification {

    def repository = Mock(ModelRepository)
    def dwhClient = Mock(DwhClient)
    def mapper = new ModelMapperEx()
    def currentUser = Mock(CurrentUser)
    def activeWorkspace = Mock(ActiveWorkspace)
    def nodeRepository = Mock(NodeRepository)
    def operations = Mock(MongoOperations)

    def service = new ModelService(repository, dwhClient, mapper, currentUser, activeWorkspace, nodeRepository, operations)

    def "should list model definitions"() {
        when:
            def list = service.listModelDefinitions()
        then:
            1 * activeWorkspace.getId() >> 'ws-123'
            1 * repository.findAllByWorkspaceId('ws-123') >> [new ModelDefinition(id: '111'), new ModelDefinition(id: '222')]
            list.collect { it.id } == ['111', '222']
    }

    def "should create model definition"() {
        given:
            def bean = new ModelDefinitionBean(modelCode: 'test', params: [:])
            def entity = new ModelDefinition(modelCode: 'test', workspaceId: 'ws-123', params: [:])
        when:
            def result = service.createModelDefinition(bean)
        then:
            1 * activeWorkspace.getId() >> 'ws-123'
            2 * dwhClient.getModelList() >> [new ModelTemplate(code: 'test', params: [new ModelTemplateParam(code: 'a')])]
            1 * operations.exists(_, ModelDefinition.class) >> false
            1 * repository.save(entity) >> entity
            1 * dwhClient.saveModelDefinition('test', _)
            result.modelCode == 'test'
    }

    def "should edit model definition"() {
        given:
            def bean = new ModelDefinitionBean(modelCode: 'test', params: [:])
            def entity = new ModelDefinition(modelCode: 'test', workspaceId: 'ws-123', params: [:])
        when:
            service.editModelDefinition('987', bean)
        then:
            2 * dwhClient.getModelList() >> [new ModelTemplate(code: 'test', params: [new ModelTemplateParam(code: 'a')])]
            1 * repository.findById('987') >> Optional.of(entity)
            1 * operations.exists(_, ModelDefinition.class) >> false
            1 * repository.save(entity) >> entity
            1 * dwhClient.saveModelDefinition('test', _)
    }

    def "should deny creating duplicate model definitions"() {
        given:
            def bean = new ModelDefinitionBean(modelCode: 'test', params: [:])
        when:
            service.createModelDefinition(bean)
        then:
            2 * dwhClient.getModelList() >> [new ModelTemplate(code: 'test', params: [new ModelTemplateParam(code: 'a')])]
            1 * operations.exists(_, ModelDefinition.class) >> true
            0 * repository.save(_)
            0 * dwhClient.saveModelDefinition(_, _)
            thrown(ResponseStatusException)
    }

    def "should check access to model parameters"() {
        given:
            dwhClient.getModelList() >> [new ModelTemplate(
                    code: 'test',
                    params: [
                            new ModelTemplateParam(code: 'a', type: 'nodeId'),
                            new ModelTemplateParam(code: 'b', type: 'nodeId'),
                            new ModelTemplateParam(code: 'c', type: 'number')
                    ])]
            activeWorkspace.getId() >> 'active-ws'
            nodeRepository.getNode('node-unknown') >> { throw new NotFoundException() }
            nodeRepository.getNode('node-other-ws') >> new Node(workspaceId: 'other-ws')
            nodeRepository.getNode('node-valid') >> new Node(workspaceId: 'active-ws')
            repository.save(_) >> new ModelDefinition()

        when: 'unknown model'
            def bean = new ModelDefinitionBean(modelCode: 'unknown')
            service.createModelDefinition(bean)
        then:
            thrown(NotFoundException)

        when: 'unknown node'
            bean = new ModelDefinitionBean(modelCode: 'test', params: [a: 'node-unknown', 'x': 'fake-node-id'])
            service.createModelDefinition(bean)
        then:
            thrown(NotFoundException)

        when: 'node from another workspace'
            bean = new ModelDefinitionBean(modelCode: 'test', params: [a: 'node-other-ws', 'b': 'node-valid'])
            service.createModelDefinition(bean)
        then:
            thrown(ResponseStatusException)

        when: 'all nodes from valid workspace'
            bean = new ModelDefinitionBean(modelCode: 'test', params: [a: 'node-valid', 'b': 'node-valid'])
            service.createModelDefinition(bean)
        then:
            notThrown(RuntimeException)
    }

    def "should get model definition"() {
        when:
            service.getModelDefinition('123')
        then:
            1 * repository.findById('123') >> Optional.of(new ModelDefinition())
    }

    def "should delete model definition"() {
        when:
            service.deleteModelDefinition('123')
        then:
            1 * repository.findById('123') >> Optional.of(new ModelDefinition(modelCode: 'test', id: '123'))
            1 * repository.delete(_)
            1 * dwhClient.deleteModelDefinition('test', '123')
    }

    def "should list model templates"() {
        when:
            service.listModelTemplates()
        then:
            1 * dwhClient.getModelList()
    }
}
