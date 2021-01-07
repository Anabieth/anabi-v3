package lv.llu.science.bees.webapi.domain.auth0

import feign.FeignException
import lv.llu.science.bees.webapi.domain.tokens.TokenCacheService
import lv.llu.science.bees.webapi.domain.tokens.TokenRequestBean
import lv.llu.science.bees.webapi.domain.tokens.TokenResponseBean
import lv.llu.science.bees.webapi.utils.TimeMachine
import lv.llu.science.bees.webapi.utils.TooManyRequestsException
import org.springframework.security.authentication.BadCredentialsException
import spock.lang.Specification

import static java.time.ZonedDateTime.parse

class TokenCacheServiceSpec extends Specification {

    def tm = Mock(TimeMachine)
    def auth0 = Mock(Auth0Client)
    def service = new TokenCacheService(tm, auth0)

    def "should get new token when not found in cache"() {
        given:
            def req = new TokenRequestBean(clientId: 'test', clientSecret: 'password');
            def now = parse('2020-02-13T10:00:00Z')
            tm.zonedNow() >> now
        when:
            def res = service.getToken(req)
        then:
            1 * auth0.getToken(req) >> new TokenResponseBean(token: 'aaa-bbb', expiresIn: 3600)
            res.token == 'aaa-bbb'
            res.expiresIn == 3600
            res.expiresAt == now.plusSeconds(3600)
            res.lastRequest == now
    }

    def "should throw if request rate exceeds limit"() {
        given:
            def req = new TokenRequestBean(clientId: 'test', clientSecret: 'password');
            def now = parse('2020-02-13T10:00:00Z')
            tm.zonedNow() >> now
        when: 'first request'
            def res = service.getToken(req)
        then:
            1 * auth0.getToken(req) >> new TokenResponseBean(token: 'aaa-bbb', expiresIn: 3600)
            res.token == 'aaa-bbb'

        when: 'next frequent request'
            tm.zonedNow() >> now.plusSeconds(29)
            service.getToken(req)
        then:
            thrown(TooManyRequestsException)

        when: 'next request after time limit'
            tm.zonedNow() >> now.plusSeconds(31)
            res = service.getToken(req)
        then:
            res.token == 'aaa-bbb'
            0 * auth0.getToken(_)
    }

    def "should get new token after timeout if previous attempt failed"() {
        given:
            def req = new TokenRequestBean(clientId: 'test', clientSecret: 'password');
            def now = parse('2020-02-13T10:00:00Z')
            tm.zonedNow() >> now
        when: 'failed request'
            service.getToken(req)
        then:
            1 * auth0.getToken(req) >> {
                throw new FeignException(401, 'wrong credentials');
            }
            thrown(BadCredentialsException)

        when: 'next frequent request'
            service.getToken(req)
        then:
            thrown(TooManyRequestsException)

        when: 'next request after time limit'
            tm.zonedNow() >> now.plusSeconds(31)
            def res = service.getToken(req)
        then:
            1 * auth0.getToken(req) >> new TokenResponseBean(token: 'aaa-bbb', expiresIn: 3600)
            res.token == 'aaa-bbb'
    }

    def "should use cached token is not expired"() {
        given:
            def req = new TokenRequestBean(clientId: 'test', clientSecret: 'password');
            def now = parse('2020-02-13T10:00:00Z')
            tm.zonedNow() >> now
        when: 'first request'
            service.getToken(req)
        then:
            1 * auth0.getToken(req) >> new TokenResponseBean(token: 'aaa-bbb', expiresIn: 120)

        when: 'next request '
            tm.zonedNow() >> now.plusSeconds(119)
            def res = service.getToken(req)
        then:
            0 * auth0.getToken(_)
            res.token == 'aaa-bbb'
    }

    def "should get new token if expired"() {
        given:
            def req = new TokenRequestBean(clientId: 'test', clientSecret: 'password');
            def now = parse('2020-02-13T10:00:00Z')
            tm.zonedNow() >> now
        when: 'first request'
            service.getToken(req)
        then:
            1 * auth0.getToken(req) >> new TokenResponseBean(token: 'aaa-bbb', expiresIn: 120)

        when: 'next request after expiration'
            tm.zonedNow() >> now.plusSeconds(121)
            def res = service.getToken(req)
        then:
            1 * auth0.getToken(_) >> new TokenResponseBean(token: 'xxx-yyy', expiresIn: 120)
            res.token == 'xxx-yyy'
    }
}
