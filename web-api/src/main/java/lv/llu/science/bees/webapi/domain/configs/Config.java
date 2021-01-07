package lv.llu.science.bees.webapi.domain.configs;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;
import java.util.Map;

@Data
@Document
public class Config {
    @Id
    private String id;
    private String name;
    private Map<String, Object> config;
    private ZonedDateTime changedTs;
    private String changedBy;
    @Indexed(unique = true, sparse = true)
    private Boolean isDefault;
}
