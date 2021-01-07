package lv.llu.science.bees.webapi.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.proc.JWSAlgorithmFamilyJWSKeySelector;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.SingleKeyJWSKeySelector;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.util.List;

public class SamsJWSKeySelector implements JWTClaimsSetAwareJWSKeySelector<SecurityContext> {

    private final String remoteIssuer;
    private final String localIssuer;

    private final JWSKeySelector<SecurityContext> localSelector;
    private final JWSKeySelector<SecurityContext> remoteSelector;

    public SamsJWSKeySelector(
            String remoteIssuer,
            String localIssuer,
            Key localKey
    ) throws MalformedURLException, KeySourceException {
        this.remoteIssuer = remoteIssuer;
        this.localIssuer = localIssuer;
        localSelector = new SingleKeyJWSKeySelector<>(JWSAlgorithm.HS512, localKey);
        remoteSelector = JWSAlgorithmFamilyJWSKeySelector.fromJWKSetURL(new URL(remoteIssuer + ".well-known/jwks.json"));
    }

    @Override
    public List<? extends Key> selectKeys(JWSHeader header, JWTClaimsSet claimsSet, SecurityContext context) throws KeySourceException {
        if (localIssuer.equals(claimsSet.getIssuer())) {
            return localSelector.selectJWSKeys(header, context);
        } else if (remoteIssuer.equals(claimsSet.getIssuer())) {
            return remoteSelector.selectJWSKeys(header, context);
        } else {
            throw new KeySourceException("Invalid token issuer");
        }
    }
}
