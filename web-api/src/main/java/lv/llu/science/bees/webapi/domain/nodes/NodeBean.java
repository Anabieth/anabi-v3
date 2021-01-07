package lv.llu.science.bees.webapi.domain.nodes;

import lombok.Data;

@Data
public class NodeBean {
    private String id;
    private String name;
    private String type;
    private String parentId;
    private String location;
    private String clientId;
    private Boolean isActive;
    private String hwConfigId;
//    private Map<String, List<DwhValueBean>> lastValues = new HashMap<>();
}
