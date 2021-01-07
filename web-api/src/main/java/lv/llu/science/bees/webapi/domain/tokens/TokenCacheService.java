package lv.llu.science.bees.webapi.domain.tokens;

import feign.FeignException;
import lv.llu.science.bees.webapi.domain.auth0.Auth0Client;
import lv.llu.science.bees.webapi.utils.TimeMachine;
import lv.llu.science.bees.webapi.utils.TooManyRequestsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.text.MessageFormat.format;
import static java.time.temporal.ChronoUnit.SECONDS;

@Service
public class TokenCacheService {

    private final TimeMachine timeMachine;
    private final Auth0Client auth0Client;
    private final Map<String, TokenResponseBean> cache = new HashMap<>();

    @Autowired
    public TokenCacheService(TimeMachine timeMachine, Auth0Client auth0Client) {
        this.timeMachine = timeMachine;
        this.auth0Client = auth0Client;
    }

    public TokenResponseBean getToken(TokenRequestBean request) {
        TokenResponseBean entry = cache.get(request.getKey());

        if (entry == null) {
            return renewToken(request);
        }

        ZonedDateTime now = timeMachine.zonedNow();
        int timeout = 30;
        if (now.minusSeconds(timeout).isBefore(entry.getLastRequest())) {
            throw new TooManyRequestsException(format("Token can be requested only once in {0} seconds.", timeout));
        }

        if (entry.getToken() == null) {
            return renewToken(request);
        }

        if (entry.getExpiresAt().isBefore(now)) {
            return renewToken(request);
        }

        entry.setExpiresIn(SECONDS.between(now, entry.getExpiresAt()));
        entry.setLastRequest(now);
        return entry;
    }

    private TokenResponseBean renewToken(TokenRequestBean request) {
        ZonedDateTime now = timeMachine.zonedNow();
        try {
            TokenResponseBean resp = auth0Client.getToken(request);
            resp.setExpiresAt(now.plusSeconds(resp.getExpiresIn()));
            resp.setLastRequest(now);
            cache.put(request.getKey(), resp);
            return resp;
        } catch (FeignException ex) {
            TokenResponseBean failed = new TokenResponseBean();
            failed.setLastRequest(now);
            cache.put(request.getKey(), failed);
            if (ex.status() == 401) {
                throw new BadCredentialsException(ex.getMessage(), ex);
            }
            throw ex;
        }
    }
}
