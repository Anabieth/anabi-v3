package lv.llu.science.bees.webapi;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lv.llu.science.bees.webapi.security.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.net.MalformedURLException;
import java.security.Key;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Profile("!dev")
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Value(value = "${sams.token.audience}")
    private String audience;

    @Value("${sams.token.remote-issuer}")
    private String remoteIssuer;
    @Value("${sams.token.local-issuer}")
    private String localIssuer;

    @Autowired
    SamsAccessDeniedHandler accessDeniedHandler;

    @Autowired
    SamsAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    SecretProvider secretProvider;

    JwtDecoder jwtDecoder() throws MalformedURLException, KeySourceException {
        Key secretKey = new SecretKeySpec(secretProvider.getSecretBytes(), "HmacSHA512");
        SamsJWSKeySelector jwsKeySelector = new SamsJWSKeySelector(remoteIssuer, localIssuer, secretKey);

        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWTClaimsSetAwareJWSKeySelector(jwsKeySelector);

        NimbusJwtDecoder jwtDecoder = new NimbusJwtDecoder(jwtProcessor);

        OAuth2TokenValidator<Jwt> audienceValidator = new SamsJwtAudienceValidator(audience);
        OAuth2TokenValidator<Jwt> issuerValidator = new SamsJwtIssuerValidator(remoteIssuer, localIssuer);
        OAuth2TokenValidator<Jwt> combinedValidator = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(),
                audienceValidator,
                issuerValidator);
        jwtDecoder.setJwtValidator(combinedValidator);

        return jwtDecoder;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic().disable()
                .formLogin().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/token").permitAll()
                .antMatchers("/").permitAll()
                .anyRequest().authenticated();

        http.oauth2ResourceServer()
                .jwt()
                .decoder(jwtDecoder())
                .and()
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(authenticationEntryPoint);
    }
}
