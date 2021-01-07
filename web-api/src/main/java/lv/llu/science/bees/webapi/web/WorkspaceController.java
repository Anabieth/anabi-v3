package lv.llu.science.bees.webapi.web;

import lv.llu.science.bees.webapi.domain.workspaces.WorkspaceBean;
import lv.llu.science.bees.webapi.domain.workspaces.WorkspaceInviteBean;
import lv.llu.science.bees.webapi.domain.workspaces.WorkspaceInviteResultBean;
import lv.llu.science.bees.webapi.domain.workspaces.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workspaces")
public class WorkspaceController {

    private final WorkspaceService service;

    @Autowired
    public WorkspaceController(WorkspaceService service) {
        this.service = service;
    }

    @GetMapping
    public List<WorkspaceBean> listWorkspaces() {
        return service.getWorkspaces();
    }

    @PostMapping
    public WorkspaceBean createWorkspace(@RequestBody WorkspaceBean bean) {
        return service.createWorkspace(bean);
    }

    @PutMapping(path = "{id}")
    public WorkspaceBean updateWorkspace(@PathVariable String id, @RequestBody WorkspaceBean bean) {
        return service.updateWorkspace(id, bean);
    }

    @DeleteMapping(path = "{id}")
    public void deleteWorkspace(@PathVariable String id, HttpServletResponse response) {
        service.deleteWorkspace(id);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @PostMapping(path = "{id}/share")
    public WorkspaceBean shareWorkspace(@PathVariable String id) {
        return service.shareWorkspace(id);
    }

    @DeleteMapping(path = "{id}/share")
    public WorkspaceBean unshareWorkspace(@PathVariable String id) {
        return service.unshareWorkspace(id);
    }

    @DeleteMapping(path = "{id}/share/users")
    public WorkspaceBean removeUser(@PathVariable String id, @RequestBody Map<String, String> userName) {
        return service.removeUser(id, userName.get("userName"));
    }

    @PostMapping(path = "{id}/invite")
    public WorkspaceInviteResultBean acceptInvite(@PathVariable String id, @RequestBody WorkspaceInviteBean bean, HttpServletResponse response) {
        WorkspaceInviteResultBean result = service.acceptInvite(id, bean);
        switch (result.getStatus()) {
            case OwnWorkspace:
            case AlreadyJoined:
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                break;
            case NotValid:
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_OK);
        }
        return result;
    }
}
