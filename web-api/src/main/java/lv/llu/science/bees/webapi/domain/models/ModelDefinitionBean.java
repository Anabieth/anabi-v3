package lv.llu.science.bees.webapi.domain.models;

import lombok.Data;

import java.util.Map;

@Data
public class ModelDefinitionBean {
    private String id;
    private String modelCode;
    private Map<String, Object> params;
}
