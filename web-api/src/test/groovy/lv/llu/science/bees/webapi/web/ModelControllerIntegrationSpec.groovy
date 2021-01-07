package lv.llu.science.bees.webapi.web

import com.github.tomakehurst.wiremock.client.WireMock
import groovy.json.JsonOutput
import lv.llu.science.bees.webapi.domain.models.ModelDefinition
import lv.llu.science.bees.webapi.domain.models.ModelRepository
import lv.llu.science.bees.webapi.domain.models.ModelTemplate
import lv.llu.science.bees.webapi.domain.models.ModelTemplateParam
import lv.llu.science.bees.webapi.web.helpers.RestDocsSpecification
import org.springframework.beans.factory.annotation.Autowired

import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ModelControllerIntegrationSpec extends RestDocsSpecification {

    @Autowired
    ModelRepository modelRepository

    def setup() {
        setupDefaultWorkspace()
        setupDefaultNodes()

        def modelTemplates = [
                new ModelTemplate(code: 'demo-model-A', name: 'First demo model',
                        description: 'This is the first model for demonstration purposes',
                        params: [
                                new ModelTemplateParam(code: 'paramA', name: 'Parameter A', type: 'nodeId',
                                        description: 'Parameter providing node reference for the model',
                                        master: true),
                                new ModelTemplateParam(code: 'paramB', name: 'Parameter B', type: 'number',
                                        description: 'Parameter providing numeric value for the model')
                        ]
                ),
                new ModelTemplate(code: 'demo-model-B', name: 'Second demo model',
                        description: 'This is the second model with single parameter',
                        params: [new ModelTemplateParam(code: 'hive', name: 'Main hive', type: 'nodeId', master: true),]
                )
        ]

        WireMock.stubFor(WireMock.get("/dwh/models")
                .willReturn(WireMock.ok()
                        .withBody(JsonOutput.toJson(modelTemplates))
                        .withHeader('Content-Type', 'application/json')
                )
        )


        modelRepository.insert([
                new ModelDefinition(id: 'model-123', modelCode: 'demo-model-A', workspaceId: 'ws1',
                        params: [paramA: 'hive1', paramB: 20.5f]),
                new ModelDefinition(id: 'model-987', modelCode: 'demo-model-A', workspaceId: 'ws1',
                        params: [paramA: 'hive2', paramB: 17.4f]),
                new ModelDefinition(id: 'model-582', modelCode: 'demo-model-B', workspaceId: 'ws1',
                        params: [hive: 'hive2']),
        ])


    }

    def cleanup() {
        modelRepository.deleteAll()
    }

    def "should list model templates"() {
        expect:
            mvc.perform(
                    get('/models/templates'))
                    .andExpect(status().isOk())
                    .andDo(document('model-templates',
                            responseFields(
                                    fieldWithPath('[].code').description('Model code'),
                                    fieldWithPath('[].name').description('Model name'),
                                    fieldWithPath('[].description').optional().description('Model description'),
                                    fieldWithPath('[].params[]').description('List of model parameters'),
                                    fieldWithPath('[].params[].code').description('Parameter code'),
                                    fieldWithPath('[].params[].name').description('Parameter name'),
                                    fieldWithPath('[].params[].description').optional().description('Parameter description'),
                                    fieldWithPath('[].params[].type').description('Type of parameter: ' +
                                            '`nodeId` for node references, `number` for fixed numeric values.'),
                                    fieldWithPath('[].params[].master').optional().description('Indicates that modeling results are stored for the provided node'),
                            )
                    ))
    }

    def "should list model definitions"() {
        expect:
            mvc.perform(
                    get('/models'))
                    .andExpect(status().isOk())
                    .andDo(document('model-list',
                            responseFields(
                                    fieldWithPath('[].id').description('Model definition id'),
                                    fieldWithPath('[].modelCode').description('Model code'),
                                    fieldWithPath('[].params.*').description('Actual values of the model parameters')
                            )
                    ))
    }

    def "should get model definition"() {
        expect:
            mvc.perform(get('/models/model-123'))
                    .andExpect(status().isOk())
                    .andDo(document('model-details'))
    }

    def "should create model definition"() {
        given:
            WireMock.stubFor(WireMock.post("/dwh/models/demo-model-A")
                    .willReturn(WireMock.ok())
            )
            def request = [modelCode: 'demo-model-A', params: [paramA: 'hive3', paramB: 5.6]]
        expect:
            mvc.perform(
                    post('/models').contentType(APPLICATION_JSON)
                            .content(JsonOutput.toJson(request)))
                    .andExpect(status().isOk())
                    .andDo(document('model-create'))
    }

    def "should edit model definition"() {
        given:
            WireMock.stubFor(WireMock.post("/dwh/models/demo-model-A")
                    .willReturn(WireMock.ok())
            )
            def request = [modelCode: 'demo-model-A', params: [paramA: 'hive3', paramB: 16.6]]
        expect:
            mvc.perform(
                    put('/models/model-123').contentType(APPLICATION_JSON)
                            .content(JsonOutput.toJson(request)))
                    .andExpect(status().isOk())
                    .andDo(document('model-edit'))
    }

    def "should delete model definition"() {
        given:
            WireMock.stubFor(WireMock.delete("/dwh/models/demo-model-A/model-987")
                    .willReturn(WireMock.ok())
            )
        expect:
            mvc.perform(delete('/models/model-987'))
                    .andExpect(status().is(204))
                    .andDo(document('model-delete'))
    }
}
