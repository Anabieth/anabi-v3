package lv.llu.science.bees.webapi.web

import com.github.tomakehurst.wiremock.client.WireMock
import groovy.json.JsonOutput
import lv.llu.science.bees.webapi.domain.reports.ReportBean
import lv.llu.science.bees.webapi.web.helpers.RestDocsSpecification

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import static org.springframework.restdocs.request.RequestDocumentation.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ReportControllerIntegrationSpec extends RestDocsSpecification {

    def setup() {
        setupDefaultWorkspace()
        setupDefaultNodes()
    }

    def "should get report list"() {
        given:
            def dwhResponse = [
                    new ReportBean(code: 'demo-report-1', name: 'First demo report'),
                    new ReportBean(code: 'demo-report-2', name: 'Second demo report')
            ]
            WireMock.stubFor(WireMock.get("/dwh/reports")
                    .willReturn(WireMock.ok()
                            .withBody(JsonOutput.toJson(dwhResponse))
                            .withHeader('Content-Type', 'application/json')
                    )
            )
        expect:
            mvc.perform(
                    get('/reports'))
                    .andExpect(status().isOk())
                    .andDo(document('report-list',
                            responseFields(
                                    fieldWithPath('[].code').description('Report code'),
                                    fieldWithPath('[].name').description('Report name')
                            )
                    ))
    }

    def "should get report data"() {
        given:
            def now = ZonedDateTime.now()
                    .withZoneSameInstant(ZoneId.of('Z'))
                    .withNano(0)
                    .withSecond(0)

            def json = new File(getClass().getResource('/demo-report.json').toURI()).getText()

            WireMock.stubFor(WireMock.get(WireMock.urlMatching("/dwh/reports/demo-report/hive1.+"))
                    .willReturn(WireMock.ok()
                            .withBody(json)
                            .withHeader('Content-Type', 'application/json')
                    )
            )

        expect:
            mvc.perform(
                    get('/reports/{code}/{nodeId}?from={from}&to={to}&limit={limit}',
                            'demo-report', 'hive1',
                            now.minusDays(7).format(DateTimeFormatter.ISO_DATE_TIME),
                            now.format(DateTimeFormatter.ISO_DATE_TIME),
                            500))
                    .andExpect(status().isOk())
                    .andDo(document('report-data',
                            pathParameters(
                                    parameterWithName('code').description('Report code'),
                                    parameterWithName('nodeId').description('Node ID '),
                            ),
                            requestParameters(
                                    parameterWithName('from').description('Start of report period'),
                                    parameterWithName('to').description('End of report period'),
                                    parameterWithName('limit').description('Limit the number of data points in the report. +\n' +
                                            '*Random sampling* is applied if number of actual data points exceeds requested limit.'),
                            ),
                            responseFields(
                                    fieldWithPath('code').description('Report code'),
                                    fieldWithPath('name').description('Report name'),
                                    fieldWithPath('data[]').description('List of data series'),
                                    fieldWithPath('data[].name').description('Name of a data series'),
                                    fieldWithPath('data[].type').description('Type of a data series. ' +
                                            'Common values are `timestamp`, `category`, and other measured value types.'),
                                    fieldWithPath('data[].categories').optional()
                                            .description('Only for category series: list of all possible options'),
                                    fieldWithPath('data[].values[]').description('List of actual values of the series'),
                            )
                    ))
    }
}
