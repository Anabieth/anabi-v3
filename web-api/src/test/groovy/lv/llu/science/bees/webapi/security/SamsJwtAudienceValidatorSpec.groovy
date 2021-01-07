package lv.llu.science.bees.webapi.security


import org.springframework.security.oauth2.jwt.Jwt
import spock.lang.Specification

class SamsJwtAudienceValidatorSpec extends Specification {

    def validator = new SamsJwtAudienceValidator('test-audience')

    def "should pass valid token"() {
        given:
            def token = Mock(Jwt) { getAudience() >> ['test-audience'] }
        expect:
            !validator.validate(token).hasErrors()
    }

    def "should reject wrong token"() {
        given:
            def token = Mock(Jwt) { getAudience() >> ['wrong-audience'] }
        expect:
            validator.validate(token).hasErrors()
    }
}
