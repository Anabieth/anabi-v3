package lv.llu.science.bees.webapi.web;

import lv.llu.science.bees.webapi.domain.reports.ReportBean;
import lv.llu.science.bees.webapi.domain.reports.ReportDataBean;
import lv.llu.science.bees.webapi.domain.reports.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService service;

    @Autowired
    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping
    public List<ReportBean> listReports() {
        return service.getReportList();
    }

    @GetMapping(path = "/{code}")
    public ReportBean getReportDetails(@PathVariable String code) {
        return service.getReportDetails(code);
    }

    @GetMapping(path = "/{code}/{nodeId}")
    public ReportDataBean getReportData(@PathVariable String code,
                                        @PathVariable String nodeId,
                                        @RequestParam(value = "from", required = false)
                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                ZonedDateTime from,
                                        @RequestParam(value = "to", required = false)
                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                ZonedDateTime to,
                                        @RequestParam(value = "limit", required = false)
                                                Integer limit) {
        return service.getReportData(code, nodeId, from, to, limit);
    }
}
