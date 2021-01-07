package lv.llu.science.bees.webapi.domain.tokens

import lv.llu.science.bees.webapi.security.SecretProvider
import lv.llu.science.bees.webapi.utils.TimeMachine
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification
import spock.lang.Unroll

import java.time.ZonedDateTime

class DeviceTokenServiceSpec extends Specification {
    def secretProvider = Mock(SecretProvider)
    def repository = Mock(DeviceClientRepository)
    def timeMachine = Mock(TimeMachine) {
        zonedNow() >> ZonedDateTime.parse("2020-08-28T10:20:30Z")
    }
    def service = new DeviceTokenService(secretProvider, repository, timeMachine)

    def setup() {
        service.audience = "test-audience"
        service.expireIn = 86400
    }

    def "should get token"() {
        given:
            def req = new TokenRequestBean(
                    clientId: 'test-client',
                    clientSecret: 'test-secret',
                    audience: 'test-audience',
                    grantType: 'client_credentials')

        when:
            def res = service.getToken(req)
        then:
            1 * secretProvider.getSecretBytes() >> ('abcd1234' * 8).getBytes()
            1 * repository.findByIdAndSecret('test-client', 'test-secret') >>
                    Optional.of(new DeviceClient(id: 'test-client'))
            res.scope == 'device'
            res.tokenType == 'Bearer'
            res.token == 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0LWNsaWVudEBjbGllbnRzIiwiYXVkIjoidGVzdC1hdWRp' +
                    'ZW5jZSIsInNjb3BlIjoiZGV2aWNlIiwiZXhwIjoxNTk4Njk2NDMwLCJpYXQiOjE1OTg2MTAwMzB9' +
                    '.MpAfPD9uTSLa8jGWchIyWe8-p9LOFxxOrKXu0UfOinC1iPC23feq-SKhHVw_YK2dnOhWze-5DLSO1aB1S8fLug'
    }

    @Unroll
    def "should reject wrong #label"() {
        given:
            def req = new TokenRequestBean(audience: audience, grantType: grant)
        when:
            service.getToken(req)
        then:
            thrown(ResponseStatusException)
        where:
            label        | audience          | grant
            'audience'   | 'random-audience' | 'client_credentials'
            'grant type' | 'test-audience'   | 'random_grant'
    }

    def "should reject wrong credentials"() {
        given:
            def req = new TokenRequestBean(
                    audience: 'test-audience',
                    grantType: 'client_credentials',
                    clientId: 'wrong-client',
                    clientSecret: 'wrong-secret',)
        when:
            service.getToken(req)
        then:
            1 * repository.findByIdAndSecret(_, _) >> Optional.empty()
            thrown(BadCredentialsException)
    }
}
