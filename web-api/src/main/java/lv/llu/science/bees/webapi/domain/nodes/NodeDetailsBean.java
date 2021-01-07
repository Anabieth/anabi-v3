package lv.llu.science.bees.webapi.domain.nodes;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
public class NodeDetailsBean extends NodeBean {
    private List<NodeBean> ancestors = new ArrayList<>();
    private List<NodeBean> children = new ArrayList<>();
}
