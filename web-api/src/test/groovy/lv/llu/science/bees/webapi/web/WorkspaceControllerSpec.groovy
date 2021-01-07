package lv.llu.science.bees.webapi.web

import lv.llu.science.bees.webapi.domain.workspaces.WorkspaceInviteResultBean
import lv.llu.science.bees.webapi.domain.workspaces.WorkspaceService
import spock.lang.Specification

import javax.servlet.http.HttpServletResponse

class WorkspaceControllerSpec extends Specification {

    def service = Mock(WorkspaceService)
    def controller = new WorkspaceController(service)

    def "should list workspaces"() {
        when:
            controller.listWorkspaces()
        then:
            1 * service.getWorkspaces()
    }

    def "should create workspace"() {
        when:
            controller.createWorkspace(null)
        then:
            1 * service.createWorkspace(_)
    }

    def "should update workspace"() {
        when:
            controller.updateWorkspace('123', null)
        then:
            1 * service.updateWorkspace('123', _)
    }

    def "should delete workspace"() {
        given:
            def response = Mock(HttpServletResponse)
        when:
            controller.deleteWorkspace('123', response)
        then:
            1 * service.deleteWorkspace('123')
            1 * response.setStatus(204)
    }

    def "should share workspace"() {
        when:
            controller.shareWorkspace('123')
        then:
            1 * service.shareWorkspace('123')
    }

    def "should unshare workspace"() {
        when:
            controller.unshareWorkspace('123')
        then:
            1 * service.unshareWorkspace('123')
    }

    def "should remove user"() {
        when:
            controller.removeUser('123', ['userName': 'john'])
        then:
            1 * service.removeUser('123', 'john')
    }

    def "should accept invite"() {
        given:
            def response = Mock(HttpServletResponse)
        when:
            controller.acceptInvite('123', null, response)
        then:
            1 * service.acceptInvite('123', _) >> new WorkspaceInviteResultBean(status)
            1 * response.setStatus(code)
        where:
            status                                         | code
            WorkspaceInviteResultBean.Status.Valid         | 200
            WorkspaceInviteResultBean.Status.Joined        | 200
            WorkspaceInviteResultBean.Status.AlreadyJoined | 409
            WorkspaceInviteResultBean.Status.OwnWorkspace  | 409
            WorkspaceInviteResultBean.Status.NotValid      | 404


    }
}
