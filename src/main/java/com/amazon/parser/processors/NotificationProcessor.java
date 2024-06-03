package com.amazon.parser.processors;

import com.amazon.parser.data.CloudTrailLog;
import com.amazon.parser.data.Notification;
import com.amazon.parser.deserializers.DeSerializer;
import com.amazon.parser.factories.ClientFactory;
import com.amazon.parser.EventSource;
import lombok.Data;
import org.apache.log4j.Logger;
import com.amazon.parser.data.CloudTrailEvent;
import com.amazon.parser.data.S3NotificationMessage;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.zip.GZIPInputStream;

@Data
public class NotificationProcessor {
    private final List<String> notifications;
    private final String region;
    public static final DeSerializer<CloudTrailLog> cloudTrailLogDeSerializer = new DeSerializer<>(){};
    public static final DeSerializer<CloudTrailEvent> cloudTrailEventsDeSerializer = new DeSerializer<>(){};
    public static final DeSerializer<Notification> snsNotificationDeSerializer = new DeSerializer<>(){};
    public static final DeSerializer<S3NotificationMessage> s3NotificationMessageDeSerializer = new DeSerializer<>(){};
    private final ClientFactory clientFactory;
    public static final Logger LOGGER = Logger.getLogger(NotificationProcessor.class);

    public List<EventSource> getEventSources(){
        List<EventSource> eventSources = new ArrayList<>();
        for(String notification : notifications){
            Notification notificationObj = snsNotificationDeSerializer.deserializeToObject(notification, Notification.class);
            String notificationMessageBody = notificationObj.getMessage();
            switch(notificationObj.getNotificationType()){
                case BUCKETLOGSREADY:
                    eventSources.add(() -> {
                        List<CloudTrailEvent> cloudTrailEvents = new ArrayList<>();
                        try {
                            S3Client client = (S3Client) clientFactory.getClient(Region.of(region), S3Client.builder());
                            S3NotificationMessage s3NotificationMessage = s3NotificationMessageDeSerializer.deserializeToObject(
                                    notificationMessageBody,
                                    S3NotificationMessage.class
                            );
                            String bucket = s3NotificationMessage.getBucket();
                            List<String> logFiles = s3NotificationMessage.getObjects();

                            for(String logFile : logFiles){
                                ResponseInputStream<GetObjectResponse> objectInputStream = client.getObject(GetObjectRequest.builder()
                                                .bucket(bucket)
                                                .key(logFile)
                                        .build());
                                try(InputStreamReader streamReader = new InputStreamReader(new GZIPInputStream(objectInputStream));
                                    BufferedReader bufferedReader = new BufferedReader(streamReader)) {
                                    String line;
                                    StringJoiner cloudTrailEventsJson = new StringJoiner("\n");
                                    while ((line = bufferedReader.readLine()) != null) {
                                        cloudTrailEventsJson.add(line);
                                    }
                                    cloudTrailEvents.addAll(cloudTrailLogDeSerializer
                                            .deserializeToObject(cloudTrailEventsJson.toString(), CloudTrailLog.class)
                                            .getCloudTrailsEvents()
                                    );

                                } catch (IOException e) {
                                    LOGGER.error(e);
                                }
                            }
                        } catch (AwsServiceException e) {
                            LOGGER.error(e);
                        }
                        return cloudTrailEvents;
                    });
                    break;
                case OTHER:
                    LOGGER.info("Notification contains an unknown events logs source. ");
                    break;
            }

        }
        return eventSources;
    }

}
