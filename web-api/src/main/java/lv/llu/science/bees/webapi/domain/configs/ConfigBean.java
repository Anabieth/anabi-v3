package lv.llu.science.bees.webapi.domain.configs;

import lombok.Data;
import lv.llu.science.bees.webapi.domain.nodes.DeviceBean;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ConfigBean {
    private String id;
    private String name;
    private Map<String, Object> config;
    private ZonedDateTime changedTs;
    private String changedBy;
    private List<DeviceBean> devices = new ArrayList<>();
    private Boolean isDefault;
}
