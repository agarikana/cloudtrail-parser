package com.amazon.parser.wrappers;

import com.amazon.parser.factories.ClientFactory;
import lombok.NonNull;
import org.apache.log4j.Logger;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudtrail.CloudTrailClient;
import software.amazon.awssdk.services.cloudtrail.model.*;

import java.lang.UnsupportedOperationException;

/**
 * A CloudTrailHandler class
 */
public class CloudTrailHandler {

    private final CloudTrailClient client;

    private static final Logger LOGGER = Logger.getLogger(CloudTrailHandler.class);

    public CloudTrailHandler(ClientFactory clientFactory, String region) {
        client = (CloudTrailClient) clientFactory.getClient(Region.of(region), CloudTrailClient.builder());
    }

    public void create(@NonNull String trailName) {
        throw new UnsupportedOperationException("Cloud trail trail should be setup through cfn stack");
    }

    public void delete(@NonNull String trailName) {
        this.client.deleteTrail(DeleteTrailRequest.builder()
                .name(trailName)
                .build());
        LOGGER.info(String.format("Cloud trail trail: %s deleted", trailName));
    }

    public void start(@NonNull String trailName) {
        this.client.startLogging(StartLoggingRequest.builder()
                .name(trailName)
                .build());
        LOGGER.info(String.format("Cloud trail trail: %s started", trailName));
    }

    public void stop(@NonNull String trailName) {
        this.client.stopLogging(StopLoggingRequest.builder()
                .name(trailName)
                .build());
        LOGGER.info(String.format("Cloud trail trail: %s stopped", trailName));
    }

}
