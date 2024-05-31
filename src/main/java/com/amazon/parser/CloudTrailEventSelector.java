package com.amazon.parser;

import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudtrail.CloudTrailClient;
import software.amazon.awssdk.services.cloudtrail.model.CloudTrailException;
import software.amazon.awssdk.services.cloudtrail.model.Event;
import software.amazon.awssdk.services.cloudtrail.model.LookupAttribute;
import software.amazon.awssdk.services.cloudtrail.model.LookupAttributeKey;
import software.amazon.awssdk.services.cloudtrail.model.LookupEventsRequest;


import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
public class CloudTrailEventSelector {

    public static final List<String> excludedEventNames = Collections.unmodifiableList(
            Arrays.asList(
                    "CreateStack",
                    "DescribeStacks",
                    "DeleteStack"
            ));

    private LookupEventsRequest.Builder lookupEventsBuilder;
    private final CloudTrailClient cloudTrailClient;
    public CloudTrailEventSelector(String region) {
        cloudTrailClient = CloudTrailClient.builder()
                .httpClient(ApacheHttpClient.builder().build())
                .region(Region.of(region))
                .build();
        lookupEventsBuilder = LookupEventsRequest.builder();
    }

    public CloudTrailEventSelector byUserName(String userName) {
        this.lookupEventsBuilder =  lookupEventsBuilder.lookupAttributes(
                LookupAttribute.builder()
                        .attributeKey(LookupAttributeKey.USERNAME)
                        .attributeValue(userName)
                        .build());
        return this;
    }

    public CloudTrailEventSelector byEventName(String eventName) {
        this.lookupEventsBuilder =  lookupEventsBuilder.lookupAttributes(
                LookupAttribute.builder()
                        .attributeKey(LookupAttributeKey.EVENT_NAME)
                        .attributeValue(eventName)
                        .build());
        return this;
    }

    public CloudTrailEventSelector byStartTime(Instant startTime) {
        this.lookupEventsBuilder =  lookupEventsBuilder.startTime(startTime);
        return this;
    }

    public CloudTrailEventSelector byEndTime(Instant endTime) {
        this.lookupEventsBuilder =  lookupEventsBuilder.endTime(endTime);
        return this;
    }

    public List<Event> select() {
        List<Event> events = new ArrayList<>();
        try {
            events = cloudTrailClient.lookupEvents(lookupEventsBuilder.build()).events();
        } catch (CloudTrailException e) {
            System.err.println("Error in CloudTrailEventSelector: " + e.getMessage());
        }
        return events;
    }

    public static List<String> getAllAPICalls(List<Event> allAPICalls) {
        return allAPICalls.stream()
                .map(Event::eventName
                )
                .filter(eventName -> !excludedEventNames.contains(eventName))
                .collect(Collectors.toList());
    }
}
