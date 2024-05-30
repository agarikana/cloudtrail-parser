package org.example.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UserIdentity {
    @JsonProperty("type")
    private String type;
    @JsonProperty("principalId")
    private String principalId;
    @JsonProperty("arn")
    private String arn;
    @JsonProperty("account")
    private String account;
    @JsonProperty("sessionContext")
    private SessionContext sessionContext;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class SessionContext {
        @JsonProperty("sessionIssuer")
        private SessionIssuer sessionIssuer;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class SessionIssuer {
            @JsonProperty("type")
            private String type;
            @JsonProperty("principalId")
            private String principalId;
            @JsonProperty("arn")
            private String arn;
            @JsonProperty("accountId")
            private String accountId;
            @JsonProperty("userName")
            private String userName;
        }
    }
}
