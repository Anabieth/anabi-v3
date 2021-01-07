package lv.llu.science.bees.webapi.domain.workspaces;

import lombok.Data;

import java.util.List;

@Data
public class WorkspaceBean {
    private String id;
    private String name;
    private Boolean isOwner;
    private String owner;
    private String sharingKey;
    private List<InvitedUser> invitedUsers;
}
