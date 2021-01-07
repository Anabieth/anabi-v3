package lv.llu.science.bees.webapi.web

import groovy.json.JsonOutput
import lv.llu.science.bees.webapi.domain.tokens.DeviceClient
import lv.llu.science.bees.webapi.domain.tokens.DeviceClientRepository
import lv.llu.science.bees.webapi.web.helpers.RestDocsSpecification
import org.springframework.beans.factory.annotation.Autowired

import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import static org.springframework.restdocs.payload.PayloadDocumentation.*
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class TokenControllerIntegrationSpec extends RestDocsSpecification {

    @Autowired
    DeviceClientRepository repository

    def setup() {
        repository.insert(new DeviceClient(id: 'demo-device', secret: 'device-secret'));
    }

    def "should get token"() {
        given:
            def request = [
                    client_id    : 'demo-device',
                    client_secret: 'device-secret',
                    audience     : 'sams-dwh-web-api',
                    grant_type   : 'client_credentials'
            ]
        expect:
            mvc.perform(
                    post("/token")
                            .contentType(APPLICATION_JSON)
                            .content(JsonOutput.toJson(request)))
                    .andExpect(status().isOk())
                    .andDo(document('token',
                            requestFields(
                                    fieldWithPath('client_id').description('Client ID of the device'),
                                    fieldWithPath('client_secret').description('Client secret of the device'),
                                    fieldWithPath('audience').description('Should be set to "sams-dwh-web-api"'),
                                    fieldWithPath('grant_type').description('Should be set to "client_credentials"')
                            ),
                            responseFields(
                                    fieldWithPath('access_token').description('Access token to be used by device for data-in requests'),
                                    fieldWithPath('token_type').description('Type of the access token'),
                                    fieldWithPath('scope').description('Scopes assigned to the device'),
                                    fieldWithPath('expires_in').description('Token lifetime in seconds')
                            )
                    ))
    }
}
