package com.amazon.parser.processors;

import com.amazon.parser.QueueConsumer;
import com.amazon.parser.factories.ClientFactory;
import com.amazon.parser.wrappers.SQSQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QueueProcessorTests {
    SQSQueue sqsQueue;
    QueueConsumer queueConsumer;
    QueueProcessor queueProcessor;

    @Mock
    ClientFactory clientFactory;
    @Mock
    SqsClient sqsClient;

    String QUEUE_URL = "https://dummy-sqs-queue.com";
    int LONG_POLLING_TIME = 5; //seconds
    Duration TOTAL_QUEUE_PROCESSING_DURATION = Duration.ofMinutes(1);
    Duration QUEUE_POLLING_INTERVAL = Duration.ofMinutes(1);

    @BeforeEach
    public void setUp() {
        clientFactory = mock(ClientFactory.class);
        sqsClient = mock(SqsClient.class);
        when(clientFactory.getClient(any(Region.class), any(SqsClientBuilder.class))).thenReturn(sqsClient);
    }

    @Test
    public void testProcess() {
        sqsQueue = new SQSQueue(clientFactory, "us-east-1", QUEUE_URL);
        queueConsumer = new QueueConsumer(sqsQueue, LONG_POLLING_TIME);
        queueProcessor = new QueueProcessor(1, QUEUE_POLLING_INTERVAL);
        List<Message> messages = new ArrayList<>();
        Message message1 = Message.builder()
                .receiptHandle("1")
                .body("message body for 1")
                .messageId("1")
                .build();
        Message message2 = Message.builder()
                .receiptHandle("2")
                .body("message body for 2")
                .messageId("2")
                .build();
        messages.add(message1); messages.add(message2);
        Map<String, String> outputMessages = new HashMap<>();
        messages.forEach( message -> outputMessages.put(message.messageId(), message.body()));
        ReceiveMessageResponse receiveMessageResponse = ReceiveMessageResponse.builder()
                        .messages(messages)
                                .build();
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(receiveMessageResponse);
        Assertions.assertEquals(outputMessages, queueProcessor.process(queueConsumer, TOTAL_QUEUE_PROCESSING_DURATION));
    }
}
