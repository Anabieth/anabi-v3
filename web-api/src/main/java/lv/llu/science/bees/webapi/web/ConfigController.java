package lv.llu.science.bees.webapi.web;

import lv.llu.science.bees.webapi.domain.configs.ConfigBean;
import lv.llu.science.bees.webapi.domain.configs.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/configs")
public class ConfigController {

    private final ConfigService configService;

    @Autowired
    public ConfigController(ConfigService service) {
        this.configService = service;
    }

    @GetMapping
    public List<ConfigBean> listConfigs() {
        return configService.getAllConfigs();
    }

    @GetMapping(path = "/{id}")
    public ConfigBean getConfigDetails(@PathVariable String id) {
        return configService.getConfigDetails(id);
    }

    @PostMapping
    public ConfigBean addConfig(@RequestBody ConfigBean bean) {
        return configService.createConfig(bean);
    }

    @PutMapping(path = "/{id}")
    public ConfigBean editConfig(@PathVariable String id, @RequestBody ConfigBean bean) {
        return configService.editConfig(id, bean);
    }

    @DeleteMapping(path = "/{id}")
    public void deleteConfig(@PathVariable String id, HttpServletResponse response) {
        configService.deleteConfig(id);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @PutMapping(path = "/{id}/default")
    public ConfigBean setDefault(@PathVariable String id) {
        return configService.setDefault(id);
    }

    @GetMapping("/device")
    public Map<String, Object> getDeviceConfig() {
        return configService.getDeviceConfig();
    }
}
