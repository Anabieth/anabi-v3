package lv.llu.science.bees.webapi.utils;

import lv.llu.science.bees.webapi.domain.models.ModelDefinition;
import lv.llu.science.bees.webapi.domain.models.ModelRepository;
import lv.llu.science.bees.webapi.domain.nodes.Node;
import lv.llu.science.bees.webapi.domain.nodes.NodeRepository;
import lv.llu.science.bees.webapi.domain.workspaces.Workspace;
import lv.llu.science.bees.webapi.domain.workspaces.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component("user")
public class CurrentUser {

    private final NodeRepository nodeRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ModelRepository modelRepository;

    @Autowired
    public CurrentUser(NodeRepository nodeRepository, WorkspaceRepository workspaceRepository, ModelRepository modelRepository) {
        this.nodeRepository = nodeRepository;
        this.workspaceRepository = workspaceRepository;
        this.modelRepository = modelRepository;
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean hasAuthority(String role) {
        return getAuthentication().getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("SCOPE_" + role));
    }

    public String getUsername() {
        Authentication auth = getAuthentication();

        if (auth instanceof UsernamePasswordAuthenticationToken) {
            return auth.getName();
        } else if (auth instanceof JwtAuthenticationToken) {
            return auth.getName();
        } else {
            return auth.getPrincipal().toString();
        }
    }

    public boolean hasNodeAccess(String nodeId) {
        return isAdmin() ||
                isUser() &&
                        nodeRepository.findWorkspaceById(nodeId)
                                .map(Node::getWorkspaceId)
                                .map(this::hasWorkspaceAccess)
                                .orElse(false);
    }

    public boolean isAllowedDevice(String nodeId) {
        return isDevice() &&
                nodeRepository.findWorkspaceById(nodeId)
                        .map(Node::getWorkspaceId)
                        .flatMap(wsId -> nodeRepository.findAllowedDeviceInWorkspace(wsId, this.getUsername()))
                        .isPresent();
    }

    public boolean isWorkspaceOwner(String wsId) {
        return isUser() && workspaceRepository.findById(wsId)
                .map(this::isWorkspaceOwner)
                .orElse(false);
    }

    public boolean hasWorkspaceAccess(String workspaceId) {
        return isAdmin() ||
                isUser() && workspaceRepository.findById(workspaceId)
                        .map(w -> isWorkspaceOwner(w) || isWorkspaceParticipant(w))
                        .orElse(false);
    }

    public boolean hasModelAccess(String modelId) {
        return isAdmin() ||
                isUser() && modelRepository.findById(modelId)
                        .map(ModelDefinition::getWorkspaceId)
                        .map(this::hasWorkspaceAccess)
                        .orElse(false);
    }

    private boolean isUser() {
        return !hasAuthority("device");
    }

    public boolean isDevice() {
        return hasAuthority("device");
    }

    public boolean isHwEngineer() {
        return hasAuthority("hw");
    }

    public boolean isAdmin() {
        return hasAuthority("admin");
    }

    private Boolean isWorkspaceOwner(Workspace workspace) {
        return this.getUsername().equals(workspace.getOwner());
    }

    private Boolean isWorkspaceParticipant(Workspace workspace) {
        return workspace.getInvitedUsers() != null
                && workspace.getInvitedUsers().stream().anyMatch(userName -> this.getUsername().equals(userName));
    }
}
