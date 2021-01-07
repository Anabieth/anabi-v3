package lv.llu.science.bees.webapi.domain.reports;

import feign.RetryableException;
import lv.llu.science.bees.webapi.dwh.DwhClient;
import lv.llu.science.bees.webapi.utils.DwhCoreUnavailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class ReportService {

    private final DwhClient dwhClient;

    @Autowired
    public ReportService(DwhClient dwhClient) {
        this.dwhClient = dwhClient;
    }

    @Cacheable("reportList")
    public List<ReportBean> getReportList() {
        try {
            return dwhClient.getReportList();
        } catch (RetryableException ex) {
            throw new DwhCoreUnavailableException();
        }
    }

    @PreAuthorize("@user.hasNodeAccess(#nodeId)")
    public ReportDataBean getReportData(String code, String nodeId, ZonedDateTime from, ZonedDateTime to, Integer limit) {
        try {
            return dwhClient.getReportData(code, nodeId, from, to, limit);
        } catch (RetryableException ex) {
            throw new DwhCoreUnavailableException();
        }
    }

    @Cacheable("reportDetails")
    public ReportBean getReportDetails(String code) {
        try {
            return dwhClient.getReportDetails(code);
        } catch (RetryableException ex) {
            throw new DwhCoreUnavailableException();
        }
    }
}
