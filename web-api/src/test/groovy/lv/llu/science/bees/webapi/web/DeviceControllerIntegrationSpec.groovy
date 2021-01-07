package lv.llu.science.bees.webapi.web

import lv.llu.science.bees.webapi.domain.deviceLog.DataInLogRecord
import lv.llu.science.bees.webapi.domain.deviceLog.DeviceLog
import lv.llu.science.bees.webapi.domain.deviceLog.DeviceLogRecord
import lv.llu.science.bees.webapi.domain.deviceLog.DeviceLogReposotory
import lv.llu.science.bees.webapi.domain.nodes.Node
import lv.llu.science.bees.webapi.domain.nodes.NodeRepository
import lv.llu.science.bees.webapi.web.helpers.RestDocsSpecification
import org.springframework.beans.factory.annotation.Autowired

import java.time.ZonedDateTime

import static lv.llu.science.bees.webapi.domain.datain.DataInMappingResult.*
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class DeviceControllerIntegrationSpec extends RestDocsSpecification {

    @Autowired
    NodeRepository repository

    @Autowired
    DeviceLogReposotory logReposotory

    def setup() {
        setupDefaultWorkspace()
        def now = ZonedDateTime.now()

        repository.insert([
                new Node(id: 'dev1', name: 'Demo ESP8266', type: 'DEVICE', workspaceId: 'ws1',
                        clientId: 'demo-client-1', isActive: true),
                new Node(id: 'dev2', name: 'Demo RPi3', type: 'DEVICE', workspaceId: 'ws1',
                        clientId: 'demo-client-2', isActive: true, hwConfigId: 'hw-123'),
        ])

        logReposotory.insert([
                new DeviceLog(clientId: 'demo-client-1',
                        lastErrors: [
                                new DataInLogRecord(ts: now.minusHours(11), sourceId: 'demo-sensor-X', result: NotFound),
                                new DataInLogRecord(ts: now.minusHours(21), sourceId: 'demo-sensor-Y', result: AccessDenied),
                        ],
                        lastEvents: [
                                new DataInLogRecord(ts: now.minusMinutes(10), sourceId: 'demo-sensor-A', result: Ok,
                                        nodeId: 'hive1', nodeName: 'First hive', type: 'temperature'),
                                new DataInLogRecord(ts: now.minusHours(11), sourceId: 'demo-sensor-X', result: NotFound),
                                new DataInLogRecord(ts: now.minusMinutes(12), sourceId: 'demo-sensor-B', result: Ok,
                                        nodeId: 'hive1', nodeName: 'First hive', type: 'weight'),
                                new DataInLogRecord(ts: now.minusMinutes(20), sourceId: 'demo-sensor-A', result: Ok,
                                        nodeId: 'hive1', nodeName: 'First hive', type: 'temperature'),
                                new DataInLogRecord(ts: now.minusHours(21), sourceId: 'demo-sensor-Y', result: AccessDenied),
                                new DataInLogRecord(ts: now.minusMinutes(22), sourceId: 'demo-sensor-B', result: Ok,
                                        nodeId: 'hive1', nodeName: 'First hive', type: 'weight'),
                                new DataInLogRecord(ts: now.minusMinutes(30), sourceId: 'demo-sensor-C', result: Ok,
                                        nodeId: 'apiary1', nodeName: 'Demo apiary', type: 'humidity'),
                        ],
                        lastLogs: [
                                new DeviceLogRecord(ts: now.minusMinutes(2), level: 'debug', message: 'Demo device is online'),
                                new DeviceLogRecord(ts: now.minusMinutes(5), level: 'error', message: 'Communication error E0042')
                        ]
                ),
                new DeviceLog(clientId: 'demo-client-2',
                        lastErrors: [
                                new DataInLogRecord(ts: now.minusHours(30), sourceId: 'demo-sensor-Z', result: ValidationError),
                                new DataInLogRecord(ts: now.minusHours(40), sourceId: 'demo-sensor-W', result: CoreTemporarilyUnavailable),
                        ],
                        lastEvents: [
                                new DataInLogRecord(ts: now.minusMinutes(4), sourceId: 'demo-sensor-D', result: Ok,
                                        nodeId: 'hive2', nodeName: 'Second hive', type: 'weight'),
                                new DataInLogRecord(ts: now.minusMinutes(7), sourceId: 'demo-sensor-E', result: Ok,
                                        nodeId: 'apiary1', nodeName: 'Demo apiary', type: 'voltage'),
                                new DataInLogRecord(ts: now.minusHours(30), sourceId: 'demo-sensor-Z', result: ValidationError),
                                new DataInLogRecord(ts: now.minusHours(40), sourceId: 'demo-sensor-W', result: CoreTemporarilyUnavailable),
                        ],
                        lastLogs: [
                                new DeviceLogRecord(ts: now.minusMinutes(50), level: 'info', message: 'System startup')
                        ]
                )
        ])


    }

    def cleanup() {
        repository.deleteAll()
        logReposotory.deleteAll()
    }

    def "should get device list"() {
        expect:
            mvc.perform(
                    get('/devices'))
                    .andExpect(status().isOk())
                    .andDo(document('device-list',
                            responseFields(
                                    fieldWithPath('[].id').description('Device (node) ID'),
                                    fieldWithPath('[].name').description('Device name'),
                                    fieldWithPath('[].clientId').description('Client ID used for authentication'),
                                    fieldWithPath('[].isActive').description('Enabled / Disabled status of device'),
                                    fieldWithPath('[].lastEvent.*').description('Latest data-in event produced by the device'),
                                    fieldWithPath('[].lastEvent.ts').description('Event ts'),
                                    fieldWithPath('[].lastEvent.sourceId').description('Source ID provided in the event'),
                                    fieldWithPath('[].lastEvent.result')
                                            .description('Event result: +\n' +
                                                    '`Ok` - Data-In request successfully processed and stored +\n' +
                                                    '`ValidationError` - Data-In request is not valid (missing required fields and/or values) +\n' +
                                                    '`NotFound` - Mapping for provided Source ID not found *or* sending device is not activated +\n' +
                                                    '`AccessDenied` - Source mapping is found, but device is not authorized to provide data (belongs to different workspace) +\n' +
                                                    '`CoreTemporarilyUnavailable` - Data-In request was valid, but data was not stored (DW Core maintenance)'),
                                    fieldWithPath('[].lastEvent.type').optional().description('(if successful) Type of incoming data'),
                                    fieldWithPath('[].lastEvent.nodeId').optional().description('(if successful) Node ID for which incoming data assigned'),
                                    fieldWithPath('[].lastEvent.nodeName').optional().description('(if successful) Node name for which incoming data assigned'),
                                    fieldWithPath('[].lastError.*').description('Latest *failed* data-in event produced by the device'),
                                    fieldWithPath('[].lastLog.*').description('Latest log message sent by the device'),
                            )
                    ))
    }

    def "should get device events"() {
        expect:
            mvc.perform(
                    get('/devices/dev1/events'))
                    .andExpect(status().isOk())
                    .andDo(document('device-events',
                            responseFields(
                                    fieldWithPath('name').description('Device name'),
                                    fieldWithPath('events[].*').description('Latest 20 data-in events (any result)'),
                                    fieldWithPath('errors[].*').description('Latest 10 failed data-in events (result != Ok)'),
                                    fieldWithPath('logs[].*').description('Latest 100 log messages'),
                            )
                    ))
    }

    def "should toggle device activity"() {
        expect:
            mvc.perform(
                    put('/devices/dev2/active'))
                    .andExpect(status().isOk())
                    .andDo(document('device-toggle-active'))
    }
}
