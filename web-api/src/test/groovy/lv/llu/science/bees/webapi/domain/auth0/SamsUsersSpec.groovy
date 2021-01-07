package lv.llu.science.bees.webapi.domain.auth0

import lv.llu.science.bees.webapi.domain.tokens.TokenResponseBean
import spock.lang.Specification
import spock.lang.Unroll

import static java.time.ZonedDateTime.now

class SamsUsersSpec extends Specification {

    def auth0 = Mock(Auth0Client)
    def users = new SamsUsers(auth0)

    @Unroll
    def "should handle #desc token"() {
        given:
            users.token = token
        when:
            def map = users.getUserMap()
        then:
            calls * auth0.getToken(_) >> new TokenResponseBean(tokenType: 'Bearer', token: 'abc-xyz', expiresIn: 1234)
            1 * auth0.getUsers('Bearer abc-xyz') >> [
                    [user_id: 'john', name: 'John The Tester']
            ]

            map == ['john': 'John The Tester']
        where:
            desc      | token                                                                                        | calls
            'empty'   | null                                                                                         | 1
            'expired' | new TokenResponseBean(tokenType: 'Bearer', token: 'qwe-rty', expiresAt: now().minusHours(1)) | 1
            'valid'   | new TokenResponseBean(tokenType: 'Bearer', token: 'abc-xyz', expiresAt: now().plusHours(1))  | 0
    }
}
