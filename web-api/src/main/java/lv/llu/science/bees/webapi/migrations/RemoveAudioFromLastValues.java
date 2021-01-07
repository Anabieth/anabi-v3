package lv.llu.science.bees.webapi.migrations;

import com.mongodb.client.result.UpdateResult;
import lombok.extern.java.Log;
import lv.llu.science.bees.webapi.domain.nodes.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import static java.text.MessageFormat.format;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@Log
public class RemoveAudioFromLastValues implements Migration {

    private final MongoOperations operations;

    @Autowired
    public RemoveAudioFromLastValues(MongoOperations operations) {
        this.operations = operations;
    }

    @Override
    public void execute() {
        UpdateResult result = operations.updateMulti(
                new Query(where("lastValues.audio").exists(true)),
                new Update().unset("lastValues.audio"),
                Node.class);
        if (result.getModifiedCount() > 0) {
            log.info(format("Removed audio from last values for {0} nodes", result.getModifiedCount()));
        }
    }
}
