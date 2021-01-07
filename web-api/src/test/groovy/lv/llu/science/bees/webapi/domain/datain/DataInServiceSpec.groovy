package lv.llu.science.bees.webapi.domain.datain

import feign.RetryableException
import lv.llu.science.bees.webapi.domain.deviceLog.DeviceLogService
import lv.llu.science.bees.webapi.domain.mapping.MappingService
import lv.llu.science.bees.webapi.domain.mapping.SourceMapping
import lv.llu.science.bees.webapi.domain.nodes.Node
import lv.llu.science.bees.webapi.domain.nodes.NodeRepository
import lv.llu.science.bees.webapi.domain.nodes.NodeService
import lv.llu.science.bees.webapi.domain.nodes.latestValues.NodeLatestValueService
import lv.llu.science.bees.webapi.dwh.DwhClient
import lv.llu.science.bees.webapi.dwh.DwhDataSetBean
import lv.llu.science.bees.webapi.dwh.DwhValueBean
import lv.llu.science.bees.webapi.utils.NotFoundException
import lv.llu.science.bees.webapi.utils.TimeMachine
import org.springframework.security.access.AccessDeniedException
import spock.lang.Specification

import javax.validation.ConstraintViolation
import javax.validation.Validator
import java.time.ZonedDateTime

import static lv.llu.science.bees.webapi.domain.datain.DataInMappingResult.*

class DataInServiceSpec extends Specification {

    def dwhClient = Mock(DwhClient)
    def mappingService = Mock(MappingService)
    def timeMachine = Mock(TimeMachine)
    def nodeService = Mock(NodeService)
    def latestValueService = Mock(NodeLatestValueService)
    def nodeRepository = Mock(NodeRepository)
    def logService = Mock(DeviceLogService)
    def validator = Mock(Validator) {
        validate(_) >> []
    }

    def service = new DataInService(dwhClient, mappingService, timeMachine, nodeService, latestValueService, logService, nodeRepository, validator)

    def "should add new measurements"() {
        given:
            def valuesA = [new DwhValueBean(ts: null, value: 33.2f),
                           new DwhValueBean(ts: null, value: 33.5f)]
            def valuesB = [new DwhValueBean(ts: null, value: 45.3f)]
            def valuesC = [new DwhValueBean(ts: null, values: [150, 200, 170])]

            def data = [new DataInBean(sourceId: "temp-id-000", values: valuesA),
                        new DataInBean(sourceId: "hum-id-123", values: valuesB),
                        new DataInBean(sourceId: "audio-id-111", values: valuesC)]

            mappingService.getMapping("temp-id-000") >> new SourceMapping(valueKey: "temperature", nodeId: "hive-000")
            mappingService.getMapping("hum-id-123") >> new SourceMapping(valueKey: "humidity", nodeId: "hive-123")
            mappingService.getMapping("audio-id-111") >> new SourceMapping(valueKey: "audio", nodeId: "hive-123")

            def expectedTempBean = new DwhDataSetBean(objectId: 'hive-000', type: 'temperature', values: valuesA)
            def expectedHumBean = new DwhDataSetBean(objectId: 'hive-123', type: 'humidity', values: valuesB)
            def expectedAudioBean = new DwhDataSetBean(objectId: 'hive-123', type: 'audio', values: valuesC)

        when:
            def result = service.addMeasurements(data)
        then:
            result == ["temp-id-000": Ok, "hum-id-123": Ok, "audio-id-111": Ok]
            1 * dwhClient.sendData(expectedTempBean)
            1 * latestValueService.pushLatestValues('hive-000', 'temperature', valuesA)

            1 * dwhClient.sendData(expectedHumBean)
            1 * latestValueService.pushLatestValues('hive-123', 'humidity', valuesB)

            1 * dwhClient.sendData(expectedAudioBean)
            1 * latestValueService.pushLatestValues('hive-123', 'audio', valuesC)

            3 * nodeRepository.getNode(_) >> new Node()
            3 * timeMachine.zonedNow()

            3 * logService.addDataInLogRecord(_)
    }

    def "should add new measurements without POST time stamp"() {
        given:
            def values = [new DwhValueBean(value: 33.2f),
                          new DwhValueBean(value: 33.5f),
                          new DwhValueBean(value: 34.7f)]
            def data = [new DataInBean(sourceId: "temp-id-000", values: values, tint: 5)]
            mappingService.getMapping("temp-id-000") >> new SourceMapping(valueKey: "temperature", nodeId: "hive-000")

            def expectedValues = [new DwhValueBean(ts: ZonedDateTime.parse('2020-02-03T14:59:50Z'), value: 33.2f),
                                  new DwhValueBean(ts: ZonedDateTime.parse('2020-02-03T14:59:55Z'), value: 33.5f),
                                  new DwhValueBean(ts: ZonedDateTime.parse('2020-02-03T15:00:00Z'), value: 34.7f)]
            def expectedTempBean = new DwhDataSetBean(objectId: 'hive-000', type: 'temperature', values: expectedValues)
        when:
            def result = service.addMeasurements(data)
        then:
            result == ["temp-id-000": Ok]
            2 * timeMachine.zonedNow() >> ZonedDateTime.parse('2020-02-03T15:00:00Z')
            1 * dwhClient.sendData(expectedTempBean)
            1 * latestValueService.pushLatestValues('hive-000', 'temperature', values)
            1 * nodeRepository.getNode(_) >> new Node()


    }

    def "should handle failed mappings"() {
        given:
            def data = [new DataInBean(sourceId: "temp-id-000", values: []),
                        new DataInBean(sourceId: "hum-id-123", values: [])]

            mappingService.getMapping("temp-id-000") >> { throw new NotFoundException() }
            mappingService.getMapping("hum-id-123") >> { throw new AccessDeniedException("testing") }
        when:
            def result = service.addMeasurements(data)
        then:
            result == ["temp-id-000": NotFound, "hum-id-123": AccessDenied]
            0 * dwhClient.sendData(_)
            0 * nodeService.pushLatestValues(_)
            0 * nodeRepository.getNode(_)
            2 * timeMachine.zonedNow()
            2 * logService.addDataInLogRecord(_)
    }

    def "should handle failed core request"() {
        given:
            def data = [new DataInBean(sourceId: "temp-id-000", values: [])]

            mappingService.getMapping("temp-id-000") >> new SourceMapping(valueKey: "temperature", nodeId: "hive-000")
        when:
            def result = service.addMeasurements(data)
        then:
            result == ["temp-id-000": CoreTemporarilyUnavailable]
            1 * dwhClient.sendData(_) >> { throw new RetryableException("testing", null) }
            0 * nodeService.pushLatestValues(_)
            0 * nodeRepository.getNode(_)
            1 * timeMachine.zonedNow()
            1 * logService.addDataInLogRecord(_)
    }

    def "should handle failed validation"() {
        given:
            def data = [new DataInBean(sourceId: "temp-id-000", values: null)]
        when:
            def result = service.addMeasurements(data)
        then:
            1 * validator.validate(_) >> [Stub(ConstraintViolation)]

            result == ["temp-id-000": ValidationError]
            0 * dwhClient.sendData(_)
            0 * nodeService.pushLatestValues(_)
            1 * logService.addDataInLogRecord(_)
    }
}
