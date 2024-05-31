package com.amazon.parser.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudTrailEvent {

    @JsonProperty("userIdentity")
    private UserIdentity userIdentity;
    @JsonProperty("eventTime")
    private String eventTime;
    @JsonProperty("eventSource")
    private String eventSource;
    @JsonProperty("awsRegion")
    private String awsRegion;
    @JsonProperty("eventName")
    private String eventName;
    @JsonProperty("eventType")
    private String eventType;
    @JsonProperty("readOnly")
    private boolean readOnly;
    @JsonProperty("managementEvent")
    private String managementEvent;

    public boolean isCreatedWithSession(String sessionName) {
        String principalId = this.getUserIdentity().getPrincipalId();
        return principalId != null && principalId.endsWith(sessionName);
    }
    public boolean isEventSource(String eventSource) {
        return this.getEventSource().equals(eventSource);
    }
}
