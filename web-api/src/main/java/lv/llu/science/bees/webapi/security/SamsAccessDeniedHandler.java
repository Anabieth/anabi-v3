package lv.llu.science.bees.webapi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class SamsAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper mapper;
    private final BearerTokenAccessDeniedHandler defaultHandler;

    @Autowired
    public SamsAccessDeniedHandler(ObjectMapper mapper) {
        this.mapper = mapper;
        this.defaultHandler = new BearerTokenAccessDeniedHandler();
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        defaultHandler.handle(request, response, accessDeniedException);

        Map<String, Object> map = new HashMap<>();
        int status = response.getStatus();
        map.put("status", status);
        map.put("error", Optional.ofNullable(HttpStatus.resolve(status))
                .map(HttpStatus::getReasonPhrase)
                .orElse("Status " + status));
        map.put("message", accessDeniedException.getMessage());
        map.put("path", request.getServletPath());
        response.setContentType("application/json");
        mapper.writeValue(response.getOutputStream(), map);
    }
}
