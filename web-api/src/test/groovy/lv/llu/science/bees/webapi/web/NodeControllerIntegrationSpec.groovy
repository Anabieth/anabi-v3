package lv.llu.science.bees.webapi.web

import com.github.tomakehurst.wiremock.client.WireMock
import groovy.json.JsonOutput
import lv.llu.science.bees.webapi.domain.mapping.MappingRepository
import lv.llu.science.bees.webapi.domain.mapping.SourceMapping
import lv.llu.science.bees.webapi.domain.mapping.SourceMappingBean
import lv.llu.science.bees.webapi.domain.nodes.Node
import lv.llu.science.bees.webapi.domain.nodes.NodeRepository
import lv.llu.science.bees.webapi.dwh.DwhValueBean
import lv.llu.science.bees.webapi.web.helpers.RestDocsSpecification
import org.springframework.beans.factory.annotation.Autowired

import java.time.ZonedDateTime

import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*
import static org.springframework.restdocs.payload.PayloadDocumentation.*
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class NodeControllerIntegrationSpec extends RestDocsSpecification {

    @Autowired
    NodeRepository repository

    @Autowired
    MappingRepository mappingRepository

    def setup() {
        setupDefaultWorkspace()

        repository.insert(new Node(id: 'apiary1', name: 'Demo apiary', type: 'APIARY', workspaceId: 'ws1',
                location: 'Wonderland'))
        repository.insert(new Node(id: 'hive1', name: 'Hive 1', type: 'HIVE', parentId: 'apiary1', workspaceId: 'ws1',
                ancestors: ['apiary1'],
                lastValues: [
                        temperature: [
                                new DwhValueBean(ts: ZonedDateTime.now().minusMinutes(2), value: 32.5),
                                new DwhValueBean(ts: ZonedDateTime.now().minusMinutes(4), value: 31.7),
                                new DwhValueBean(ts: ZonedDateTime.now().minusMinutes(6), value: 30.3)
                        ]
                ]))
        repository.insert(new Node(id: 'element1', name: 'Bottom frame', type: 'HIVE_ELEMENT', parentId: 'hive1', workspaceId: 'ws1'))
        repository.insert(new Node(id: 'hive2', name: 'Hive 2', type: 'HIVE', parentId: 'apiary1', workspaceId: 'ws1',
                location: 'Next to big tree'
        ))
        repository.insert(new Node(id: 'dev1', name: 'ESP8266', type: 'DEVICE', parentId: 'apiary1', workspaceId: 'ws1',
                clientId: 'demo-device-client', isActive: true, hwConfigId: 'hw-123'
        ))

        mappingRepository.insert([
                new SourceMapping(nodeId: 'hive1', sourceId: 'dht22-temp-Fjx', valueKey: 'temperature'),
                new SourceMapping(nodeId: 'hive1', sourceId: 'dht22-hum-iSk', valueKey: 'humidity'),
                new SourceMapping(nodeId: 'hive1', sourceId: 'scale-LKo', valueKey: 'weight')
        ])
    }

    def cleanup() {
        repository.deleteAll()
        mappingRepository.deleteAll()
    }

    def "should list nodes"() {
        expect:
            mvc.perform(
                    get('/nodes'))
                    .andExpect(status().isOk())
                    .andDo(document('node-list',
                            responseFields(
                                    fieldWithPath('[].id').description('Node id'),
                                    fieldWithPath('[].name').description('Node name'),
                                    fieldWithPath('[].type').description('Node type'),
                                    fieldWithPath('[].location').optional().description('Location of the node'),
                                    fieldWithPath('[].parentId').optional().description('Parent node id'),
                                    fieldWithPath('[].clientId').optional().description('(Devices) Client ID used for authentication'),
                                    fieldWithPath('[].isActive').optional().description('(Devices) Enabled / Disabled status of device'),
                                    fieldWithPath('[].hwConfigId').optional().description('(Devices, HW) ID of hardware configuration'),
                            )
                    ))
    }

    def "should get node details"() {
        expect:
            mvc.perform(
                    get('/nodes/{id}', 'hive1'))
                    .andExpect(status().isOk())
                    .andDo(document('node-details',
                            pathParameters(
                                    parameterWithName('id').description('Node id'),
                            ),
                            relaxedResponseFields(
                                    fieldWithPath('*').description('Node basic fields'),
                                    fieldWithPath('ancestors').description('Full list of the node ancestors'),
                                    fieldWithPath('children').description('List of direct children of the node')
                            )
                    ))
    }

    def "should add node"() {
        given:
            def request = [name: 'New demo hive', type: 'HIVE', parentId: 'apiary1']
        expect:
            mvc.perform(
                    post('/nodes')
                            .contentType(APPLICATION_JSON)
                            .content(JsonOutput.toJson(request)))
                    .andExpect(status().isOk())
                    .andDo(document('node-add'))
    }

    def "should edit node"() {
        given:
            def request = [name: 'New apiary name', type: 'GROUP']
        expect:
            mvc.perform(
                    put('/nodes/{id}', 'apiary1')
                            .contentType(APPLICATION_JSON)
                            .content(JsonOutput.toJson(request)))
                    .andExpect(status().isOk())
                    .andDo(document('node-edit'))
    }

    def "should delete node"() {
        expect:
            mvc.perform(
                    delete('/nodes/{id}', 'hive2'))
                    .andExpect(status().is(204))
                    .andDo(document('node-delete'))
    }

    def "should get latest values"() {
        given:
            def dwhResponse = [
                    [modelCode: 'demo', label: 'ok', description: 'Status is OK',
                     timestamp: '2020-11-12T10:20:30Z', rawValue: [alpha: 12.3, gamma: 2.34]]
            ]
            WireMock.stubFor(WireMock.get("/dwh/models/latest/hive1")
                    .willReturn(WireMock.ok()
                            .withBody(JsonOutput.toJson(dwhResponse))
                            .withHeader('Content-Type', 'application/json')
                    )
            )
        expect:
            mvc.perform(
                    get('/nodes/{id}/latestValues', 'hive1'))
                    .andExpect(status().isOk())
                    .andDo(document('node-latest-values',
                            pathParameters(
                                    parameterWithName('id').description('Node id'),
                            ),
                            responseFields(
                                    fieldWithPath('id').description('Node id'),
                                    fieldWithPath('latestModelValues[]').description('List of latest modelling results'),
                                    fieldWithPath('latestModelValues[].modelCode').description('Model code'),
                                    fieldWithPath('latestModelValues[].timestamp').description('Timestamp of modelling'),
                                    fieldWithPath('latestModelValues[].label').description('Modelling result'),
                                    fieldWithPath('latestModelValues[].description').description('Human readable description of modelling results'),
                                    fieldWithPath('latestModelValues[].rawValue.*').description('Raw modelling results'),
                                    fieldWithPath('latestMeasurements[]').description('List of latest incoming measurements'),
                                    fieldWithPath('latestMeasurements[].type').description('Measurement type'),
                                    fieldWithPath('latestMeasurements[].timestamp').description('Measurement timestamp'),
                                    fieldWithPath('latestMeasurements[].value').description('Measured value'),
                            )
                    ))
    }

    def "should get latest measurements"() {
        expect:
            mvc.perform(
                    get('/nodes/{id}/latestMeasurements', 'hive1'))
                    .andExpect(status().isOk())
                    .andDo(document('node-latest-measurements',
                            pathParameters(
                                    parameterWithName('id').description('Node id'),
                            ),
                            responseFields(
                                    fieldWithPath('*[]').description('List of latest measurements for given type'),
                                    fieldWithPath('*[].ts').description('Measurement timestamp'),
                                    fieldWithPath('*[].value').description('Measured value'),
                            )
                    ))
    }

    def "should supported value keys"() {
        given:
            def dwhResponse = ['temperature', 'humidity', 'weight']
            WireMock.stubFor(WireMock.get("/dwh/topics")
                    .willReturn(WireMock.ok()
                            .withBody(JsonOutput.toJson(dwhResponse))
                            .withHeader('Content-Type', 'application/json')
                    )
            )
        expect:
            mvc.perform(
                    get('/nodes/{id}/mapping/valueKeys', 'hive1'))
                    .andExpect(status().isOk())
                    .andDo(document('node-supported-value-keys',
                            pathParameters(
                                    parameterWithName('id').description('Node id'),
                            ),
                            responseFields(
                                    fieldWithPath('[]').description('List of supported value keys'),
                            )
                    ))
    }

    def "should get node mappings"() {
        expect:
            mvc.perform(
                    get('/nodes/{id}/mapping', 'hive1'))
                    .andExpect(status().isOk())
                    .andDo(document('node-mapping',
                            pathParameters(
                                    parameterWithName('id').description('Node id'),
                            ),
                            responseFields(
                                    fieldWithPath('[]').description('List of defined source mappings'),
                                    fieldWithPath('[].sourceId').description('Unique ID of data source sent by measurement device (usually corresponds to sensor)'),
                                    fieldWithPath('[].valueKey').description('Type of values received from given source'),
                            )
                    ))
    }

    def "should update node mappings"() {
        given:
            def request = [
                    new SourceMappingBean(sourceId: 'demo-sensor-123', valueKey: 'temperature'),
                    new SourceMappingBean(sourceId: 'demo-sensor-987', valueKey: 'voltage'),
            ]
        expect:
            mvc.perform(
                    put('/nodes/{id}/mapping', 'hive2')
                            .contentType(APPLICATION_JSON)
                            .content(JsonOutput.toJson(request)))
                    .andExpect(status().isOk())
                    .andDo(document('node-update-mapping',
                            pathParameters(
                                    parameterWithName('id').description('Node id'),
                            )
                    ))
    }

    def "should check is source id used"() {
        expect:
            mvc.perform(
                    post('/nodes/{id}/mapping/isUsed', 'hive2')
                            .contentType(APPLICATION_JSON)
                            .content('dht22-temp-Fjx'))
                    .andExpect(status().isOk())
                    .andDo(document('node-check-mapping',
                            pathParameters(
                                    parameterWithName('id').description('Node id'),
                            ),
                            responseFields(
                                    fieldWithPath('isUsed').description('Indicates is provided source ID used in any other node'),
                                    fieldWithPath('nodeId').optional().description('Node ID where source ID used (shown only if the node is from the same workspace)'),
                                    fieldWithPath('nodeName').optional().description('Node name where source ID used (shown only if the node is from the same workspace)'),
                            )
                    ))
    }
}
