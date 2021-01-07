package lv.llu.science.bees.webapi.security

import org.springframework.security.oauth2.jwt.Jwt
import spock.lang.Specification

class SamsJwtIssuerValidatorSpec extends Specification {

    def validator = new SamsJwtIssuerValidator('http://remote.issuer/', 'http://local.issuer/')

    def "should pass valid remote token"() {
        given:
            def token = Mock(Jwt) {
                getClaimAsString('iss') >> 'http://remote.issuer/'
            }
        expect:
            !validator.validate(token).hasErrors()
    }

    def "should pass valid local token"() {
        given:
            def token = Mock(Jwt) {
                getClaimAsString('iss') >> 'http://local.issuer/'
            }
        expect:
            !validator.validate(token).hasErrors()
    }

    def "should reject other token"() {
        given:
            def token = Mock(Jwt) {
                getClaimAsString('iss') >> 'http://random.issuer/'
            }
        expect:
            validator.validate(token).hasErrors()
    }
}
