package lv.llu.science.bees.webapi.migrations;

import lombok.extern.java.Log;
import lv.llu.science.bees.webapi.domain.nodes.Node;
import lv.llu.science.bees.webapi.domain.workspaces.Workspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.text.MessageFormat.format;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

@Component
@Log
public class CreateMissingWorkspaces implements Migration {

    private final MongoOperations operations;

    @Autowired
    public CreateMissingWorkspaces(MongoOperations operations) {
        this.operations = operations;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute() {
        List<Node> nodes = operations.find(query(where("workspaceId").exists(false)), Node.class);

        nodes.forEach(node -> {
            log.info(format("Setting workspace for node id={0}, name={1}", node.getId(), node.getName()));

            Workspace ws = operations.findOne(
                    query(where("owner").is(node.getOwner())),
                    Workspace.class);

            if (ws == null) {
                Workspace newWs = new Workspace();
                newWs.setName("My workspace");
                newWs.setOwner(node.getOwner());
                ws = operations.insert(newWs);
                log.info(format("Created default workspace id={0}, owner={0}", ws.getId(), ws.getOwner()));
            }

            operations.updateFirst(
                    query(where("id").is(node.getId())),
                    update("workspaceId", ws.getId()).set("createdBy", node.getOwner()),
                    Node.class
            );
        });
    }
}
