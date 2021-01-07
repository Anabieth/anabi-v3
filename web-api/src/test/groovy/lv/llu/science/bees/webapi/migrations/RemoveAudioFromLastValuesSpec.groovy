package lv.llu.science.bees.webapi.migrations


import lv.llu.science.bees.webapi.domain.nodes.Node
import lv.llu.science.bees.webapi.dwh.DwhValueBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@SpringBootTest
@ActiveProfiles("embedded")
class RemoveAudioFromLastValuesSpec extends Specification {

    @Autowired
    MongoOperations operations

    def cleanup() {
        operations.remove(new Query(), Node.class)
    }

    def "should remove audio from last values"() {
        given:
            operations.save(new Node(id: '111',
                    lastValues: [
                            'one'  : [new DwhValueBean(value: 1), new DwhValueBean(value: 11)],
                            'audio': [new DwhValueBean(value: 999)],
                            'other': [new DwhValueBean(value: 2), new DwhValueBean(value: 22)]
                    ]))

            def job = new RemoveAudioFromLastValues(operations)
        when:
            job.execute()
            def node = operations.findById('111', Node.class)
        then:
            !node.lastValues.containsKey('audio')
    }

}
