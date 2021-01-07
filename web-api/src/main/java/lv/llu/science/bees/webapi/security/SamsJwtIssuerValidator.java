package lv.llu.science.bees.webapi.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;

import java.util.ArrayList;

public class SamsJwtIssuerValidator implements OAuth2TokenValidator<Jwt> {

    private final JwtIssuerValidator remoteValidator;
    private final JwtIssuerValidator localValidator;

    public SamsJwtIssuerValidator(String remoteIssuer, String localIssuer) {
        this.remoteValidator = new JwtIssuerValidator(remoteIssuer);
        this.localValidator = new JwtIssuerValidator(localIssuer);
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        OAuth2TokenValidatorResult remoteResult = remoteValidator.validate(token);
        OAuth2TokenValidatorResult localResult = localValidator.validate(token);
        if (remoteResult.hasErrors() && localResult.hasErrors()) {
            ArrayList<OAuth2Error> errors = new ArrayList<>();
            errors.addAll(remoteResult.getErrors());
            errors.addAll(localResult.getErrors());
            return OAuth2TokenValidatorResult.failure(errors);
        }

        return OAuth2TokenValidatorResult.success();
    }
}
