package lv.llu.science.bees.webapi.domain.reports;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LabeledValues<T> {

    public LabeledValues(String name) {
        this.name = name;
    }

    public LabeledValues() {
    }

    private String name;
    private String type;
    private List<String> categories;
    private List<T> values = new ArrayList<>();
}
