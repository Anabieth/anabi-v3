package lv.llu.science.bees.webapi.domain.tokens;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TokenRequestBean {
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("client_secret")
    private String clientSecret;
    private String audience;
    @JsonProperty("grant_type")
    private String grantType;

    public String getKey() {
        return clientId + clientSecret;
    }
}
