package lv.llu.science.bees.webapi.migrations


import lv.llu.science.bees.webapi.domain.deviceLog.DeviceLog
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@SpringBootTest
@ActiveProfiles("embedded")
class RenameDeviceLogCollectionSpec extends Specification {

    @Autowired
    MongoOperations operations

    def "should rename collection"() {
        given:
            operations.save([_id: '111'], "dataInLog")
            operations.save([_id: '222'], "dataInLog")

            def job = new RenameDeviceLogCollection(operations)
        when:
            job.execute()
            def all = operations.findAll(DeviceLog.class)
        then:
            all.collect { it.clientId } == ['111', '222']
    }
}
