package lv.llu.science.bees.webapi.dwh;

import lombok.Data;

import java.util.List;

@Data
public class DwhDataSetBean {
    private String objectId;
    private String type;
    private List<DwhValueBean> values;
}
