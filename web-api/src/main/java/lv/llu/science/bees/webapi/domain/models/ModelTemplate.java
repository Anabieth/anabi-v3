package lv.llu.science.bees.webapi.domain.models;

import lombok.Data;

import java.util.List;

@Data
public class ModelTemplate {
    private String code;
    private String name;
    private String description;
    private List<ModelTemplateParam> params;
}
