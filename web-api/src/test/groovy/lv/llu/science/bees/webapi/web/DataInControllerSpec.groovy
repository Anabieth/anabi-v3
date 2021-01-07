package lv.llu.science.bees.webapi.web

import lv.llu.science.bees.webapi.domain.datain.DataInBean
import lv.llu.science.bees.webapi.domain.datain.DataInService
import lv.llu.science.bees.webapi.dwh.DwhValueBean
import org.springframework.web.context.request.WebRequest
import spock.lang.Specification

import static java.time.ZonedDateTime.now

class DataInControllerSpec extends Specification {
    def service = Mock(DataInService)
    def controller = new DataInController(service)

    def "should add measurements"() {
        given:
            def data = [new DataInBean(sourceId: "temp-id-000", values: [
                    new DwhValueBean(ts: now(), value: 33.2),
                    new DwhValueBean(ts: now(), value: 33.5)])]
        when:
            controller.addMeasurements(data)
        then:
            1 * service.addMeasurements(data) >> ["temp-id-000": "Ok"]
    }

    def "should handle validation exception"() {
        given:
            def request = Mock(WebRequest)
        when:
            def result = controller.handleException(null, request)
        then:
            result.status == 400
            result.error != ''
    }
}
