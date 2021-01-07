package lv.llu.science.bees.webapi.domain.mapping;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@CompoundIndexes({
        @CompoundIndex(def = "{'nodeId': 1, 'valueKey': 1}", unique = true)
})
public class SourceMapping {
    @Id
    private String sourceId;
    private String valueKey;
    private String nodeId;
}
