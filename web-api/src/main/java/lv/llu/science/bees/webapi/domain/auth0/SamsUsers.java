package lv.llu.science.bees.webapi.domain.auth0;

import feign.FeignException;
import lombok.extern.java.Log;
import lv.llu.science.bees.webapi.domain.tokens.TokenRequestBean;
import lv.llu.science.bees.webapi.domain.tokens.TokenResponseBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Log
public class SamsUsers {
    TokenResponseBean token;

    private final Auth0Client auth0;

    @Value("${AUTH0_CLIENT}")
    private String clientId;
    @Value("${AUTH0_SECRET}")
    private String clientSecret;

    @Autowired
    public SamsUsers(Auth0Client auth0) {
        this.auth0 = auth0;
    }

    private void renewTokenFeign() {
        TokenRequestBean requestBean = new TokenRequestBean();
        requestBean.setClientId(clientId);
        requestBean.setClientSecret(clientSecret);
        requestBean.setAudience("https://dev-42331gx2.us.auth0.com/api/v2/");
        requestBean.setGrantType("client_credentials");

        try {
            token = auth0.getToken(requestBean);
            token.setExpiresAt(ZonedDateTime.now().plusSeconds(token.getExpiresIn()));
            log.info("Renewed Auth0 token");
        } catch (FeignException ex) {
            log.severe("Error renewing Auth0 token: " + ex.getMessage());
        }
    }

    @Cacheable("samsUsers")
    public Map<String, String> getUserMap() {
        if (token == null || !token.getExpiresAt().isAfter(ZonedDateTime.now())) {
            renewTokenFeign();
        }

        Map<String, String> users = new HashMap<>();

        try {
            List<Map<String, String>> list = auth0.getUsers(token.getTokenType() + " " + token.getToken());
            list.forEach(entry -> users.put(entry.get("user_id"), entry.get("name")));
            log.info("Fetched Auth0 users");
        } catch (FeignException ex) {
            log.severe("Error fetching Auth0 users: " + ex.getMessage());
        }

        return users;
    }
}
