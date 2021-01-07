package lv.llu.science.bees.webapi.domain.nodes


import lv.llu.science.bees.webapi.domain.nodes.latestValues.NodeLatestValueService
import lv.llu.science.bees.webapi.domain.workspaces.ActiveWorkspace
import lv.llu.science.bees.webapi.domain.workspaces.Workspace
import lv.llu.science.bees.webapi.domain.workspaces.WorkspaceRepository
import lv.llu.science.bees.webapi.dwh.DwhValueBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

@SpringBootTest
@WithMockUser
@ActiveProfiles('embedded')
class NodeSecuritySpec extends Specification {

    @Autowired
    NodeService service

    @Autowired
    NodeLatestValueService latestValueService;

    @Autowired
    NodeRepository repository

    @Autowired
    WorkspaceRepository workspaceRepository

    @Autowired
    ActiveWorkspace activeWorkspace

    def setup() {
        workspaceRepository.save(new Workspace(id: '10', owner: 'user'))
        workspaceRepository.save(new Workspace(id: '20', owner: 'otherUser'))
        workspaceRepository.save(new Workspace(id: '30', owner: 'sharedUser', invitedUsers: ['user']))

        repository.save(new Node(id: '100', workspaceId: '10', type: 'HIVE'))
        repository.save(new Node(id: '110', workspaceId: '10', type: 'OTHER', parentId: '100'))
        repository.save(new Node(id: '120', workspaceId: '10', type: 'DEVICE', parentId: '100', clientId: 'dev', isActive: true))

        repository.save(new Node(id: '200', workspaceId: '20', type: 'HIVE'))
        repository.save(new Node(id: '210', workspaceId: '20', type: 'OTHER', parentId: '200'))
        repository.save(new Node(id: '211', workspaceId: '20', type: 'OTHER', parentId: '210'))
        repository.save(new Node(id: '220', workspaceId: '20', type: 'DEVICE', parentId: '200', clientId: 'otherDev', isActive: true))
        repository.save(new Node(id: '230', workspaceId: '20', type: 'DEVICE', parentId: '200', clientId: 'otherInactiveDev'))

        repository.save(new Node(id: '300', workspaceId: '30', type: 'HIVE'))
        repository.save(new Node(id: '310', workspaceId: '30', type: 'OTHER', parentId: '300'))
        repository.save(new Node(id: '311', workspaceId: '30', type: 'OTHER', parentId: '310'))
        repository.save(new Node(id: '320', workspaceId: '30', type: 'DEVICE', parentId: '300', clientId: 'sharedDev', isActive: true))
    }

    def cleanup() {
        // check that no invalid nodes created during tests
        assert repository.findAllByWorkspaceId(null) == []

        repository.deleteAll()
        workspaceRepository.deleteAll()
    }


    def "should create node"() {
        given:
            activeWorkspace.setId('10')
        expect:
            service.createNode(new NodeBean(name: 'One')).id != null
    }

    def "should create node with parent"() {
        given:
            activeWorkspace.setId('10')
        expect:
            service.createNode(new NodeBean(name: 'Child', parentId: '110')).id != null
    }

    def "should deny create node without active workspace"() {
        when:
            service.createNode(new NodeBean(name: 'One'))
        then:
            thrown(AccessDeniedException)
    }

    def "should deny create node with other's parent"() {
        given:
            activeWorkspace.setId('10')
        when:
            service.createNode(new NodeBean(name: 'Child', parentId: '210'))
        then:
            thrown(AccessDeniedException)
    }

    def "should deny create node with parent from other workspace"() {
        given:
            activeWorkspace.setId('10')
        when:
            service.createNode(new NodeBean(name: 'Child', parentId: '310'))
        then:
            thrown(ResponseStatusException)
    }

    def "should get user nodes"() {
        given:
            activeWorkspace.setId('10')
        expect:
            service.getAllNodes().collect { it.id } == ['100', '110', '120']
    }

    def "should get user nodes from shared workspace"() {
        given:
            activeWorkspace.setId('30')
        expect:
            service.getAllNodes().collect { it.id } == ['300', '310', '311', '320']
    }

    def "should deny user nodes without active workspace"() {
        when:
            service.getAllNodes()
        then:
            thrown(AccessDeniedException)
    }

    def "should edit user node"() {
        expect:
            service.editNode('110', new NodeBean(name: 'One name')).name == 'One name'
    }

    def "should edit node from shared workspace"() {
        expect:
            service.editNode('310', new NodeBean(name: 'Third name')).name == 'Third name'
    }

    def "should deny edit other user node"() {
        when:
            service.editNode('210', new NodeBean(name: 'Other name'))
        then:
            thrown(AccessDeniedException)
    }

    def "should get user node details"() {
        expect:
            service.getNodeDetails('120') != null
    }

    def "should get user node details from shared workspace"() {
        expect:
            service.getNodeDetails('320') != null
    }

    def "should deny other user node details"() {
        when:
            service.getNodeDetails('220')
        then:
            thrown(AccessDeniedException)
    }

    def "should get user node"() {
        expect:
            repository.getNode('100') != null
    }

    def "should get shared node"() {
        expect:
            repository.getNode('300') != null
    }

    @WithMockUser(username = 'dev', authorities = ['SCOPE_device'])
    def "should get node as device"() {
        expect:
            repository.getNode('100') != null
    }

    def "should deny other user node"() {
        when:
            repository.getNode('210')
        then:
            thrown(AccessDeniedException)
    }

    @WithMockUser(username = 'dev', authorities = ['SCOPE_device'])
    def "should deny shared node as device"() {
        when:
            repository.getNode('300')
        then:
            thrown(AccessDeniedException)
    }

    @WithMockUser(username = 'devOther', authorities = ['SCOPE_device'])
    def "should deny node as other device"() {
        when:
            repository.getNode('100') != null
        then:
            thrown(AccessDeniedException)
    }

    def "should get user devices"() {
        given:
            activeWorkspace.setId('10')
        expect:
            service.getDevices().collect { it.id } == ['120']
    }

    def "should get shared devices"() {
        given:
            activeWorkspace.setId('30')
        expect:
            service.getDevices().collect { it.id } == ['320']
    }

    def "should deny devices without active workspace"() {
        when:
            service.getDevices()
        then:
            thrown(AccessDeniedException)
    }

    @WithMockUser(username = 'dev', authorities = ['SCOPE_device'])
    def "should push latest events"() {
        expect:
            latestValueService.pushLatestValues('100', 'test', [new DwhValueBean(value: 1), new DwhValueBean(value: 2)])
    }

    @WithMockUser(username = 'sharedDev', authorities = ['SCOPE_device'])
    def "should deny latest events from shared device"() {
        when:
            latestValueService.pushLatestValues('100', 'test', [])
        then:
            thrown(AccessDeniedException)
    }

    @WithMockUser(username = 'otherDev', authorities = ['SCOPE_device'])
    def "should deny latest events from other device"() {
        when:
            latestValueService.pushLatestValues('100', 'test', [])
        then:
            thrown(AccessDeniedException)
    }

}
