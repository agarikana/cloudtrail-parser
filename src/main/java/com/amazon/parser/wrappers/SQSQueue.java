package com.amazon.parser.wrappers;

import com.amazon.parser.factories.ClientFactory;
import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

/**
 * SQS Queue class
 */
public class SQSQueue {

    private final SqsClient client;
    @Getter
    private final String queueUrl;

    public SQSQueue(ClientFactory clientFactory, String region, String queueUrl) {
        this.client = (SqsClient) clientFactory.getClient(Region.of(region), SqsClient.builder());
        this.queueUrl = queueUrl;
    }

    public ReceiveMessageResponse getMessages(int maxMessagesCount,
                                              int longPollTime,
                                              int visibilityTimeout
                       ) {
        ReceiveMessageRequest req = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(maxMessagesCount)
                .visibilityTimeout(visibilityTimeout)
                .waitTimeSeconds(longPollTime)
                .build();

        return this.client.receiveMessage(req);
    }

    public void deleteMessage(String receiptHandle) {
        DeleteMessageRequest req = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(receiptHandle)
                .build();
        this.client.deleteMessage(req);
    }

}
