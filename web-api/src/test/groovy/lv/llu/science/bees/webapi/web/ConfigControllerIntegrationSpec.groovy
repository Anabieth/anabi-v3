package lv.llu.science.bees.webapi.web

import groovy.json.JsonOutput
import lv.llu.science.bees.webapi.domain.configs.Config
import lv.llu.science.bees.webapi.domain.configs.ConfigRepository
import lv.llu.science.bees.webapi.domain.nodes.Node
import lv.llu.science.bees.webapi.web.helpers.RestDocsSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser

import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*
import static org.springframework.restdocs.payload.PayloadDocumentation.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WithMockUser(authorities = ['SCOPE_hw'])
class ConfigControllerIntegrationSpec extends RestDocsSpecification {

    @Autowired
    ConfigRepository configRepository

    def setup() {
        setupDefaultWorkspace()
        setupDefaultNodes()

        configRepository.insert([
                new Config(id: 'conf1', name: 'First demo config', isDefault: true, config: [paramA: 123, paramB: 'demo']),
                new Config(id: 'conf2', name: 'Second demo config', config: [paramC: ['list', 'of', 'values']]),
        ])

        nodeRepository.insert(new Node(id: 'dev1', name: 'Demo device', type: 'DEVICE', workspaceId: 'ws1',
                hwConfigId: 'conf2', clientId: 'demo-device'))
    }

    def cleanup() {
        configRepository.deleteAll()
    }

    def "should get config list"() {
        expect:
            mvc.perform(
                    get('/configs'))
                    .andExpect(status().isOk())
                    .andDo(document('config-list',
                            responseFields(
                                    fieldWithPath('[].id').description('Configuration ID'),
                                    fieldWithPath('[].name').description('Configuration name'),
                                    fieldWithPath('[].isDefault').optional().description('Indicates default configuration'),
                                    fieldWithPath('[].devices[].*').optional().description('List of devices assigned to the config'),
                                    subsectionWithPath('[].config').description('Config as nested JSON object'),
                            )
                    ))
    }

    def "should get config details"() {
        expect:
            mvc.perform(
                    get('/configs/conf1'))
                    .andExpect(status().isOk())
                    .andDo(document('config-details'))
    }

    def "should add new config"() {
        given:
            def request = [name: 'New demo config', config: [param: 123]]
        expect:
            mvc.perform(
                    post('/configs').contentType(APPLICATION_JSON)
                            .content(JsonOutput.toJson(request)))
                    .andExpect(status().isOk())
                    .andDo(document('config-create'))
    }


    def "should save existing config"() {
        given:
            def request = [name: 'Edited config', config: [param: 987]]
        expect:
            mvc.perform(
                    put('/configs/conf1').contentType(APPLICATION_JSON)
                            .content(JsonOutput.toJson(request)))
                    .andExpect(status().isOk())
                    .andDo(document('config-edit'))
    }

    def "should delete config"() {
        expect:
            mvc.perform(
                    delete('/configs/conf1'))
                    .andExpect(status().is(204))
                    .andDo(document('config-delete'))
    }

    def "should set default config"() {
        expect:
            mvc.perform(
                    put('/configs/conf2/default').contentType(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(document('config-set-default'))
    }


    @WithMockUser(username = 'demo-device', authorities = ['SCOPE_device'])
    def "should get device config"() {
        expect:
            mvc.perform(
                    get('/configs/device'))
                    .andExpect(status().isOk())
                    .andDo(document('config-device'))
    }

}
