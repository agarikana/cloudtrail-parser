package org.example;

import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import org.example.wrappers.SQSQueue;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

/**
 * Generic SQS Queue Consumer
 * SQS recommends using 1 thread per Queue to consume messages
 */
public class QueueConsumer implements Runnable {

    private final SQSQueue queue;
    private final int queueLongPollTime;
    private final ConcurrentHashMap<String, String> messages;
    // All times are in seconds
    private static final int MAX_MESSAGE_TO_PROCESS = 10;
    private static final int VISIBILITY_TIMEOUT = 5;

    private static final Logger LOGGER = Logger.getLogger(QueueConsumer.class);

    public QueueConsumer(SQSQueue queue,
                         int queueLongPollTime) {
        assert queueLongPollTime <= 20; // this is enforced by the SQS queue

        this.queue = queue;
        this.queueLongPollTime = queueLongPollTime;
        this.messages = new ConcurrentHashMap<>();
    }

//    @Override
//    public Set<String> call() throws Exception {
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//        while (true) {
//            ReceiveMessageResponse response = queue.getMessages(MAX_MESSAGE_TO_PROCESS,
//                    queueLongPollTime,
//                    VISIBILITY_TIMEOUT);
//
//            if(response.hasMessages()) {
//                List<Message> messages = response.messages();
//                messages.forEach(this::processMessage);
//            }
//            if(hasTotalTimeElapsed(stopWatch)) {
//                LOGGER.info("Total time has elapsed to process messages");
//                break;
//            }
//        }
//        stopWatch.stop();
//        return returnValues;
//    }
    @Override
    public void run() {
        ReceiveMessageResponse response = queue.getMessages(MAX_MESSAGE_TO_PROCESS,
        queueLongPollTime,
        VISIBILITY_TIMEOUT);

        if(response.hasMessages()) {
            List<Message> sqsNotificationMessages = response.messages();
            LOGGER.info("Found " + sqsNotificationMessages.size() + " messages");
            sqsNotificationMessages.forEach(this::processMessage);
        }
    }

    private void processMessage(Message message) {
        messages.put(message.messageId(), message.body());
        queue.deleteMessage(message.receiptHandle());
    }

    public Map<String, String> getMessages() { return messages; }
}
