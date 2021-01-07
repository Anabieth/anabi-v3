package lv.llu.science.bees.webapi.migrations;

import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import lombok.extern.java.Log;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

@Component
@Log
public class RenameDeviceLogCollection implements Migration {

    private final MongoOperations operations;

    private final String oldName = "dataInLog";
    private final String newName = "deviceLog";

    @Autowired
    public RenameDeviceLogCollection(MongoOperations operations) {
        this.operations = operations;
    }

    @Override
    public void execute() {
        if (operations.collectionExists(newName)) {
            log.config(newName + " collection already exists, skipping renaming.");
            return;
        }

        if (!operations.collectionExists(oldName)) {
            log.config(oldName + " collection not found, skipping renaming.");
            return;
        }

        log.info("Renaming collection " + oldName + " to " + newName);
        MongoCollection<Document> collection = operations.getCollection(oldName);
        MongoNamespace oldNamespace = collection.getNamespace();
        MongoNamespace newNamespace = new MongoNamespace(oldNamespace.getDatabaseName(), newName);
        collection.renameCollection(newNamespace);
    }
}
