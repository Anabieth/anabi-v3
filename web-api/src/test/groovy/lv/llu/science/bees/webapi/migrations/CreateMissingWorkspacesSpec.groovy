package lv.llu.science.bees.webapi.migrations

import lv.llu.science.bees.webapi.domain.nodes.Node
import lv.llu.science.bees.webapi.domain.workspaces.Workspace
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import static org.springframework.data.mongodb.core.query.Criteria.where
import static org.springframework.data.mongodb.core.query.Query.query

@SpringBootTest
@ActiveProfiles("embedded")
class CreateMissingWorkspacesSpec extends Specification {

    @Autowired
    MongoOperations operations

    def setup() {
        operations.remove(new Query(), Workspace.class)
        operations.remove(new Query(), Node.class)
    }


    def "should create missing workspaces and update nodes"() {
        given:
            operations.save(new Node(id: 'a', owner: 'userA'))
            operations.save(new Node(id: 'b', owner: 'userB'))

            def job = new CreateMissingWorkspaces(operations)
        when:
            job.execute()
        then:
            def wsA = operations.findOne(query(where('owner').is('userA')), Workspace.class)
            def wsB = operations.findOne(query(where('owner').is('userB')), Workspace.class)
            def nodeA = operations.findById('a', Node.class)
            def nodeB = operations.findById('b', Node.class)

            wsA.owner == nodeA.owner
            wsB.owner == nodeB.owner
            wsA.name == 'My workspace'
            wsB.name == 'My workspace'
            nodeA.workspaceId == wsA.id
            nodeB.workspaceId == wsB.id
            nodeA.createdBy == wsA.owner
            nodeB.createdBy == wsB.owner
    }

    def "should handle node owners with multiple workspaces"() {
        given:
            operations.save(new Node(id: 'a', owner: 'userA'))
            operations.save(new Workspace(id: 'ws-1', owner: 'userA'))
            operations.save(new Workspace(id: 'ws-2', owner: 'userA'))

            def job = new CreateMissingWorkspaces(operations)
        when:
            job.execute()
        then:
            operations.count(query(where('owner').is('userA')), Workspace.class) == 2
            def node = operations.findById('a', Node.class)
            node.workspaceId == 'ws-1'
    }

    def "should not change nodes with existing workspaces"() {
        given:
            operations.save(new Node(id: 'a', owner: 'userA', workspaceId: 'ws-1'))
            operations.save(new Workspace(id: 'ws-1', owner: 'userA', name: 'Custom workspace'))

            def job = new CreateMissingWorkspaces(operations)
        when:
            job.execute()
        then:
            def workspaces = operations.find(query(where('owner').is('userA')), Workspace.class)
            workspaces.size() == 1
            workspaces[0].name == 'Custom workspace'

            def node = operations.findById('a', Node.class)
            node.workspaceId == 'ws-1'
    }
}
