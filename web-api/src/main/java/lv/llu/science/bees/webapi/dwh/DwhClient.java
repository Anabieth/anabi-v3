package lv.llu.science.bees.webapi.dwh;

import lv.llu.science.bees.webapi.domain.models.ModelDefinitionBean;
import lv.llu.science.bees.webapi.domain.models.ModelTemplate;
import lv.llu.science.bees.webapi.domain.nodes.latestValues.LatestModelValueBean;
import lv.llu.science.bees.webapi.domain.reports.ReportBean;
import lv.llu.science.bees.webapi.domain.reports.ReportDataBean;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Component
@FeignClient(name = "dwh-core-service", url = "${sams.dwh.address}")
public interface DwhClient {

    @PostMapping("/dwh")
    Map<String, Object> sendData(DwhDataSetBean bean);

    @Cacheable("reportList")
    @GetMapping("/dwh/reports")
    List<ReportBean> getReportList();

    @Cacheable("reportDetails")
    @GetMapping("/dwh/reports/{code}")
    ReportBean getReportDetails(@PathVariable("code") String code);

    @GetMapping("/dwh/reports/{code}/{objectId}")
    ReportDataBean getReportData(@PathVariable("code") String code,
                                 @PathVariable("objectId") String objectId,
                                 @RequestParam(value = "from", required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                         ZonedDateTime from,
                                 @RequestParam(value = "to", required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                         ZonedDateTime to,
                                 @RequestParam(value = "limit", required = false)
                                         Integer limit);

    @Cacheable("modelTemplates")
    @GetMapping("/dwh/models")
    List<ModelTemplate> getModelList();

    @PostMapping("/dwh/models/{code}")
    void saveModelDefinition(@PathVariable("code") String code, @RequestBody ModelDefinitionBean bean);

    @DeleteMapping("/dwh/models/{code}/{id}")
    void deleteModelDefinition(@PathVariable("code") String code, @PathVariable("id") String id);

    @GetMapping("dwh/models/latest/{nodeId}")
    List<LatestModelValueBean> getModelLatestValues(@PathVariable("nodeId") String nodeId);

    @Cacheable("topicList")
    @GetMapping("/dwh/topics")
    List<String> getSupportedValueKeys();
}
