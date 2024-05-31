package com.amazon.parser.processors;

import com.amazon.parser.QueueConsumer;
import org.apache.log4j.Logger;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class QueueProcessor {
    private Integer noOfConsumers = 1;
    private final Duration pollingInterval;
    private static final Logger LOGGER = Logger.getLogger(QueueProcessor.class);
    public QueueProcessor(Integer noOfConsumers, Duration pollingInterval) {
        this.noOfConsumers = noOfConsumers;
        this.pollingInterval = pollingInterval;
    }
    public Map<String, String> process(final QueueConsumer queueConsumer, final Duration totalProcessingTime) {
        try {
            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(noOfConsumers + 1);
            ScheduledFuture<?> futureTask = executorService.scheduleAtFixedRate(queueConsumer, 0,
                    pollingInterval.toMinutes(), TimeUnit.MINUTES);
            LOGGER.info(String.format("Poller for queue %s started. Waiting for %d minutes to finish",
                    queueConsumer.getQueueUrl(), totalProcessingTime.toMinutes()));
            executorService.schedule(() -> futureTask.cancel(true),
                    totalProcessingTime.toMinutes(), TimeUnit.MINUTES);

            try {
                executorService.awaitTermination(totalProcessingTime.toMinutes()+2, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                LOGGER.info("SQS poller termination is interrupted");
            }
            if(!executorService.isShutdown()) {
                LOGGER.info("Forcing SQS poller to shutdown");
                executorService.shutdownNow();
            }
            LOGGER.info("SQS poller is shutdown");

        } catch (Exception e) {
            LOGGER.error("SQS poller encountered error", e);
        }
        return queueConsumer.getMessages();
    }

}
