package lv.llu.science.bees.webapi.domain.models;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Document
public class ModelDefinition {
    private String id;
    private String modelCode;
    private Map<String, Object> params;
    private String workspaceId;
    private String createdBy;
}
