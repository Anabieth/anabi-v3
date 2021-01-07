package lv.llu.science.bees.webapi.domain.workspaces

import lv.llu.science.bees.webapi.domain.auth0.SamsUsers
import lv.llu.science.bees.webapi.domain.nodes.NodeRepository
import lv.llu.science.bees.webapi.utils.CurrentUser
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

import static lv.llu.science.bees.webapi.domain.workspaces.WorkspaceInviteResultBean.Status.*

class WorkspaceServiceSpec extends Specification {

    def repository = Mock(WorkspaceRepository)
    def nodeRepository = Mock(NodeRepository)
    def currentUser = Mock(CurrentUser) {
        getUsername() >> 'userA'
        isAdmin() >> false
    }
    def samsUsers = Mock(SamsUsers) {
        getUserMap() >> ['john': 'John The Tester']
    }
    def service = new WorkspaceService(repository, nodeRepository, currentUser, samsUsers)

    def 'should get workspace list'() {
        when:
            def list = service.getWorkspaces()
        then:
            1 * repository.findByUser('userA') >> [
                    new Workspace(id: '111', name: 'One', owner: 'userA',
                            invitedUsers: ['john', 'randomUser'],
                            sharingKey: 'key-111'
                    ),
                    new Workspace(id: '222', name: 'Two', owner: 'userB',
                            invitedUsers: ['john'],
                            sharingKey: 'key-222')
            ]

            with(list[0]) {
                it.name == 'One'
                it.isOwner
                it.owner == 'userA'
                it.invitedUsers == [
                        new InvitedUser('john', 'John The Tester'),
                        new InvitedUser('randomUser', 'randomUser')
                ]
                it.sharingKey == 'key-111'
            }

            with(list[1]) {
                it.name == 'Two'
                !it.isOwner
                it.owner == 'userB'
                it.invitedUsers == null
                it.sharingKey == null
            }
    }

    def 'should get workspace list as admin'() {
        when:
            def list = service.getWorkspaces()
        then:
            1 * repository.findAll() >> [new Workspace(name: 'One', owner: 'userA'), new Workspace(name: 'Two'), new Workspace(name: 'Three')]
            1 * currentUser.isAdmin() >> true
            list.size() == 3
    }

    def 'should create default workspace'() {
        when:
            service.getWorkspaces()
        then:
            1 * repository.findByUser(_) >> [new Workspace(name: 'Shared', owner: 'sharedUser')]
            1 * repository.save(_) >> { Workspace ws ->
                assert ws.name == 'My workspace'
                ws
            }
    }

    def 'should create workspace'() {
        when:
            service.createWorkspace(new WorkspaceBean(name: 'new ws'))
        then:
            1 * repository.save({ ws -> ws.name == 'new ws' }) >> new Workspace()
    }

    def 'should not create blank workspace' (){
        when:
            service.createWorkspace(new WorkspaceBean(name:' '))
        then:
            0 * repository.save({ ws -> ws.name == ' ' }) >> new Workspace()
            thrown(ResponseStatusException)
    }

    def 'should update workspace'() {
        when:
            service.updateWorkspace('123', new WorkspaceBean(name: 'new name', sharingKey: 'other fields'))
        then:
            1 * repository.findById('123') >> Optional.of(new Workspace(name: 'old name', sharingKey: 'old key'))
            1 * repository.save({ ws -> ws.name == 'new name' && ws.sharingKey == 'old key' }) >> new Workspace()
    }

    def 'should delete empty workspace'() {
        when:
            service.deleteWorkspace('123')
        then:
            1 * repository.findById('123') >> Optional.of(new Workspace(id: '123'))
            1 * nodeRepository.countByWorkspaceId('123') >> 0
            1 * repository.delete({ ws -> ws.id == '123' })
    }

    def 'should not delete workspace with nodes'() {
        when:
            service.deleteWorkspace('123')
        then:
            1 * repository.findById('123') >> Optional.of(new Workspace(id: '123'))
            1 * nodeRepository.countByWorkspaceId('123') >> 100500
            thrown(ResponseStatusException)
    }

    def 'should share workspace'() {
        when:
            service.shareWorkspace('123')
        then:
            1 * repository.findById('123') >> Optional.of(new Workspace(id: '123'))
            1 * repository.save({ ws -> ws.sharingKey != null }) >> new Workspace()
    }

    def 'should unshare workspace'() {
        when:
            service.unshareWorkspace('123')
        then:
            1 * repository.findById('123') >> Optional.of(new Workspace(id: '123'))
            1 * repository.save({ ws -> ws.sharingKey == null }) >> new Workspace()
    }

    def 'should remove user from workspace'() {
        when:
            service.removeUser('123', 'john')
        then:
            1 * repository.findById('123') >> Optional.of(new Workspace(id: '123', invitedUsers: ['john']))
            1 * repository.save({ ws -> ws.invitedUsers == [] }) >> new Workspace()
    }

    def 'should throw error if cannot remove user from workspace'() {
        when:
            service.removeUser('123', 'john')
        then:
            1 * repository.findById('123') >> Optional.of(new Workspace(id: '123', invitedUsers: []))
            thrown(ResponseStatusException)
    }

    def 'should accept invite'() {
        given:
            def workspace = new Workspace(id: '123', sharingKey: wsKey, owner: wsOwner, invitedUsers: wsUsers)
        when:
            def result = service.acceptInvite('123', new WorkspaceInviteBean(key: 'key-123', confirm: confirm))

        then:
            1 * repository.findById('123') >> Optional.of(workspace)
            saves * repository.save(_) >> new Workspace()
            result.status == resultStatus

        where:
            wsKey      | wsOwner | wsUsers   | confirm | resultStatus  | saves
            'otherKey' | null    | []        | false   | NotValid      | 0
            'key-123'  | 'userA' | []        | false   | OwnWorkspace  | 0
            'key-123'  | 'userB' | ['userA'] | false   | AlreadyJoined | 0
            'key-123'  | 'userB' | []        | false   | Valid         | 0
            'key-123'  | 'userB' | []        | true    | Joined        | 1
    }

}
