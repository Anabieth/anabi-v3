package lv.llu.science.bees.webapi.domain.workspaces;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WorkspaceInviteResultBean {
    private WorkspaceBean workspace;
    private Status status;

    public WorkspaceInviteResultBean(Status status) {
        this.status = status;
    }

    public enum Status {
        Valid,
        Joined,
        AlreadyJoined,
        OwnWorkspace,
        NotValid
    }
}
