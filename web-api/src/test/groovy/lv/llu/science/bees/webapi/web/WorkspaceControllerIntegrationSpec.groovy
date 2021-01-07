package lv.llu.science.bees.webapi.web

import groovy.json.JsonOutput
import lv.llu.science.bees.webapi.domain.auth0.SamsUsers
import lv.llu.science.bees.webapi.domain.workspaces.Workspace
import lv.llu.science.bees.webapi.web.helpers.RestDocsSpecification
import org.springframework.beans.factory.annotation.Autowired

import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*
import static org.springframework.restdocs.payload.PayloadDocumentation.*
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class WorkspaceControllerIntegrationSpec extends RestDocsSpecification {


    @Autowired
    SamsUsers samsUsers

    def setup() {
        samsUsers.getUserMap().put('john', 'John The Tester')

        workspaceRepository.insert(new Workspace(id: 'ws1', name: 'First workspace', owner: 'user',
                sharingKey: '123-456-789', invitedUsers: ['john']))
        workspaceRepository.insert(new Workspace(id: 'ws2', name: 'Second workspace', owner: 'user'))
        workspaceRepository.insert(new Workspace(id: 'ws9', name: 'Shared workspace', owner: 'otherUser',
                sharingKey: '987-654-321'))
    }

    def cleanup() {
        workspaceRepository.deleteAll()
    }

    def "should list workspaces"() {
        expect:
            mvc.perform(
                    get("/workspaces"))
                    .andExpect(status().isOk())
                    .andDo(document('workspace-list',
                            responseFields(
                                    fieldWithPath('[].id').description('Workspace id'),
                                    fieldWithPath('[].name').description('Workspace name'),
                                    fieldWithPath('[].isOwner').description('Is current user owns the workspace'),
                                    fieldWithPath('[].owner').description('Owner username'),
                                    fieldWithPath('[].sharingKey').optional().description('Unique key used within invitation'),
                                    fieldWithPath('[].invitedUsers[]').optional().description('List of users with an access to the shared workspace'),
                                    fieldWithPath('[].invitedUsers[].*').ignored(),
                            )
                    ))
    }

    def "should create workspace"() {
        given:
            def request = [name: 'Other workspace']
        expect:
            mvc.perform(
                    post("/workspaces")
                            .contentType(APPLICATION_JSON)
                            .content(JsonOutput.toJson(request)))
                    .andExpect(status().isOk())
                    .andDo(document('workspace-add',
                            requestFields(
                                    fieldWithPath('name').description('Name of new workspace'),
                            ),
                    ))
    }

    def "should update workspace"() {
        given:
            def request = [name: 'New workspace name']
        expect:
            mvc.perform(
                    put('/workspaces/{id}', 'ws1')
                            .contentType(APPLICATION_JSON)
                            .content(JsonOutput.toJson(request)))
                    .andExpect(status().isOk())
                    .andDo(document('workspace-edit',
                            pathParameters(
                                    parameterWithName('id').description('Workspace id'),
                            ),
                            requestFields(
                                    fieldWithPath('name').description('New name of the workspace'),
                            ),
                    ))
    }

    def "should delete workspace"() {
        expect:
            mvc.perform(
                    delete('/workspaces/{id}', 'ws1'))
                    .andExpect(status().is(204))
                    .andDo(document('workspace-delete',
                            pathParameters(
                                    parameterWithName('id').description('Workspace id'),
                            )
                    ))
    }

    def "should share workspace"() {
        expect:
            mvc.perform(
                    post('/workspaces/{id}/share', 'ws2'))
                    .andExpect(status().isOk())
                    .andDo(document('workspace-share',
                            pathParameters(
                                    parameterWithName('id').description('Workspace id'),
                            ),
                            relaxedResponseFields(
                                    fieldWithPath('sharingKey').description('Unique key used for invitation')
                            )

                    ))
    }

    def "should unshare workspace"() {
        expect:
            mvc.perform(
                    delete('/workspaces/{id}/share', 'ws2'))
                    .andExpect(status().isOk())
                    .andDo(document('workspace-unshare',
                            pathParameters(
                                    parameterWithName('id').description('Workspace id'),
                            )
                    ))
    }

    def "should accept invite"() {
        given:
            def request = [key: '987-654-321', confirm: true]
        expect:
            mvc.perform(
                    post('/workspaces/{id}/invite', 'ws9')
                            .contentType(APPLICATION_JSON)
                            .content(JsonOutput.toJson(request)))
                    .andExpect(status().isOk())
                    .andDo(document('workspace-invite',
                            pathParameters(
                                    parameterWithName('id').description('Workspace id'),
                            ),
                            requestFields(
                                    fieldWithPath('key').description('Invitation key'),
                                    fieldWithPath('confirm').description('`True` to join the workspace +\n`False` to get invitation details without joining'),
                            ),
                            responseFields(
                                    fieldWithPath('workspace.*').description('Workspace details'),
                                    fieldWithPath('status')
                                            .description('Invitation status, possible values are: +\n' +
                                                    '`Valid` - invitation is valid and ready for accepting +\n' +
                                                    '`Joined` - used joined the workspace +\n' +
                                                    '`AlreadyJoined` - trying to join the workspace once again +\n' +
                                                    '`OwnWorkspace` - trying to join own workspace +\n' +
                                                    '`NotValid` - invitation is not valid'
                                            )
                            )
                    ))
    }

    def "should remove user"() {
        given:
            def request = [userName: 'john']
        expect:
            mvc.perform(
                    delete('/workspaces/{id}/share/users', 'ws1')
                            .contentType(APPLICATION_JSON)
                            .content(JsonOutput.toJson(request)))
                    .andExpect(status().isOk())
                    .andDo(document('workspace-remove',
                            pathParameters(
                                    parameterWithName('id').description('Workspace id'),
                            ),
                            requestFields(
                                    fieldWithPath('userName').description('User name to remove from the workspace. Specify own username to "leave" from the workspace.'),
                            )
                    ))
    }
}
