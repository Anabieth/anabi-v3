package lv.llu.science.bees.webapi.domain.nodes.latestValues

import lv.llu.science.bees.webapi.domain.nodes.Node
import lv.llu.science.bees.webapi.dwh.DwhClient
import lv.llu.science.bees.webapi.dwh.DwhValueBean
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import spock.lang.Specification

class NodeLatestValueServiceSpec extends Specification {

    def operations = Mock(MongoOperations)
    def dwhClient = Mock(DwhClient)
    def service = new NodeLatestValueService(operations, dwhClient)

    def "should push last values"() {
        when:
            service.pushLatestValues('123', 'temperature', [new DwhValueBean()] * 3)
        then:
            1 * operations.updateFirst(_ as Query, _ as Update, Node.class) >> { query, update, cls ->
                def str = update.toString()
                assert str =~ '\\$push.+lastValues.+\\$sort.+ts.+\\$slice.+\\$each'
            }
    }

    def "should ignore audio in last values"() {
        when:
            service.pushLatestValues('123', 'audio', [new DwhValueBean()] * 3)
        then:
            0 * operations.updateFirst(*_)
    }

    def "should get latest values"() {
        given:
            def values = [
                    a: [new DwhValueBean(value: 1), new DwhValueBean(value: 2)],
                    b: [new DwhValueBean(value: 9), new DwhValueBean(value: 8)]
            ]
        when:
            def result = service.getLatestValues('node123')
        then:
            1 * operations.findOne(_, Node.class) >> new Node(lastValues: values)
            1 * dwhClient.getModelLatestValues('node123') >> [new LatestModelValueBean(modelCode: 'test')]
            result.latestMeasurements[0].type == 'a'
            result.latestMeasurements[0].value == 1.0
            result.latestMeasurements[1].type == 'b'
            result.latestMeasurements[1].value == 9.0
            result.latestModelValues[0].modelCode == 'test'

    }

    def "should get latest measurements"() {
        given:
            def values = [a: [], b: []]
        when:
            def result = service.getLatestMeasurements('node123')
        then:
            1 * operations.findOne(_, Node.class) >> new Node(lastValues: values)
            result == values
    }
}
