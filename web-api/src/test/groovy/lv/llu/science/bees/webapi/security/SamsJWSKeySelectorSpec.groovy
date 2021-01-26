package lv.llu.science.bees.webapi.security

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.KeySourceException
import com.nimbusds.jwt.JWTClaimsSet
import spock.lang.Specification

import java.security.Key

class SamsJWSKeySelectorSpec extends Specification {
    def remoteUri = 'https://dev-42331gx2.us.auth0.com/'
    def localUri = 'http://local.issuer/'
    def selector = new SamsJWSKeySelector(remoteUri, localUri, Stub(Key))

    def "should select remote key"() {
        given:

        when:
            def res = selector.selectKeys(new JWSHeader(JWSAlgorithm.RS256),
                    new JWTClaimsSet.Builder().issuer(remoteUri).build(),
                    null)
        then:
            res.size() > 0
    }

    def "should select local key"() {
        when:
            def res = selector.selectKeys(new JWSHeader(JWSAlgorithm.HS512),
                    new JWTClaimsSet.Builder().issuer(localUri).build(),
                    null)
        then:
            res.size() > 0
    }

    def "should reject unknown key"() {
        when:
            def res = selector.selectKeys(new JWSHeader(JWSAlgorithm.RS256),
                    new JWTClaimsSet.Builder().issuer("random.issuer").build(),
                    null)
        then:
            thrown(KeySourceException)
    }
}
