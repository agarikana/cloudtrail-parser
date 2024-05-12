package org.example;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.CloudFormationException;
import software.amazon.awssdk.services.cloudformation.model.CreateStackRequest;
import software.amazon.awssdk.services.cloudformation.model.DeleteStackRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksResponse;
import software.amazon.awssdk.services.cloudformation.waiters.CloudFormationWaiter;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;

import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

/**
 * CloudFormation Stack handler
 * handles all stack operations
 */
public class StackHandler {

    private final CloudFormationClient client;
    private final CloudFormationWaiter waiter;
    public StackHandler(String region, String executionRoleArn, String sessionName) {
        AssumeRoleRequest req = AssumeRoleRequest.builder()
                .roleArn(executionRoleArn)
                .roleSessionName(sessionName)
                .build();
        AwsCredentialsProvider stsCredentialProvider = StsAssumeRoleCredentialsProvider.builder()
                .refreshRequest(req)
                .stsClient(StsClient.builder().httpClient(ApacheHttpClient.builder().build()).build())
                .build();

        this.client = CloudFormationClient.builder()
                .region(Region.of(region))
                .httpClient(ApacheHttpClient.builder().build())
                .credentialsProvider(stsCredentialProvider)
                .build();
        this.waiter = client.waiter();
    }

    public void createStack(String stackName, String stackTemplate) {
        CreateStackRequest createStackRequest = CreateStackRequest.builder()
                .stackName(stackName)
                .templateBody(stackTemplate)
                .build();
        try {
            client.createStack(createStackRequest);
            DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder()
                    .stackName(stackName)
                    .build();
            WaiterResponse<DescribeStacksResponse> waiterResponse = waiter.waitUntilStackCreateComplete(describeStacksRequest);
            waiterResponse.matched().response().ifPresent(System.out::println);
        } catch (CloudFormationException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    //TODO: updateStack

    public void deleteStack(String stackName) {
        DeleteStackRequest deleteStackRequest = DeleteStackRequest.builder()
                .stackName(stackName)
                .build();
        try {
            client.deleteStack(deleteStackRequest);
            DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder()
                    .stackName(stackName)
                    .build();
            WaiterResponse<DescribeStacksResponse> waiterResponse = waiter.waitUntilStackDeleteComplete(describeStacksRequest);
            waiterResponse.matched().response().ifPresent(System.out::println);
        } catch (CloudFormationException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

}

