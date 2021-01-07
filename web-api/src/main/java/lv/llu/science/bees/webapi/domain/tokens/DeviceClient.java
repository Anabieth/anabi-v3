package lv.llu.science.bees.webapi.domain.tokens;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class DeviceClient {
    @Id
    private String id;
    private String secret;
    private String name;
    private String description;
}
