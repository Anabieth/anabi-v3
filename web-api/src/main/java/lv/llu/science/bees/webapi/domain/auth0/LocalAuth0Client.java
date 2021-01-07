package lv.llu.science.bees.webapi.domain.auth0;

import lv.llu.science.bees.webapi.domain.tokens.TokenRequestBean;
import lv.llu.science.bees.webapi.domain.tokens.TokenResponseBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component()
@Profile("!prod")
public class LocalAuth0Client implements Auth0Client {

    public TokenResponseBean getToken(TokenRequestBean bean) {
        TokenResponseBean token = new TokenResponseBean();
        token.setExpiresIn(10L * 60);
        token.setTokenType("Bearer");
        token.setToken("dev");
        return token;
    }

    public List<Map<String, String>> getUsers(String token) {
        return new ArrayList<>();
    }

}
