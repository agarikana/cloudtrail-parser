package org.example;

import org.apache.log4j.Logger;
import org.example.data.CloudTrailEvent;
import org.example.data.Input;
import org.example.factories.ClientFactory;
import org.example.processors.NotificationProcessor;
import org.example.wrappers.CloudTrail;
import org.example.wrappers.SQSQueue;
import org.example.wrappers.S3Handler;
import org.example.wrappers.StackHandler;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudtrail.CloudTrailClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * to generate all the permissions required for
 * a legacy handler
 */
public class HandlerPermissionGetter {

    public static String REGION = "us-west-2";
    public static String HANDLER_TEMPLATE_FILE_NAME = "Bucket.json";
    public static String TRAIL_LOGS_BUCKET = String.format("cloudtrail-%s-handler-logs",REGION);
    public static String TRAIL_NAME = "handler-api-trail-%s-%s";
    public static String SETUP_DEPENDENCIES_TEMPLATE_FILE_NAME = "TrailDependencies.yaml";
    public static String SETUP_TEMPLATE_FILE_NAME = "Trail.yaml";
    public static String TRAIL_DEPENDENCIES_STACK_NAME = String.format("ct-setup-%s-dependencies-stack", REGION);
    public static String TRAIL_STACK_NAME = String.format("ct-setup-%s-stack", REGION);
    public static String HANDLER_STACK_NAME = String.format("ct-create-handler-%s-stack", REGION);
    public static String SETUP_STACK_OUTPUTS_HANDLER_INVOCATION_ROLE = "HandlerInvocationRole";
    public static String SETUP_STACK_OUTPUTS_TRAIL_NOTIFICATION_QUEUE = "CloudTrailNotificationMsgQueue";
    public static final Logger LOGGER = Logger.getLogger(HandlerPermissionGetter.class);


    public static void main(String[] args) {

        // init
        HandlerPermissionGetter handlerPermissionGetter = new HandlerPermissionGetter();
        ClientFactory clientFactory = new ClientFactory();
        S3Handler s3Handler = new S3Handler((S3Client) clientFactory.getClient(Region.of(REGION), S3Client.builder()));
        StackHandler setUpStackHandler = new StackHandler(REGION, null);

        List<String> stackOutputNames = List.of(
                SETUP_STACK_OUTPUTS_HANDLER_INVOCATION_ROLE,
                SETUP_STACK_OUTPUTS_TRAIL_NOTIFICATION_QUEUE);

        try {
            // 1. set up the infra for cloud trail trail
            final Map<String, String> setUpStackOutputs = handlerPermissionGetter.setUpInfraStructure(
                    setUpStackHandler,
                    s3Handler,
                    stackOutputNames);
            LOGGER.info("Trail InfraStructure Stack created");

            try(CloudTrailClient cloudTrailClient = CloudTrailClient.builder()
                    .httpClient(ApacheHttpClient.builder().build())
                    .region(Region.of(REGION))
                    .build();){
                CloudTrail handlerInvocationTrail = CloudTrail.builder(
                        TRAIL_NAME, cloudTrailClient)
                        .build();

                //invoke stack operations
                String handlerStackTemplate = handlerPermissionGetter.loadResourceFileOnClassPath(HANDLER_TEMPLATE_FILE_NAME);

                // start trail
                System.out.println("Waiting for 5 mins before invoking handlers");
                Thread.sleep(360*1000);
                StackHandler resourceCreateStackHandler = new StackHandler(
                        REGION,
                        null,
                        setUpStackOutputs.get(SETUP_STACK_OUTPUTS_HANDLER_INVOCATION_ROLE),
                        "create-handler-permissions"
                );
                resourceCreateStackHandler.createStack(
                        HANDLER_STACK_NAME,
                        handlerStackTemplate,
                        null
                );


                // poll for ct notification messages
                SQSQueue sqsQueue = new SQSQueue(
                        SqsClient.builder()
                                .httpClient(ApacheHttpClient.builder().build())
                                .region(Region.of(REGION))
                                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                                .build(),
                        setUpStackOutputs.get(SETUP_STACK_OUTPUTS_TRAIL_NOTIFICATION_QUEUE));
                QueueConsumer sqsQueueConsumer = new QueueConsumer(
                        sqsQueue, 20);
                Map<String, String> notificationMessages = SQSQueuePoller(sqsQueueConsumer);

                // process ct notification messages
                notificationMessages.forEach(
                        (k, v) -> System.out.println("message id: "+ k + ", message : " + v));
                NotificationProcessor notificationProcessor = new NotificationProcessor(notificationMessages.values().stream().toList(), REGION, clientFactory);
                List<CloudTrailEvent> cloudTrailEvents = new ArrayList<>();
                notificationProcessor.getEventSources().forEach(eventSource -> cloudTrailEvents.addAll(eventSource.readEvents()));
                cloudTrailEvents.forEach( event -> System.out.println(event.getEventName()));
                handlerInvocationTrail.stop();

                // display results
            } catch(Exception e) {
                e.printStackTrace();

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            handlerPermissionGetter.tearDownInfraStructure(setUpStackHandler, s3Handler);
        }

    }

    public static List<Input> getInputs(String pathToInputs) throws IOException {
        try(Stream<Path> dirStream = Files.list(Paths.get(pathToInputs))){
            List<Input> stackInputs = new ArrayList<>();
            dirStream.forEach(
                    file -> {
                        if(Files.isDirectory(file) && file.getFileName().toString().matches("input[0-9]*")){
                            String dirName = file.getFileName().toString();
                            Input stackInput = new Input();
                            try(Stream<Path> subDirStream = Files.list(file)) {
                                subDirStream.forEach(
                                      nestedFile -> {
                                          String nestedFileName = nestedFile.getFileName().toString();
                                          try {
                                              String createTemplate;
                                              String updateTemplate;
                                              if (nestedFileName.matches("^create.(yaml|json)$")) {
                                                  createTemplate = Files.readString(nestedFile, StandardCharsets.UTF_8);
                                                  stackInput.setCreateTemplateBody(createTemplate);
                                              }
                                              if (nestedFileName.matches("^update.(yaml|json)$")) {
                                                  updateTemplate = Files.readString(nestedFile, StandardCharsets.UTF_8);
                                                  stackInput.setUpdateTemplateBody(updateTemplate);
                                              }
                                          } catch (IOException e) {
                                              LOGGER.error(String.format("error :%s reading file :%s", nestedFileName, e.getMessage()));
                                              throw new RuntimeException(e);
                                          }
                                      }
                                );
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            if(stackInput.getCreateTemplateBody() == null || stackInput.getUpdateTemplateBody() == null) {
                                throw new IllegalArgumentException("input directory should contain template files in the form create.yaml and update.yaml");
                            }
                            stackInputs.add(stackInput);
                            LOGGER.info(String.format("create and update templates read from input directory %s", dirName));
                        }
                    }
            );
            return stackInputs;
        }
    }

    private Map<String, String> setUpInfraStructure(final StackHandler setUpStackHandler,
                                     final S3Handler s3Handler,
                                     final List<String> stackOutputNames) throws IOException {
            String uuid = UUID.randomUUID().toString();
            Map<String, String> trailStackParameters = new HashMap<>();
            trailStackParameters.put("TrailName", String.format(TRAIL_NAME, REGION, uuid));
            trailStackParameters.put("ObjectPrefix", "HandlerInvocations");
            Map<String, String> trailDependenciesStackParameters = new HashMap<>(trailStackParameters);

            if(!s3Handler.doesBucketExist(TRAIL_LOGS_BUCKET)){
                trailDependenciesStackParameters.put("BucketName", TRAIL_LOGS_BUCKET);
            }
            setUpStackHandler.createStack(
                    TRAIL_DEPENDENCIES_STACK_NAME,
                    loadResourceFileOnClassPath(SETUP_DEPENDENCIES_TEMPLATE_FILE_NAME),
                    trailDependenciesStackParameters);
            setUpStackHandler.createStack(
                    TRAIL_STACK_NAME,
                    loadResourceFileOnClassPath(SETUP_TEMPLATE_FILE_NAME),
                    trailStackParameters);

            return setUpStackHandler.getStackOutputsByName(TRAIL_DEPENDENCIES_STACK_NAME,
                    stackOutputNames);
    }

    private String loadResourceFileOnClassPath(final String fileName) throws IOException {
        InputStream inputStream = HandlerPermissionGetter.class.getClassLoader().getResourceAsStream(fileName);
        if(inputStream == null) {throw new IOException("file %s not found on classpath");}
        try(InputStreamReader inputStreamReader = new InputStreamReader(
                inputStream,
                StandardCharsets.UTF_8);) {
            return new BufferedReader(inputStreamReader)
                    .lines()
                    .collect(Collectors.joining("\n"));
        }
    }

    private void tearDownInfraStructure(final StackHandler setUpStackHandler,
                                        final S3Handler s3Handler) {
        // empty the trail logs bucket
        s3Handler.emptyBucket(TRAIL_LOGS_BUCKET);
        // delete the trail & dependencies stacks
        if(!setUpStackHandler.isStackExists(TRAIL_STACK_NAME)) {
            LOGGER.error(String.format("Trail Stack %s does not exist", TRAIL_STACK_NAME));
            return;
        }
        setUpStackHandler.deleteStack(TRAIL_STACK_NAME);
        if(!setUpStackHandler.isStackExists(TRAIL_DEPENDENCIES_STACK_NAME)) {
            LOGGER.error(String.format("Trail Dependencies Stack %s does not exist", TRAIL_DEPENDENCIES_STACK_NAME));
            return;
        }
        setUpStackHandler.deleteStack(TRAIL_DEPENDENCIES_STACK_NAME);
    }

    public static void invokeHandler() {

    }

    public static Map<String, String> SQSQueuePoller(QueueConsumer queueConsumer){

        try {
            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
            ScheduledFuture<?> futureTask = executorService.scheduleAtFixedRate(queueConsumer, 0, 1, TimeUnit.MINUTES);
            // cloudtrail typically delivers logs within an average of about 5 mins of
            // an API call. This time is not guaranteed as per SLA of cloudtrail
            System.out.println("Poller started. Waiting for it to finish");
            executorService.schedule(() -> futureTask.cancel(true), 6, TimeUnit.MINUTES);

            try {
                executorService.awaitTermination(6+2, TimeUnit.MINUTES);
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
