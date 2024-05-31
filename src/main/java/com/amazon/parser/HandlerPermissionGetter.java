package com.amazon.parser;

import com.amazon.parser.wrappers.CloudTrail;
import com.amazon.parser.wrappers.S3Handler;
import com.amazon.parser.wrappers.SQSQueue;
import com.amazon.parser.wrappers.StackHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import com.amazon.parser.data.CloudTrailEvent;
import com.amazon.parser.data.Input;
import com.amazon.parser.factories.ClientFactory;
import com.amazon.parser.processors.NotificationProcessor;
import com.amazon.parser.processors.QueueProcessor;
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
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main Class
 * Generates all the permissions required for
 * a legacy handler
 */
public class HandlerPermissionGetter {

    public static String CLOUDTRAIL_LOGS_PREFIX = "HandlerInvocations";
    public static String CREATE_HANDLER_SESSION = "create-handler-session";
    public static String UPDATE_HANDLER_SESSION = "update-handler-session";
    public static String DELETE_HANDLER_SESSION = "delete-handler-session";
    public static String REGION = "us-west-2";
    public static String PATH_TO_INPUTS = "/Users/alexark/inputs";
    public static String TRAIL_LOGS_BUCKET = String.format("cloudtrail-%s-handler-logs",REGION);
    public static String TRAIL_NAME = String.format("handler-api-trail-%s-%s",REGION, UUID.randomUUID());
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
        List<Input> inputs = handlerPermissionGetter.getInputs(PATH_TO_INPUTS);
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

                //invoke legacy handlers
                StackHandler resourceCreateStackHandler = new StackHandler(
                        REGION,
                        null,
                        setUpStackOutputs.get(SETUP_STACK_OUTPUTS_HANDLER_INVOCATION_ROLE),
                        CREATE_HANDLER_SESSION
                );
                StackHandler resourceUpdateStackHandler = new StackHandler(
                        REGION,
                        null,
                        setUpStackOutputs.get(SETUP_STACK_OUTPUTS_HANDLER_INVOCATION_ROLE),
                        UPDATE_HANDLER_SESSION
                );
                StackHandler resourceDeleteStackHandler = new StackHandler(
                        REGION,
                        null,
                        setUpStackOutputs.get(SETUP_STACK_OUTPUTS_HANDLER_INVOCATION_ROLE),
                        DELETE_HANDLER_SESSION
                );


                // start trail
                LOGGER.info("Waiting for 6 minutes before invoking handlers");
                Thread.sleep(360*1000);
                handlerPermissionGetter.processInputs(
                        inputs,
                        resourceCreateStackHandler,
                        resourceUpdateStackHandler,
                        resourceDeleteStackHandler);


                // poll for cloudtrail notification messages
                SQSQueue sqsQueue = new SQSQueue(
                        SqsClient.builder()
                                .httpClient(ApacheHttpClient.builder().build())
                                .region(Region.of(REGION))
                                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                                .build(),
                        setUpStackOutputs.get(SETUP_STACK_OUTPUTS_TRAIL_NOTIFICATION_QUEUE));
                QueueConsumer sqsQueueConsumer = new QueueConsumer(
                        sqsQueue, 20);
                //Map<String, String> notificationMessages = SQSQueuePoller(sqsQueueConsumer);
                QueueProcessor queueProcessor = new QueueProcessor(1, Duration.ofMinutes(1));
                Map<String, String> notificationMessages = queueProcessor.process(sqsQueueConsumer, Duration.ofMinutes(6));

                // process cloudtrail notification messages
                notificationMessages.forEach(
                        (k, v) -> System.out.println("message id: "+ k + ", message : " + v));
                NotificationProcessor notificationProcessor = new NotificationProcessor(
                        notificationMessages.values().stream().toList(), REGION, clientFactory);
                List<CloudTrailEvent> cloudTrailEvents = new ArrayList<>();

                // get all cloudtrail events
                notificationProcessor.getEventSources().forEach(eventSource -> cloudTrailEvents.addAll(eventSource.readEvents()));

                // stop the trail
                handlerInvocationTrail.stop();

                // extract handlers' invocation events
                ObjectMapper objectMapper = new ObjectMapper();
                String handlerEventsJson = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(handlerPermissionGetter.extractHandlerEvents(cloudTrailEvents));
                LOGGER.info(handlerEventsJson);
            } catch(Exception e) {
                LOGGER.error(e.getMessage());
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } finally {
            handlerPermissionGetter.tearDownInfraStructure(setUpStackHandler, s3Handler);
        }
    }

    private Map<String, List<String>> extractHandlerEvents(final List<CloudTrailEvent> cloudTrailEvents) {
        Map<String, List<String>> handlerEvents = new HashMap<>();
        try {
            handlerEvents.put("create-handler-events", new ArrayList<>());
            handlerEvents.put("update-handler-events", new ArrayList<>());
            handlerEvents.put("delete-handler-events", new ArrayList<>());
            cloudTrailEvents.forEach( event -> {
                if(event.isCreatedWithSession(CREATE_HANDLER_SESSION) &&
                        !event.isEventSource("cloudformation.amazonaws.com"))
                    handlerEvents.get("create-handler-events").add(event.getEventName());
                else if(event.isCreatedWithSession(UPDATE_HANDLER_SESSION) &&
                        !event.isEventSource("cloudformation.amazonaws.com"))
                    handlerEvents.get("update-handler-events").add(event.getEventName());
                else if(event.isCreatedWithSession(DELETE_HANDLER_SESSION) &&
                        !event.isEventSource("cloudformation.amazonaws.com"))
                    handlerEvents.get("delete-handler-events").add(event.getEventName());
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return handlerEvents;
    }

    public List<Input> getInputs(String rootDirPath) {
        try(Stream<Path> dirStream = Files.list(Paths.get(rootDirPath))){
            List<Input> inputs = new ArrayList<>();
            dirStream.forEach(
                    file -> {
                        if(Files.isDirectory(file) && file.getFileName().toString().matches("input[0-9]*")){
                            String dirName = file.getFileName().toString();
                            Input input = new Input();
                            try(Stream<Path> subDirStream = Files.list(file)) {
                                subDirStream.forEach(
                                      nestedFile -> {
                                          String nestedFileName = nestedFile.getFileName().toString();
                                          try {
                                              String createTemplate;
                                              String updateTemplate;
                                              if (nestedFileName.matches("^create.(yaml|json)$")) {
                                                  createTemplate = Files.readString(nestedFile, StandardCharsets.UTF_8);
                                                  input.setCreateTemplateBody(createTemplate);
                                              }
                                              if (nestedFileName.matches("^update.(yaml|json)$")) {
                                                  updateTemplate = Files.readString(nestedFile, StandardCharsets.UTF_8);
                                                  input.setUpdateTemplateBody(updateTemplate);
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
                            if(input.getCreateTemplateBody() == null || input.getUpdateTemplateBody() == null) {
                                throw new IllegalArgumentException("input directory should contain stack template files create.yaml and update.yaml");
                            }
                            inputs.add(input);
                            LOGGER.info(String.format("create and update stack templates read from input directory %s", dirName));
                        }
                    }
            );
            return inputs;
        } catch (IOException e) {
            throw new IllegalArgumentException("Error processing Inputs: " + e + ",from path " + rootDirPath);
        }
    }

    private Map<String, String> setUpInfraStructure(final StackHandler setUpStackHandler,
                                     final S3Handler s3Handler,
                                     final List<String> stackOutputNames) throws IOException {
            Map<String, String> trailStackParameters = new HashMap<>();
            trailStackParameters.put("TrailName", TRAIL_NAME);
            trailStackParameters.put("ObjectPrefix", CLOUDTRAIL_LOGS_PREFIX);
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
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(fileName);
        if(inputStream == null) {
            throw new IOException(String.format("file %s not found on classpath of %s",
                    fileName, this.getClass().getSimpleName()));
        }
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
        s3Handler.deleteObjectsWithPrefix(TRAIL_LOGS_BUCKET, CLOUDTRAIL_LOGS_PREFIX);
        // delete the trail & dependencies stacks
        if(setUpStackHandler.isStackExists(TRAIL_STACK_NAME)) {
            setUpStackHandler.deleteStack(TRAIL_STACK_NAME);
            LOGGER.info(String.format("Trail Stack %s deleted", TRAIL_STACK_NAME));
        }
        if(setUpStackHandler.isStackExists(TRAIL_DEPENDENCIES_STACK_NAME)) {
            setUpStackHandler.deleteStack(TRAIL_DEPENDENCIES_STACK_NAME);
            LOGGER.info(String.format("Trail Dependencies Stack %s deleted", TRAIL_DEPENDENCIES_STACK_NAME));
        }
    }

    public void processInputs(List<Input> inputs,
                               StackHandler createStackHandler,
                               StackHandler updateStackHandler,
                               StackHandler deleteStackHandler){
        for(Input input : inputs) {
            createStackHandler.createStack(HANDLER_STACK_NAME, input.getCreateTemplateBody(), null);
            updateStackHandler.updateStack(HANDLER_STACK_NAME, input.getUpdateTemplateBody(), null);
            deleteStackHandler.deleteStack(HANDLER_STACK_NAME);
        }
    }

    //TODO: load the properties from config
    private Properties loadConfig() {
        return new Properties();
    }

}
