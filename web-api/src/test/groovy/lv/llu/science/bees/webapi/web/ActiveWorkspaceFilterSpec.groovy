package lv.llu.science.bees.webapi.web

import lv.llu.science.bees.webapi.domain.workspaces.ActiveWorkspace
import spock.lang.Specification

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest

class ActiveWorkspaceFilterSpec extends Specification {
    def active = Mock(ActiveWorkspace)
    def filter = new ActiveWorkspaceFilter(active)

    def "should do filter"() {
        given:
            def req = Stub(HttpServletRequest) {
                getHeader('ws') >> '123'
            }

            def chain = Mock(FilterChain)
        when:
            filter.doFilter(req, null, chain)
        then:
            1 * chain.doFilter(req, null)
            1 * active.setId('123')
    }
}
