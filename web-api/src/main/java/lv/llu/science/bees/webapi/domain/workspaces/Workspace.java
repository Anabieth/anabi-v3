package lv.llu.science.bees.webapi.domain.workspaces;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Data
public class Workspace {
    @Id
    private String id;
    private String name;
    @Indexed
    private String owner;
    @Indexed
    private List<String> invitedUsers;
    private String sharingKey;
}
