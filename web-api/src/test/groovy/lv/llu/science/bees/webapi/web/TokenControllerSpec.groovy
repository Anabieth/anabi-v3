package lv.llu.science.bees.webapi.web

import lv.llu.science.bees.webapi.domain.tokens.DeviceTokenService
import lv.llu.science.bees.webapi.domain.tokens.TokenRequestBean
import lv.llu.science.bees.webapi.domain.tokens.TokenResponseBean
import spock.lang.Specification

class TokenControllerSpec extends Specification {
    def service = Mock(DeviceTokenService)
    def controller = new TokenController(service)

    def "should get token"() {
        given:
            def req = new TokenRequestBean()
        when:
            def res = controller.getToken(req)
        then:
            1 * service.getToken(req) >> new TokenResponseBean(token: 'aaa-bbb')
            res.token == 'aaa-bbb'
    }
}
