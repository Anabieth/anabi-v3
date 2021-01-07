package lv.llu.science.bees.webapi.domain.reports

import lv.llu.science.bees.webapi.dwh.DwhClient
import spock.lang.Specification

import java.time.ZonedDateTime

class ReportServiceSpec extends Specification {
    def dwhClient = Mock(DwhClient)
    def service = new ReportService(dwhClient)

    def "should get report list"() {
        when:
            service.getReportList()
        then:
            1 * dwhClient.getReportList()
    }

    def "should get report details"() {
        when:
            service.getReportDetails('the-code')
        then:
            1 * dwhClient.getReportDetails('the-code')
    }

    def "should get report data"() {
        given:
            def from = ZonedDateTime.now().minusHours(1)
            def to = ZonedDateTime.now()
        when:
            service.getReportData('the-code', 'node-id', from, to, 1234)
        then:
            1 * dwhClient.getReportData('the-code', 'node-id', from, to, 1234)
    }
}
