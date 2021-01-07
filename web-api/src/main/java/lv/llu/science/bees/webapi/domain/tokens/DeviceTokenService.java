package lv.llu.science.bees.webapi.domain.tokens;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lv.llu.science.bees.webapi.security.SecretProvider;
import lv.llu.science.bees.webapi.utils.TimeMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.ZonedDateTime;

@Service
public class DeviceTokenService {
    @Value("${sams.token.expireIn}")
    protected long expireIn;

    @Value("${sams.token.local-issuer}")
    protected String localIssuer;

    @Value("${sams.token.audience}")
    protected String audience;

    private final SecretProvider secretProvider;
    private final DeviceClientRepository repository;
    private final TimeMachine timeMachine;

    @Autowired
    public DeviceTokenService(SecretProvider secretProvider, DeviceClientRepository repository, TimeMachine timeMachine) {
        this.secretProvider = secretProvider;
        this.repository = repository;
        this.timeMachine = timeMachine;
    }

    public TokenResponseBean getToken(TokenRequestBean request) throws JOSEException {
        if (!audience.equals(request.getAudience())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided audience is not supported");
        }

        if (!"client_credentials".equals(request.getGrantType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided grant type is not supported");
        }

        DeviceClient client = repository.findByIdAndSecret(request.getClientId(), request.getClientSecret())
                .orElseThrow(() -> new BadCredentialsException("Invalid client ID or secret"));

        JWSSigner signer = new MACSigner(secretProvider.getSecretBytes());
        ZonedDateTime now = timeMachine.zonedNow();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(client.getId() + "@clients")
                .issuer(localIssuer)
                .audience(audience)
                .issueTime(Date.from(now.toInstant()))
                .expirationTime(Date.from(now.plusSeconds(expireIn).toInstant()))
                .claim("scope", "device")
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.HS512).build(),
                claims);
        signedJWT.sign(signer);

        TokenResponseBean bean = new TokenResponseBean();
        bean.setTokenType("Bearer");
        bean.setToken(signedJWT.serialize());
        bean.setScope("device");
        bean.setExpiresIn(expireIn);
        return bean;
    }
}
