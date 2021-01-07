package lv.llu.science.bees.webapi.domain.workspaces;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;


@RequestScope
@Component("activeWorkspace")
public class ActiveWorkspace {

    private String id;

    @PreAuthorize("@user.hasWorkspaceAccess(#id)")
    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
