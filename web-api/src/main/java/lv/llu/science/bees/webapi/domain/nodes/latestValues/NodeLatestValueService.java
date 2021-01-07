package lv.llu.science.bees.webapi.domain.nodes.latestValues;

import lv.llu.science.bees.webapi.domain.nodes.Node;
import lv.llu.science.bees.webapi.dwh.DwhClient;
import lv.llu.science.bees.webapi.dwh.DwhValueBean;
import lv.llu.science.bees.webapi.utils.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class NodeLatestValueService {

    private final MongoOperations mongoOperations;
    private final DwhClient dwhClient;

    @Autowired
    public NodeLatestValueService(MongoOperations mongoOperations, DwhClient dwhClient) {
        this.mongoOperations = mongoOperations;
        this.dwhClient = dwhClient;
    }

    @PreAuthorize("@user.isAllowedDevice(#id)")
    public void pushLatestValues(String id, String type, List<DwhValueBean> values) {
        if (type.equals("audio")) {
            // exclude audio data from last values
            return;
        }

        Query query = new Query(where("_id").is(id));
        Update update = new Update().push("lastValues." + type)
                .sort(Sort.by("ts").descending())
                .slice(10)
                .each(values);
        mongoOperations.updateFirst(query, update, Node.class);
    }

    @PreAuthorize("@user.hasNodeAccess(#nodeId)")
    public LatestValuesBean getLatestValues(String nodeId) {
        Node node = fetchLatestValues(nodeId);

        LatestValuesBean bean = new LatestValuesBean();
        bean.setId(node.getId());
        bean.setLatestMeasurements(node.getLastValues().entrySet().stream()
                .map(entry -> {
                    LatestMeasurementBean latest = new LatestMeasurementBean();
                    latest.setType(entry.getKey());
                    DwhValueBean value = entry.getValue().get(0);
                    latest.setValue(value.getValue());
                    latest.setTimestamp(value.getTs());
                    return latest;
                })
                .collect(toList())
        );

        bean.setLatestModelValues(dwhClient.getModelLatestValues(nodeId));
        return bean;
    }

    private Node fetchLatestValues(String nodeId) {
        Query query = query(where("id").is(nodeId));
        query.fields().include("lastValues");
        return Optional.ofNullable(mongoOperations.findOne(query, Node.class)).orElseThrow(NotFoundException::new);
    }

    @PreAuthorize("@user.hasNodeAccess(#nodeId)")
    public Map<String, List<DwhValueBean>> getLatestMeasurements(String nodeId) {
        Node node = fetchLatestValues(nodeId);
        return node.getLastValues();
    }
}
