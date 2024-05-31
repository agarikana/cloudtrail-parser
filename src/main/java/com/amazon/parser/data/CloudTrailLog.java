package com.amazon.parser.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CloudTrailLog {
    @JsonProperty("Records")
    private List<CloudTrailEvent> cloudTrailsEvents;
}
