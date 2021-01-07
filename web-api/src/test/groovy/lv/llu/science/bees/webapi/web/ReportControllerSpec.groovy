package lv.llu.science.bees.webapi.web

import lv.llu.science.bees.webapi.domain.reports.ReportService
import spock.lang.Specification

import java.time.ZonedDateTime

class ReportControllerSpec extends Specification {

    def service = Mock(ReportService)
    def controller = new ReportController(service)

    def "should get report list"() {
        when:
            controller.listReports()
        then:
            1 * service.getReportList()
    }

    def "should get report details"() {
        when:
            controller.getReportDetails('the-code')
        then:
            1 * service.getReportDetails('the-code')
    }

    def "should get report data"() {
        given:
            def from = ZonedDateTime.now().minusHours(1)
            def to = ZonedDateTime.now()
        when:
            controller.getReportData('the-code', 'node-id', from, to, 1234)
        then:
            1 * service.getReportData('the-code', 'node-id', from, to, 1234)
    }
}
