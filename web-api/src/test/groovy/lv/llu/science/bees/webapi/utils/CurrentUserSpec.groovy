package lv.llu.science.bees.webapi.utils

import lv.llu.science.bees.webapi.domain.models.ModelDefinition
import lv.llu.science.bees.webapi.domain.models.ModelRepository
import lv.llu.science.bees.webapi.domain.nodes.Node
import lv.llu.science.bees.webapi.domain.nodes.NodeRepository
import lv.llu.science.bees.webapi.domain.workspaces.Workspace
import lv.llu.science.bees.webapi.domain.workspaces.WorkspaceRepository
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import spock.lang.Specification
import spock.lang.Unroll

import static java.util.Optional.empty
import static java.util.Optional.of

class CurrentUserSpec extends Specification {
    def repository = Mock(NodeRepository) {
        findWorkspaceById('userNode') >> of(new Node(workspaceId: 'userWorkspace'))
        findWorkspaceById('sharedNode') >> of(new Node(workspaceId: 'sharedWorkspace'))
        findWorkspaceById('otherNode') >> of(new Node(workspaceId: 'otherWorkspace'))
        findWorkspaceById(_) >> empty()

        findAllowedDeviceInWorkspace('userWorkspace', 'userDevice') >> of(new Node())
        findAllowedDeviceInWorkspace(_, _) >> empty()
    }
    def workspaceRepository = Mock(WorkspaceRepository) {
        findById('userWorkspace') >> of(new Workspace(owner: 'user'))
        findById('sharedWorkspace') >> of(new Workspace(owner: 'sharedUser', invitedUsers: ['user']))
        findById('otherWorkspace') >> of(new Workspace(owner: 'otherUser'))
        findById(_) >> empty()
    }

    def modelRepository = Mock(ModelRepository) {
        findById('userModel') >> of(new ModelDefinition(workspaceId: 'userWorkspace'))
        findById('sharedModel') >> of(new ModelDefinition(workspaceId: 'sharedWorkspace'))
        findById('otherModel') >> of(new ModelDefinition(workspaceId: 'otherWorkspace'))
        findById(_) >> empty()
    }

    def currentUser = new CurrentUser(repository, workspaceRepository, modelRepository)
    def user = new User("user", "pass", [])

    def setup() {
        setupContext("user")
    }

    def cleanup() {
        SecurityContextHolder.clearContext()
    }

    def setupContext(Object user, List<String> roles = []) {
        def context = Stub(SecurityContext) {
            getAuthentication() >> Stub(Authentication) {
                getPrincipal() >> user
                getAuthorities() >> roles.collect { new SimpleGrantedAuthority("SCOPE_${it}") }
            }
        }
        SecurityContextHolder.setContext(context)
    }

    def "should get current user name from user details"() {
        expect:
            currentUser.getUsername() == "user"
    }

    def "should use JWT principal as user name "() {
        given:
            setupContext("jwt-user")
        expect:
            currentUser.getUsername() == "jwt-user"
    }

    @Unroll
    def "should check node access - #label"() {
        expect:
            currentUser.hasNodeAccess(node) == result
        where:
            label          | node         | result
            'allow owner'  | 'userNode'   | true
            'allow shared' | 'sharedNode' | true
            'deny other'   | 'otherNode'  | false
            'deny unknown' | 'randomNode' | false
    }

    def "should check node access - deny with device role"() {
        given:
            setupContext('user', ['device'])
        expect:
            !currentUser.hasNodeAccess('userNode')
    }

    def "should check node access - allow admin"() {
        given:
            setupContext('admin', ['admin'])
        expect:
            currentUser.hasNodeAccess('userNode')
    }

    def "should check allowed device - allow owner device"() {
        given:
            setupContext('userDevice', ['device'])
        expect:
            currentUser.isAllowedDevice('userNode')
    }

    def "should check allowed device - deny shared device"() {
        given:
            setupContext('sharedDevice', ['device'])
        expect:
            !currentUser.isAllowedDevice('userNode')
    }

    def "should check allowed device - deny without role"() {
        given:
            setupContext('userDevice', ['other', 'roles'])
        expect:
            !currentUser.isAllowedDevice('userNode')
    }

    def "should check allowed device - deny unknown device"() {
        given:
            setupContext('otherDevice', ['device'])
        expect:
            !currentUser.isAllowedDevice('userNode')
    }

    def "should check allowed device - deny unknown node"() {
        given:
            setupContext('userDevice', ['device'])
        expect:
            !currentUser.isAllowedDevice('randomNode')
    }

    def "should check workspace owner - allow owner"() {
        expect:
            currentUser.isWorkspaceOwner('userWorkspace')
    }

    def "should check workspace owner - deny non-owner"() {
        expect:
            !currentUser.isWorkspaceOwner('sharedWorkspace')
    }

    def "should check workspace access - allow owner"() {
        expect:
            currentUser.hasWorkspaceAccess('userWorkspace')
    }

    def "should check workspace access - allow participant"() {
        expect:
            currentUser.hasWorkspaceAccess('sharedWorkspace')
    }

    def "should check workspace access - deny others"() {
        expect:
            !currentUser.hasWorkspaceAccess('otherWorkspace')
    }

    def "should check workspace access - allow admin"() {
        given:
            setupContext('admin', ['admin'])
        expect:
            currentUser.hasNodeAccess('userNode')
    }

    def "should check HW engineer"() {
        given:
            setupContext('john', ['hw'])
        expect:
            currentUser.isHwEngineer()
    }

    @Unroll
    def "should check model access - #label"() {
        expect:
            currentUser.hasModelAccess(model) == result
        where:
            label          | model         | result
            'allow owner'  | 'userModel'   | true
            'allow shared' | 'sharedModel' | true
            'deny other'   | 'otherModel'  | false
            'deny unknown' | 'randomModel' | false
    }
}