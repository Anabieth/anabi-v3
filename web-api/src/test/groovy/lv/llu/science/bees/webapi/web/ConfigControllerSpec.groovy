package lv.llu.science.bees.webapi.web

import lv.llu.science.bees.webapi.domain.configs.ConfigBean
import lv.llu.science.bees.webapi.domain.configs.ConfigService
import spock.lang.Specification

import javax.servlet.http.HttpServletResponse

class ConfigControllerSpec extends Specification {

    def service = Mock(ConfigService)
    def controller = new ConfigController(service)

    def "should get config list"() {
        when:
            controller.listConfigs()
        then:
            1 * service.getAllConfigs()
    }

    def "should get config details"() {
        when:
            controller.getConfigDetails('123')
        then:
            1 * service.getConfigDetails('123')
    }

    def "should add new config"() {
        when:
            controller.addConfig(new ConfigBean())
        then:
            1 * service.createConfig(_)
    }

    def "should save existing config"() {
        when:
            controller.editConfig('123', new ConfigBean())
        then:
            1 * service.editConfig('123', _)
    }

    def "should delete config"() {
        given:
            def response = Mock(HttpServletResponse)
        when:
            controller.deleteConfig('123', response)
        then:
            1 * service.deleteConfig('123')
            1 * response.setStatus(204)
    }

    def "should set default config"() {
        when:
            controller.setDefault('123')
        then:
            1 * service.setDefault('123')
    }

    def "should get device config"() {
        when:
            controller.getDeviceConfig()
        then:
            1 * service.getDeviceConfig()
    }
}
