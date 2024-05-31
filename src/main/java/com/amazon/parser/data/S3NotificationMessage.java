package com.amazon.parser.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class S3NotificationMessage {
    @JsonProperty("s3Bucket")
    private String bucket;
    @JsonProperty("s3ObjectKey")
    private List<String> objects;
}
