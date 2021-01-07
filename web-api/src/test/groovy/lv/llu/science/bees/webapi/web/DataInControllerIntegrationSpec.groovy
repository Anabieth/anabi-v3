package lv.llu.science.bees.webapi.web

import com.github.tomakehurst.wiremock.client.WireMock
import groovy.json.JsonOutput
import lv.llu.science.bees.webapi.domain.mapping.MappingRepository
import lv.llu.science.bees.webapi.domain.mapping.SourceMapping
import lv.llu.science.bees.webapi.domain.nodes.Node
import lv.llu.science.bees.webapi.web.helpers.RestDocsSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser

import java.time.ZoneId

import static java.time.ZonedDateTime.now
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import static org.springframework.restdocs.payload.PayloadDocumentation.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WithMockUser(username = 'demo-client', authorities = ['SCOPE_device'])
class DataInControllerIntegrationSpec extends RestDocsSpecification {

    @Autowired
    MappingRepository mappingRepository

    def setup() {
        setupDefaultWorkspace()
        setupDefaultNodes()

        nodeRepository.insert(
                new Node(id: 'dev1', type: 'DEVICE', clientId: 'demo-client', isActive: true, workspaceId: 'ws1')
        )

        mappingRepository.insert([
                new SourceMapping(sourceId: 'sensor-A', nodeId: 'hive1', valueKey: 'weight'),
                new SourceMapping(sourceId: 'sensor-B', nodeId: 'hive99', valueKey: 'audio'),
        ])

        WireMock.stubFor(WireMock.post("/dwh")
                .willReturn(WireMock.ok())
        )
    }

    def cleanup() {
        mappingRepository.deleteAll()
    }

    def "should add measurements"() {
        given:
            def now = now().withZoneSameInstant(ZoneId.of('Z')).withNano(0)
            def request = [
                    [sourceId: 'sensor-A', values: [
                            [ts: now.minusMinutes(10).format(ISO_DATE_TIME), value: 78.4],
                            [ts: now.minusMinutes(5).format(ISO_DATE_TIME), value: 76.9],
                            [ts: now.format(ISO_DATE_TIME), value: 76.4],
                    ]],
                    [sourceId: 'sensor-B', values: [
                            [ts: now.format(ISO_DATE_TIME), values: [120.5, 110.7, 167.5]]
                    ]],
                    [sourceId: 'sensor-C', tint: 2, values: [
                            [value: 23.1],
                            [value: 22.5],
                            [value: 21.9],
                    ]],
            ]
        expect:
            mvc.perform(
                    post('/data')
                            .contentType(APPLICATION_JSON)
                            .content(JsonOutput.toJson(request)))
                    .andExpect(status().isOk())
                    .andDo(document('data-in',
                            requestFields(
                                    fieldWithPath('[].sourceId').description('Unique ID of data source (from source mapping)'),
                                    fieldWithPath('[].values[]').description('List of measurements'),
                                    fieldWithPath('[].values[].ts').optional().description('Measurement timestamp'),
                                    fieldWithPath('[].values[].value').optional().description('Measured value (scalar)'),
                                    fieldWithPath('[].values[].values').optional().description('Measured values (array)'),
                                    fieldWithPath('[].tint').optional().description('Measurement interval (used for data-in packages without timestamps)'),
                            ),
                            responseFields(
                                    fieldWithPath('*').description('Status report for each source within data-in package'),
                            )
                    ))
    }

    def "should add log message"() {
        given:
            def now = now().withZoneSameInstant(ZoneId.of('Z')).withNano(0)
            def request = [ts: now.format(ISO_DATE_TIME), level: 'info', message: 'Demo message from the device']
        expect:
            mvc.perform(
                    post('/logs')
                            .contentType(APPLICATION_JSON)
                            .content(JsonOutput.toJson(request)))
                    .andExpect(status().is(204))
                    .andDo(document('logs'))
    }

}
