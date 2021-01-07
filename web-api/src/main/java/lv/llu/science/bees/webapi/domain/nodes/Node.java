package lv.llu.science.bees.webapi.domain.nodes;

import lombok.Data;
import lv.llu.science.bees.webapi.dwh.DwhValueBean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document
@Data
public class Node {
    @Id
    private String id;
    private String name;
    private String type;
    private String parentId;
    /**
     * Migrated to workspaces.
     */
    @Deprecated
    private String owner;
    private String location;
    @Indexed(unique = true, sparse = true)
    private String clientId;
    private Boolean isActive;
    @Indexed
    private List<String> ancestors = new ArrayList<>();
    private Map<String, List<DwhValueBean>> lastValues = new HashMap<>();
    private String workspaceId;
    private String createdBy;

    private String hwConfigId;
}
