package lv.llu.science.bees.webapi.domain.datain

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.stubbing.Scenario
import lv.llu.science.bees.webapi.domain.mapping.MappingRepository
import lv.llu.science.bees.webapi.domain.mapping.SourceMapping
import lv.llu.science.bees.webapi.domain.nodes.Node
import lv.llu.science.bees.webapi.domain.nodes.NodeRepository
import lv.llu.science.bees.webapi.domain.workspaces.Workspace
import lv.llu.science.bees.webapi.domain.workspaces.WorkspaceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.hamcrest.Matchers.is
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@ActiveProfiles(["embedded", "mockDwh"])
@AutoConfigureMockMvc
@WithMockUser
@AutoConfigureWireMock(port = 8765)
class DataInServiceIntegrationSpec extends Specification {

    @Autowired
    MockMvc mvc

    @Autowired
    ObjectMapper mapper

    @Autowired
    WorkspaceRepository workspaceRepository
    @Autowired
    NodeRepository nodeRepository
    @Autowired
    MappingRepository mappingRepository

    def setup() {
        workspaceRepository.save(new Workspace(id: 'userWorkspace', owner: 'user'))

        nodeRepository.save(new Node(id: 'targetNode', workspaceId: 'userWorkspace'))
        nodeRepository.save(new Node(id: 'senderDevice', workspaceId: 'userWorkspace', type: 'DEVICE', clientId: 'deviceClient', isActive: true))

        mappingRepository.save(new SourceMapping(sourceId: 'sensor-1', nodeId: 'targetNode', valueKey: 'userParam'))
        mappingRepository.save(new SourceMapping(sourceId: 'sensor-3', nodeId: 'otherNode', valueKey: 'userParam'))
        mappingRepository.save(new SourceMapping(sourceId: 'sensor-4', nodeId: 'targetNode', valueKey: 'otherParam'))
    }

    @WithMockUser(username = "deviceClient", authorities = ['SCOPE_device'])
    def "should post measurements and return status"() {
        given:
            WireMock.stubFor(WireMock.post("/dwh")
                    .inScenario("DWH")
                    .whenScenarioStateIs(Scenario.STARTED)
                    .willReturn(WireMock.ok())
                    .willSetStateTo('Offline'))

            WireMock.stubFor(WireMock.post("/dwh")
                    .inScenario("DWH")
                    .whenScenarioStateIs('Offline')
                    .willReturn(WireMock.notFound()))


            def createBean = { id ->
                [
                        sourceId: id,
                        values  : [
                                [ts: '2019-06-14T14:35:00Z', value: 2.5],
                                [ts: '2019-06-14T14:35:00Z', values: [1.5, 2.5, 3.5]]
                        ]
                ]
            }

            def request = [
                    createBean('sensor-1'),
                    createBean('sensor-2'),
                    createBean('sensor-3'),
                    createBean('sensor-4')
            ]

        expect:
            mvc.perform(
                    post("/data")
                            .contentType(APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$.sensor-1', is('Ok')))
                    .andExpect(jsonPath('$.sensor-2', is('NotFound')))
                    .andExpect(jsonPath('$.sensor-3', is('AccessDenied')))
                    .andExpect(jsonPath('$.sensor-4', is('CoreTemporarilyUnavailable')))
    }

    @WithMockUser(username = "deviceClient", authorities = ['SCOPE_device'])
    def "should post measurements without given timestamp"(){
        given:
            WireMock.stubFor(WireMock.post("/dwh")
                    .inScenario("DWH-x")
                    .whenScenarioStateIs(Scenario.STARTED)
                    .willReturn(WireMock.ok()))

            def createBean = { id ->
                [
                        sourceId: id,
                        values  : [
                                [value: 2.5],
                                [value: 1.5],
                                [value: 2.5],
                                [value: 3.5]
                        ],
                        tint: 5
                ]
            }

            def request = [
                    createBean('sensor-1')
            ]

        expect:
            mvc.perform(
                    post("/data")
                            .contentType(APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$.sensor-1', is('Ok')))

    }

    @WithMockUser(username = "deviceClient", authorities = ['SCOPE_device'])
    def "should reject not valid measurements"() {
        given:
            WireMock.stubFor(WireMock.post("/dwh")
                    .willReturn(WireMock.ok()))

            def request = [
                    [sourceId: 'sensor-1', values: values]
            ]

        expect:
            mvc.perform(
                    post("/data")
                            .contentType(APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$.sensor-1', is(result)))
        where:
            values                                             | result
            null                                               | 'ValidationError'
            []                                                 | 'ValidationError'
            [null, null]                                       | 'ValidationError'
            [[:]]                                              | 'ValidationError'
            [[ts: '2019-06-14T15:35:00Z']]                     | 'ValidationError'
            [[value: 1.2]]                                     | 'ValidationError'
            [[ts: '2019-06-14T15:35:00Z', value: 1.2]]         | 'Ok'
            [[ts: '2019-06-14T15:35:00Z', values: [1.2, 2.3]]] | 'Ok'
    }
}
