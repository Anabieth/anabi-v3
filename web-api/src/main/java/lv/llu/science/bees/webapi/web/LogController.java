package lv.llu.science.bees.webapi.web;

import lv.llu.science.bees.webapi.domain.deviceLog.DeviceLogRecord;
import lv.llu.science.bees.webapi.domain.deviceLog.DeviceLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/logs")
public class LogController {
    private final DeviceLogService service;

    @Autowired
    public LogController(DeviceLogService service) {
        this.service = service;
    }

    @PostMapping
    public void addDeviceLogRecord(@RequestBody DeviceLogRecord record, HttpServletResponse response) {
        service.addDeviceLogRecord(record);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

}
