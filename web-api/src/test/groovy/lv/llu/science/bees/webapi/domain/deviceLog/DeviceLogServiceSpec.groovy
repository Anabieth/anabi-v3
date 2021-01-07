package lv.llu.science.bees.webapi.domain.deviceLog


import lv.llu.science.bees.webapi.utils.CurrentUser
import lv.llu.science.bees.webapi.utils.TimeMachine
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import spock.lang.Specification

import static lv.llu.science.bees.webapi.domain.datain.DataInMappingResult.NotFound
import static lv.llu.science.bees.webapi.domain.datain.DataInMappingResult.Ok

class DeviceLogServiceSpec extends Specification {

    def currentUser = Mock(CurrentUser)
    def mongoTemplate = Mock(MongoTemplate)
    def timeMachine = Mock(TimeMachine)
    def service = new DeviceLogService(currentUser, mongoTemplate, timeMachine)

    def "should add device log"() {
        when:
            service.addDeviceLogRecord(new DeviceLogRecord())
        then:
            1 * currentUser.getUsername() >> "dev-123"
            1 * mongoTemplate.upsert(_ as Query, _ as Update, DeviceLog.class) >> { query, update, cls ->
                def str = update.toString()
                assert str =~ '\\$push.+lastLogs.+\\$sort.+ts.+\\$slice.+\\$each'
            }
    }

    def "should add data-in log"() {
        when:
            service.addDataInLogRecord(new DataInLogRecord(result: Ok))
        then:
            1 * currentUser.getUsername() >> "dev-123"
            1 * mongoTemplate.upsert(_ as Query, _ as Update, DeviceLog.class) >> { query, update, cls ->
                def str = update.toString()
                assert str =~ '\\$push.+lastEvents.+\\$sort.+ts.+\\$slice.+\\$each'
                assert !(str =~ '\\$push.+lastErrors.+\\$sort.+ts.+\\$slice.+\\$each')
            }
    }

    def "should add failed data-in log"() {
        when:
            service.addDataInLogRecord(new DataInLogRecord(result: NotFound))
        then:
            1 * currentUser.getUsername() >> "dev-123"
            1 * mongoTemplate.upsert(_ as Query, _ as Update, DeviceLog.class) >> { query, update, cls ->
                def str = update.toString()
                assert str =~ '\\$push.+lastEvents.+\\$sort.+ts.+\\$slice.+\\$each'
                assert str =~ '\\$push.+lastErrors.+\\$sort.+ts.+\\$slice.+\\$each'
            }
    }
}
