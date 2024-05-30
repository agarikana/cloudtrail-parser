package org.example.data;

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
}
