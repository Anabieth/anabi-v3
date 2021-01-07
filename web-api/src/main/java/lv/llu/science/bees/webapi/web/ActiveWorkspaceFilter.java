package lv.llu.science.bees.webapi.web;

import lv.llu.science.bees.webapi.domain.workspaces.ActiveWorkspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class ActiveWorkspaceFilter implements Filter {
    private static final String WORKSPACE_HEADER = "ws";

    private final ActiveWorkspace activeWorkspace;

    @Autowired
    public ActiveWorkspaceFilter(ActiveWorkspace activeWorkspace) {
        this.activeWorkspace = activeWorkspace;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String id = ((HttpServletRequest) request).getHeader(WORKSPACE_HEADER);
        if (id != null) {
            activeWorkspace.setId(id);
        }
        chain.doFilter(request, response);
    }
}
