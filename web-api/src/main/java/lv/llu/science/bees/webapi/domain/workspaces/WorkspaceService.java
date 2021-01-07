package lv.llu.science.bees.webapi.domain.workspaces;

import lv.llu.science.bees.webapi.domain.auth0.SamsUsers;
import lv.llu.science.bees.webapi.domain.nodes.NodeRepository;
import lv.llu.science.bees.webapi.utils.CurrentUser;
import lv.llu.science.bees.webapi.utils.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static lv.llu.science.bees.webapi.domain.workspaces.WorkspaceInviteResultBean.Status.*;

@Service
public class WorkspaceService {

    private final WorkspaceRepository repository;
    private final NodeRepository nodeRepository;
    private final CurrentUser currentUser;
    private final SamsUsers samsUsers;

    @Autowired
    public WorkspaceService(WorkspaceRepository repository, NodeRepository nodeRepository, CurrentUser currentUser, SamsUsers samsUsers) {
        this.repository = repository;
        this.nodeRepository = nodeRepository;
        this.currentUser = currentUser;
        this.samsUsers = samsUsers;
    }

    public List<WorkspaceBean> getWorkspaces() {
        List<Workspace> workspaces;
        if (currentUser.isAdmin()) {
            workspaces = repository.findAll();
        } else {
            workspaces = repository.findByUser(currentUser.getUsername());
        }
        workspaces.sort(comparing(Workspace::getName));

        if (workspaces.stream().noneMatch(ws -> currentUser.getUsername().equals(ws.getOwner()))) {
            Workspace ws = new Workspace();
            ws.setName("My workspace");
            ws.setOwner(currentUser.getUsername());
            workspaces.add(repository.save(ws));
        }

        return workspaces.stream()
                .map(this::convertToBean)
                .collect(toList());
    }

    public WorkspaceBean createWorkspace(WorkspaceBean bean) {
        if (bean.getName() == null || bean.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Workspace can not be null or blank");
        }
        Workspace ws = new Workspace();
        ws.setName(bean.getName());
        ws.setOwner(currentUser.getUsername());
        return convertToBean(repository.save(ws));
    }

    @PreAuthorize("@user.isWorkspaceOwner(#id)")
    public WorkspaceBean updateWorkspace(String id, WorkspaceBean bean) {
        Workspace ws = repository.findById(id).orElseThrow(NotFoundException::new);
        ws.setName(bean.getName());
        return convertToBean(repository.save(ws));
    }

    @PreAuthorize("@user.isWorkspaceOwner(#id)")
    public void deleteWorkspace(String id) {
        Workspace ws = repository.findById(id).orElseThrow(NotFoundException::new);
        Long count = nodeRepository.countByWorkspaceId(ws.getId());

        if (count > 0) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Only empty workspaces can be deleted.");
        }

        repository.delete(ws);
    }

    private WorkspaceBean convertToBean(Workspace ws) {
        WorkspaceBean bean = new WorkspaceBean();
        bean.setId(ws.getId());
        bean.setName(ws.getName());
        bean.setIsOwner(currentUser.getUsername().equals(ws.getOwner()));
        bean.setOwner(username2name(ws.getOwner()));
        if (bean.getIsOwner()) {
            if (ws.getInvitedUsers() != null) {
                bean.setInvitedUsers(
                        ws.getInvitedUsers().stream()
                                .map(username -> new InvitedUser(username, username2name(username)))
                                .collect(Collectors.toList())
                );
            }
            bean.setSharingKey(ws.getSharingKey());
        }
        return bean;
    }

    private String username2name(String username) {
        return samsUsers.getUserMap().getOrDefault(username, username);
    }

    @PreAuthorize("@user.isWorkspaceOwner(#id)")
    public WorkspaceBean shareWorkspace(String id) {
        Workspace ws = repository.findById(id).orElseThrow(NotFoundException::new);
        ws.setSharingKey(UUID.randomUUID().toString());
        return convertToBean(repository.save(ws));
    }

    @PreAuthorize("@user.isWorkspaceOwner(#id)")
    public WorkspaceBean unshareWorkspace(String id) {
        Workspace ws = repository.findById(id).orElseThrow(NotFoundException::new);
        ws.setSharingKey(null);
        return convertToBean(repository.save(ws));
    }

    @PreAuthorize("@user.isWorkspaceOwner(#id) or @user.getUsername().equals(#userName)")
    public WorkspaceBean removeUser(String id, String userName) {
        Workspace ws = repository.findById(id).orElseThrow(NotFoundException::new);
        boolean removed = ws.getInvitedUsers().removeIf(u -> u.equals(userName));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to remove user");
        }

        return convertToBean(repository.save(ws));
    }

    public WorkspaceInviteResultBean acceptInvite(String id, WorkspaceInviteBean bean) {
        Workspace ws = repository.findById(id).orElseThrow(NotFoundException::new);
        if (!bean.getKey().equals(ws.getSharingKey())) {
            return new WorkspaceInviteResultBean(NotValid);
        }

        if (currentUser.getUsername().equals(ws.getOwner())) {
            return new WorkspaceInviteResultBean(OwnWorkspace);
        }

        if (ws.getInvitedUsers() == null) {
            ws.setInvitedUsers(new ArrayList<>());
        }

        boolean alreadyJoined = ws.getInvitedUsers().stream()
                .anyMatch(user -> currentUser.getUsername().equals(user));

        if (alreadyJoined) {
            return new WorkspaceInviteResultBean(AlreadyJoined);
        }

        WorkspaceInviteResultBean result = new WorkspaceInviteResultBean();

        if (bean.getConfirm()) {
            ws.getInvitedUsers().add(currentUser.getUsername());
            ws = repository.save(ws);
            result.setStatus(Joined);
        } else {
            result.setStatus(Valid);
        }

        result.setWorkspace(convertToBean(ws));
        return result;
    }
}
