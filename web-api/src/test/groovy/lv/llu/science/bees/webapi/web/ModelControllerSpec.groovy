package lv.llu.science.bees.webapi.web

import lv.llu.science.bees.webapi.domain.models.ModelDefinitionBean
import lv.llu.science.bees.webapi.domain.models.ModelService
import spock.lang.Specification

import javax.servlet.http.HttpServletResponse

class ModelControllerSpec extends Specification {

    def service = Mock(ModelService)
    def controller = new ModelController(service)

    def "should list model templates"() {
        when:
            controller.listModelTemplates()
        then:
            1 * service.listModelTemplates()
    }

    def "should create model definition"() {
        given:
            def bean = new ModelDefinitionBean()
        when:
            controller.createModelDefinition(bean)
        then:
            1 * service.createModelDefinition(bean)
    }

    def "should list model definitions"() {
        when:
            controller.listModelDefinitions()
        then:
            1 * service.listModelDefinitions()
    }

    def "should get model definition"() {
        when:
            controller.getModelDefinition('123')
        then:
            1 * service.getModelDefinition('123')
    }

    def "should edit model definition"() {
        given:
            def bean = new ModelDefinitionBean()
        when:
            controller.editModelDefinition('123', bean)
        then:
            1 * service.editModelDefinition('123', bean)
    }

    def "should delete model definition"() {
        given:
            def response = Mock(HttpServletResponse)
        when:
            controller.deleteModelDefinition('123', response)
        then:
            1 * service.deleteModelDefinition('123')
            1 * response.setStatus(204)
    }
}
