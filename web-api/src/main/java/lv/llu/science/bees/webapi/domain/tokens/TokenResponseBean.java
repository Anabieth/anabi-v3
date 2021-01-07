package lv.llu.science.bees.webapi.domain.tokens;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class TokenResponseBean {
    @JsonIgnore
    private ZonedDateTime lastRequest;
    @JsonIgnore
    private ZonedDateTime expiresAt;

    @JsonProperty("access_token")
    private String token;
    private String scope;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("expires_in")
    private Long expiresIn;
}
