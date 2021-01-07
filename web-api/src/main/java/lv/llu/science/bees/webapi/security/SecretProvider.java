package lv.llu.science.bees.webapi.security;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SecretProvider {
    private final byte[] bytes;

    public SecretProvider() {
        // generate new secret on every start
        SecureRandom random = new SecureRandom();
        this.bytes = new byte[64];
        random.nextBytes(bytes);
    }

    public byte[] getSecretBytes() {
        return bytes;
    }
}
