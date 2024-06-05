package com.amazon.parser.wrappers;

import com.amazon.parser.factories.ClientFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.CloudFormationClientBuilder;
import software.amazon.awssdk.services.cloudformation.model.*;
import software.amazon.awssdk.services.cloudformation.waiters.CloudFormationWaiter;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StackHandlerTests {

    @Mock
    private ClientFactory clientFactory;
    @Mock
    private CloudFormationClient cloudFormationClient;
    @Mock
    private CloudFormationWaiter cloudFormationWaiter;
    private StackHandler stackHandler;

    public static String REGION = "us-east-1";
    public static String STACK_TEMPLATE_WITHOUT_PARAMS = "{\n" +
            "  Resources: {\n" +
            "    \"resource1\" : {\n" +
            "    \t\"Type\": \"AWS::Dummy::type\"\n" +
            "    \t\"Properties\": {\n" +
            "    \t   \"property1\": \"value1\",\n" +
            "    \t   \"property2\": {\n" +
            "    \t      \"key\": \"name1\"\n" +
            "    \t      \"value\": \"value1\"\n" +
            "    \t   }\n" +
            "    \t}\n" +
            "    }\n" +
            "  }\n" +
            "}";

    @BeforeEach
    public void setUp() {
        clientFactory = mock(ClientFactory.class);
        cloudFormationClient = mock(CloudFormationClient.class);
        cloudFormationWaiter = mock(CloudFormationWaiter.class);
        when(clientFactory.getClient(any(Region.class), any(CloudFormationClientBuilder.class))).thenReturn(cloudFormationClient);
        when(cloudFormationClient.waiter()).thenReturn(cloudFormationWaiter);
        stackHandler = new StackHandler(clientFactory, REGION);
    }

    @Test
    void testStackCreate() {
        String stackName = "test-stack-1";
        String stackTemplateBody = STACK_TEMPLATE_WITHOUT_PARAMS;
        CreateStackRequest createStackRequest = CreateStackRequest.builder()
                        .stackName(stackName)
                                .templateBody(stackTemplateBody).build();
        DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder()
                        .stackName(stackName).build();
        DescribeStacksResponse describeStacksResponse = DescribeStacksResponse.builder()
                .stacks(Stack.builder().stackName(stackName).stackStatus("CREATE_COMPLETE").build())
                .build();
        when(cloudFormationClient.createStack(createStackRequest)).thenReturn(CreateStackResponse.builder().build());
        when(cloudFormationClient.describeStacks(describeStacksRequest)).thenReturn(DescribeStacksResponse.builder().build());
        when(cloudFormationWaiter.waitUntilStackCreateComplete(describeStacksRequest)).thenReturn(
                new WaiterResponse<DescribeStacksResponse>(){
                    @Override
                    public ResponseOrException<DescribeStacksResponse> matched() {
                        return ResponseOrException.<DescribeStacksResponse>response(describeStacksResponse);
                    }

                    @Override
                    public int attemptsExecuted() {
                        return 1;
                    }
                });

        stackHandler.createStack(stackName, stackTemplateBody, null);
        Assertions.assertDoesNotThrow(() -> stackHandler.createStack(stackName, stackTemplateBody, null));
    }

    @Test
    void testStackCreateFailed() {
        String stackName = "test-stack-1";
        String stackTemplateBody = STACK_TEMPLATE_WITHOUT_PARAMS;
        CreateStackRequest createStackRequest = CreateStackRequest.builder()
                .stackName(stackName)
                .templateBody(stackTemplateBody).build();
        DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder()
                .stackName(stackName).build();
        when(cloudFormationClient.createStack(createStackRequest)).thenReturn(CreateStackResponse.builder().build());
        when(cloudFormationWaiter.waitUntilStackCreateComplete(describeStacksRequest)).thenReturn(
                new WaiterResponse<DescribeStacksResponse>(){
                    @Override
                    public ResponseOrException<DescribeStacksResponse> matched() {
                        return ResponseOrException.<DescribeStacksResponse>exception(new RuntimeException("stack creation failed"));
                    }

                    @Override
                    public int attemptsExecuted() {
                        return 1;
                    }
                });

        Assertions.assertThrows(RuntimeException.class, () -> stackHandler.createStack(stackName, stackTemplateBody, null));
    }

    @Test
    void testStackUpdate() {
        String stackName = "test-stack-1";
        String stackTemplateBody = STACK_TEMPLATE_WITHOUT_PARAMS;
        UpdateStackRequest updateStackRequest = UpdateStackRequest.builder()
                .stackName(stackName)
                .templateBody(stackTemplateBody)
                .build();
        DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder()
                .stackName(stackName).build();
        DescribeStacksResponse describeStacksResponse = DescribeStacksResponse.builder()
                .stacks(Stack.builder().stackName(stackName).stackStatus("UPDATE_COMPLETE").build())
                .build();
        when(cloudFormationClient.updateStack(updateStackRequest)).thenReturn(UpdateStackResponse.builder().build());
        when(cloudFormationWaiter.waitUntilStackUpdateComplete(describeStacksRequest)).thenReturn(
                new WaiterResponse<DescribeStacksResponse>(){
                    @Override
                    public ResponseOrException<DescribeStacksResponse> matched() {
                        return ResponseOrException.<DescribeStacksResponse>response(describeStacksResponse);
                    }

                    @Override
                    public int attemptsExecuted() {
                        return 1;
                    }
                });
        Assertions.assertDoesNotThrow(() -> stackHandler.updateStack(stackName, stackTemplateBody, null));
    }

    @Test
    void testStackUpdateFailed() {
        String stackName = "test-stack-1";
        String stackTemplateBody = STACK_TEMPLATE_WITHOUT_PARAMS;
        UpdateStackRequest updateStackRequest = UpdateStackRequest.builder()
                .stackName(stackName)
                .templateBody(stackTemplateBody)
                .build();
        DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder()
                .stackName(stackName).build();
        when(cloudFormationClient.updateStack(updateStackRequest)).thenReturn(UpdateStackResponse.builder().build());
        when(cloudFormationWaiter.waitUntilStackUpdateComplete(describeStacksRequest)).thenReturn(
                new WaiterResponse<DescribeStacksResponse>(){
                    @Override
                    public ResponseOrException<DescribeStacksResponse> matched() {
                        return ResponseOrException.<DescribeStacksResponse>exception(new RuntimeException("stack update failed"));
                    }

                    @Override
                    public int attemptsExecuted() {
                        return 1;
                    }
                });
        Assertions.assertThrows(RuntimeException.class, () -> stackHandler.updateStack(stackName, stackTemplateBody, null));
    }

    @Test
    void testStackDeleteSuccess1() {
        String stackName = "test-stack-1";
        DeleteStackRequest deleteStackRequest = DeleteStackRequest.builder()
                .stackName(stackName)
                .build();
        DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder()
                .stackName(stackName).build();
        DescribeStacksResponse describeStacksResponse = DescribeStacksResponse.builder()
                .stacks(Stack.builder().stackName(stackName).stackStatus("DELETE_COMPLETE").build())
                .build();
        when(cloudFormationClient.deleteStack(deleteStackRequest)).thenReturn(DeleteStackResponse.builder().build());
        when(cloudFormationWaiter.waitUntilStackDeleteComplete(describeStacksRequest)).thenReturn(
                new WaiterResponse<DescribeStacksResponse>(){
                    @Override
                    public ResponseOrException<DescribeStacksResponse> matched(){
                        return ResponseOrException.<DescribeStacksResponse>response(describeStacksResponse);
                    };
                    @Override
                    public int attemptsExecuted() {
                        return 1;
                    }
                }
        );
        Assertions.assertDoesNotThrow(()-> stackHandler.deleteStack(stackName));
    }

    @Test
    void testStackDeleteSuccess2() {
        String stackName = "test-stack-1";
        DeleteStackRequest deleteStackRequest = DeleteStackRequest.builder()
                .stackName(stackName)
                .build();
        DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder()
                .stackName(stackName).build();
        CloudFormationException cloudFormationException = (CloudFormationException) CloudFormationException.builder()
                .statusCode(400).build();
        when(cloudFormationClient.deleteStack(deleteStackRequest)).thenReturn(DeleteStackResponse.builder().build());
        when(cloudFormationWaiter.waitUntilStackDeleteComplete(describeStacksRequest)).thenReturn(
                new WaiterResponse<DescribeStacksResponse>(){
                    @Override
                    public ResponseOrException<DescribeStacksResponse> matched(){
                        return ResponseOrException.<DescribeStacksResponse>exception(cloudFormationException);
                    };
                    @Override
                    public int attemptsExecuted() {
                        return 1;
                    }
                }
        );
        Assertions.assertDoesNotThrow(()-> stackHandler.deleteStack(stackName));
    }

    @Test
    void testStackDeleteFailed() {
        String stackName = "test-stack-1";
        DeleteStackRequest deleteStackRequest = DeleteStackRequest.builder()
                .stackName(stackName)
                .build();
        DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder()
                .stackName(stackName).build();
        CloudFormationException cloudFormationException = (CloudFormationException) CloudFormationException.builder()
                        .statusCode(500).build();
        when(cloudFormationClient.deleteStack(deleteStackRequest)).thenReturn(DeleteStackResponse.builder().build());
        when(cloudFormationWaiter.waitUntilStackDeleteComplete(describeStacksRequest)).thenReturn(
                new WaiterResponse<DescribeStacksResponse>(){
                    @Override
                    public ResponseOrException<DescribeStacksResponse> matched(){
                        return ResponseOrException.<DescribeStacksResponse>exception(cloudFormationException);
                    };
                    @Override
                    public int attemptsExecuted() {
                        return 1;
                    }
                });
        Assertions.assertThrows(RuntimeException.class, () -> stackHandler.deleteStack(stackName));
    }

    @Test
    void testGetStackOutputs() {
        String stackName = "test-stack-1";
        Output stackOutput = Output.builder().outputKey("key1").outputValue("value1").build();
        DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder()
                        .stackName(stackName).build();
        DescribeStacksResponse describeStacksResponse = DescribeStacksResponse.builder()
                        .stacks(Stack.builder().stackName(stackName).outputs(List.of(stackOutput)).build())
                        .build();
        when(cloudFormationClient.describeStacks(describeStacksRequest)).thenReturn(describeStacksResponse);
        Map<String, String> outputsMap = stackHandler.getStackOutputs(stackName);

        Assertions.assertEquals("value1", outputsMap.get("key1"));
    }
    @Test
    void testGetStackOutputsFailed() {
        String stackName = "test-stack-1";
        DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder()
                .stackName(stackName).build();
        when(cloudFormationClient.describeStacks(describeStacksRequest)).thenThrow(CloudFormationException.builder().build());
        Assertions.assertThrows(CloudFormationException.class, () -> stackHandler.getStackOutputs(stackName));
    }

    @Test
    void testGetStackOutputsByName() {
        String stackName = "test-stack-1";
        Output stackOutput1 = Output.builder().outputKey("key1").outputValue("value1").build();
        Output stackOutput2 = Output.builder().outputKey("key2").outputValue("value2").build();
        DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder()
                .stackName(stackName).build();
        DescribeStacksResponse describeStacksResponse = DescribeStacksResponse.builder()
                .stacks(Stack.builder().stackName(stackName).outputs(List.of(stackOutput1, stackOutput2)).build())
                .build();
        when(cloudFormationClient.describeStacks(describeStacksRequest)).thenReturn(describeStacksResponse);
        Map<String, String> outputsMap = stackHandler.getStackOutputsByName(stackName, List.of("key1"));
        Assertions.assertFalse(outputsMap.containsKey("key2"));
        Assertions.assertTrue(outputsMap.containsKey("key1"));
        Assertions.assertEquals(outputsMap.get("key1"), "value1");
    }

    @Test
    void testGetStackOutputsByNameFailure() {
        String stackName = "test-stack-1";
        Output stackOutput1 = Output.builder().outputKey("key1").outputValue("value1").build();
        Output stackOutput2 = Output.builder().outputKey("key2").outputValue("value2").build();
        DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder()
                .stackName(stackName).build();
        DescribeStacksResponse describeStacksResponse = DescribeStacksResponse.builder()
                .stacks(Stack.builder().stackName(stackName).outputs(List.of(stackOutput1, stackOutput2)).build())
                .build();
        when(cloudFormationClient.describeStacks(describeStacksRequest)).thenReturn(describeStacksResponse);
        Assertions.assertThrows(RuntimeException.class, () -> stackHandler.getStackOutputsByName(stackName, List.of("key1", "extraKey")));
    }
}
