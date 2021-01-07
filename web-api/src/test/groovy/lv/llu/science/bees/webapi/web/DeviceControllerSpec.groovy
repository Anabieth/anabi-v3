package lv.llu.science.bees.webapi.web

import lv.llu.science.bees.webapi.domain.nodes.NodeService
import spock.lang.Specification

class DeviceControllerSpec extends Specification {

    def service = Mock(NodeService)
    def controller = new DeviceController(service)

    def "should get device list"() {
        when:
            controller.listDevices()
        then:
            1 * service.getDevices()
    }

    def "should get device events"() {
        when:
            controller.getDeviceEvents('dev-123')
        then:
            1 * service.getDeviceEvents('dev-123')
    }

    def "should toggle device activity"() {
        when:
            controller.toggleActiveDevice('dev-123')
        then:
            1 * service.toggleActive('dev-123')
    }
}
