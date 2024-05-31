package com.amazon.parser.wrappers;

import org.apache.log4j.Logger;
import software.amazon.awssdk.services.cloudtrail.CloudTrailClient;
import software.amazon.awssdk.services.cloudtrail.model.*;

import java.lang.UnsupportedOperationException;

/**
 * A CloudTrail class
 */
public class CloudTrail {

    private final String name;
    private final CloudTrailClient client;
    private final String destinationBucketName;
    private final String logPathPrefix;

    private static final Logger LOG = Logger.getLogger(CloudTrail.class);

    private CloudTrail(Builder builder) {
        this.name = builder.name;
        this.client = builder.client;
        this.destinationBucketName = builder.destinationBucket;
        this.logPathPrefix = builder.logPathPrefix;
    }
    public static class Builder {

        private final String name;
        private final CloudTrailClient client;
        private String destinationBucket;
        private String logPathPrefix;

        public Builder(String name, CloudTrailClient client) {
            this.name = name;
            this.client = client;
        }

        public Builder destinationBucket(String destinationBucket) {
            this.destinationBucket = destinationBucket;
            return this;
        }

        public Builder logPathPrefix(String logPathPrefix) {
            this.logPathPrefix = logPathPrefix;
            return this;
        }

        public CloudTrail build() {
            return new CloudTrail(this);
        }
    }
    public static Builder builder(String name, CloudTrailClient client) {
        return new Builder(name, client);
    }

    public void create() {
        throw new UnsupportedOperationException("Cloud trail trail should be setup through cfn stack");
    }

    public String getName() {
        return this.name;
    }

    public String getLogPathPrefix() {
        return this.logPathPrefix;
    }

    public String getDestinationBucketName() {
        return destinationBucketName;
    }

    public void delete() {
        this.client.deleteTrail(DeleteTrailRequest.builder()
                .name(this.name)
                .build());
    }

    public void start() {
        this.client.startLogging(StartLoggingRequest.builder()
                .name(this.name)
                .build());
    }

    public void stop() {
        this.client.stopLogging(StopLoggingRequest.builder()
                .name(this.name)
                .build());
    }

}
