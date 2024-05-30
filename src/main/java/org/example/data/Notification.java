package org.example.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Notification {

    public enum NotificationType{
        BUCKETLOGSREADY,
        OTHER
    }
    @JsonProperty("MessageId")
    private String messageId;
    @JsonProperty("Type")
    private String type;
    @JsonProperty("Message")
    private String message;

    public NotificationType getNotificationType() {
        if(getMessage().contains("s3Bucket")){
            return NotificationType.BUCKETLOGSREADY;
        }else{
            return NotificationType.OTHER;
        }
    }

}
