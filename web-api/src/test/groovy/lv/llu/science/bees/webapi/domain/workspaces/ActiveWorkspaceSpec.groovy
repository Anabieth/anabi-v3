package lv.llu.science.bees.webapi.domain.workspaces

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@SpringBootTest
@ActiveProfiles("embedded")
@WithMockUser
class ActiveWorkspaceSpec extends Specification {

    @Autowired
    WorkspaceRepository repository

    @Autowired
    ActiveWorkspace activeWorkspace

    def setup() {
        repository.save(new Workspace(id: '100', owner: 'user'))
        repository.save(new Workspace(id: '200', owner: 'otherUser', invitedUsers: ['user']))
        repository.save(new Workspace(id: '300', owner: 'otherUser', invitedUsers: ['randomUser']))
    }

    def cleanup() {
        repository.deleteAll()
    }

    def "should set active own workspace"() {
        expect:
            activeWorkspace.setId('100')
    }

    def "should set active shared workspace"() {
        expect:
            activeWorkspace.setId('200')
    }

    def "should deny other workspace"() {
        when:
            activeWorkspace.setId('300')
        then:
            thrown(AccessDeniedException)
    }
}
