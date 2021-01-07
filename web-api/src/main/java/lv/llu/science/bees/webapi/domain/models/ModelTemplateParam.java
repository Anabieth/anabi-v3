package lv.llu.science.bees.webapi.domain.models;

import lombok.Data;

import java.util.List;

@Data
public class ModelTemplateParam {
    private String code;
    private String name;
    private String description;
    private String type;
    private Boolean master;
    private List<String> options;
}
