package lv.llu.science.bees.webapi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class SamsAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;
    private final BearerTokenAuthenticationEntryPoint defaultEntryPoint;

    @Autowired
    public SamsAuthenticationEntryPoint(ObjectMapper mapper) {
        this.mapper = mapper;
        this.defaultEntryPoint = new BearerTokenAuthenticationEntryPoint();
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        defaultEntryPoint.commence(request, response, authException);

        Map<String, Object> map = new HashMap<>();
        int status = response.getStatus();
        map.put("status", status);
        map.put("error", Optional.ofNullable(HttpStatus.resolve(status))
                .map(HttpStatus::getReasonPhrase)
                .orElse("Status " + status));
        map.put("message", authException.getMessage());
        map.put("path", request.getServletPath());
        response.setContentType("application/json");
        mapper.writeValue(response.getOutputStream(), map);
    }
}
