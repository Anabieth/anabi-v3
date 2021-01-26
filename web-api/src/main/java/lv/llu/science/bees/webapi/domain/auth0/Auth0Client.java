package lv.llu.science.bees.webapi.domain.auth0;

import feign.Headers;
import lv.llu.science.bees.webapi.domain.tokens.TokenRequestBean;
import lv.llu.science.bees.webapi.domain.tokens.TokenResponseBean;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

@Component
@Profile("prod")
@FeignClient(name = "Auth0", url = "https://dev-42331gx2.us.auth0.com/")
public interface Auth0Client {

    @Headers("Accept: application/json")
    @PostMapping("/oauth/token")
    TokenResponseBean getToken(@RequestBody TokenRequestBean bean);

    @Headers("Accept: application/json")
    @GetMapping("/api/v2/users?fields=user_id%2Cname")
    List<Map<String, String>> getUsers(@RequestHeader("Authorization") String token);

}
