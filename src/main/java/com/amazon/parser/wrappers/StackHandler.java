package com.amazon.parser.wrappers;

import org.apache.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.CloudFormationClientBuilder;
import software.amazon.awssdk.services.cloudformation.model.*;
import software.amazon.awssdk.services.cloudformation.waiters.CloudFormationWaiter;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;

import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CloudFormation Stack handler
 * handles all stack operations
 */
public class StackHandler {

    private final CloudFormationClient client;
    private final CloudFormationWaiter waiter;
    private static final Logger LOGGER = Logger.getLogger(StackHandler.class);
    public StackHandler(String region, String endpoint, String assumeRoleArn, String sessionName) {
        assert region != null || endpoint != null;
        AwsCredentialsProvider awsCredentialsProvider;
        if(assumeRoleArn != null) {
            AssumeRoleRequest req = AssumeRoleRequest.builder()
                    .roleArn(assumeRoleArn)
                    .roleSessionName(sessionName)
                    .build();

            awsCredentialsProvider = StsAssumeRoleCredentialsProvider.builder()
                    .refreshRequest(req)
                    .stsClient(StsClient.builder()
                            .httpClient(ApacheHttpClient.builder().build())
                            .region(region != null? Region.of(region): Region.AWS_GLOBAL).build())
                    .build();
        } else {
            awsCredentialsProvider = DefaultCredentialsProvider.builder().build();
        }

        CloudFormationClientBuilder cfnBuilder = CloudFormationClient.builder();
        if(region != null) {
            cfnBuilder.region(Region.of(region));
        }
        if(endpoint != null) {
            cfnBuilder.endpointOverride(URI.create(endpoint));
        }
        this.client = cfnBuilder
                .httpClient(ApacheHttpClient.builder().build())
                .credentialsProvider(awsCredentialsProvider)
                .build();
        this.waiter = client.waiter();

    }
    public StackHandler(String region, String endpoint) {
        this(region, endpoint, null, null);
    }

    public void createStack(String stackName, String stackTemplate, Map<String, String> parameters) {
        CreateStackRequest.Builder createStackRequestBuilder = CreateStackRequest.builder()
                .stackName(stackName)
                .templateBody(stackTemplate)
                .capabilities(Capability.CAPABILITY_IAM);
        if(parameters != null && !parameters.isEmpty()){
            createStackRequestBuilder = createStackRequestBuilder.parameters(
                    parameters.entrySet()
                            .stream()
                            .map(entry -> Parameter.builder()
                                        .parameterKey(entry.getKey())
                                        .parameterValue(entry.getValue())
                                        .build())
                            .collect(Collectors.toList()));
        }

        try {
            client.createStack(createStackRequestBuilder.build());
            DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder()
                    .stackName(stackName)
                    .build();

            WaiterResponse<DescribeStacksResponse> waiterResponse = waiter.waitUntilStackCreateComplete(describeStacksRequest);
            waiterResponse.matched().response().ifPresent(LOGGER::info);
            waiterResponse.matched().exception().ifPresent( exp -> {
                LOGGER.error(exp);
                throw new RuntimeException(exp);
            });
        } catch (AwsServiceException | SdkClientException e) {
            LOGGER.error("stack creation failed: received exception :" + e.getMessage());
            deleteStack(stackName);
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> getStackOutputsByName(final String stackName, final List<String> outputs) {
        Map<String, String> filteredOutputs = this.getStackOutputs(stackName)
                .entrySet()
                .stream()
                .filter(entry -> outputs.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if(filteredOutputs.size() != outputs.size()){
            throw new RuntimeException("some name stack outputs are not present in the stack");
        }
        return filteredOutputs;
    }

    public Map<String, String> getStackOutputs(final String stackName) {
        DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder()
                .stackName(stackName)
                .build();
        DescribeStacksResponse describeStacksResponse = client.describeStacks(describeStacksRequest);
        if(describeStacksResponse.hasStacks()){
            return describeStacksResponse.stacks().get(0).outputs()
                    .stream().collect(Collectors.toMap(Output::outputKey, Output::outputValue));
        } else {
            throw new RuntimeException(String.format("Stack with name %s not found", stackName));
        }
    }

    public boolean isStackExists(final String stackName) {
        DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder()
                .stackName(stackName)
                .build();
        try {
            DescribeStacksResponse describeStacksResponse = client.describeStacks(describeStacksRequest);
            return describeStacksResponse.hasStacks();
        } catch (AwsServiceException e){
            if(e.awsErrorDetails() != null && e.awsErrorDetails().sdkHttpResponse().statusCode() == 400){
                return false;
            }
            throw e;
        }
    }


    public void deleteStackSilently(String stackName) {
        try {
            deleteStack(stackName);
        } catch (Exception e) {
            if(e.getCause() instanceof CloudFormationException) {
                LOGGER.error(e.getMessage());
                return;
            }
            throw e;
        }
    }

    public void updateStack(String stackName, String stackTemplate, Map<String, String> parameters) {
        UpdateStackRequest.Builder updateStackRequestBuilder = UpdateStackRequest.builder()
                .stackName(stackName)
                .templateBody(stackTemplate)
                .capabilities(Capability.CAPABILITY_IAM);
        if(parameters != null && !parameters.isEmpty()){
            updateStackRequestBuilder.parameters(
                    parameters.entrySet()
                            .stream()
                            .map(entry -> Parameter.builder()
                                    .parameterKey(entry.getKey())
                                    .parameterValue(entry.getValue())
                                    .build())
                            .collect(Collectors.toList()));
        }

        try {
            client.updateStack(updateStackRequestBuilder.build());
            DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder()
                    .stackName(stackName)
                    .build();

            WaiterResponse<DescribeStacksResponse> waiterResponse = waiter.waitUntilStackUpdateComplete(describeStacksRequest);
            waiterResponse.matched().response().ifPresent(LOGGER::info);
            waiterResponse.matched().exception().ifPresent( exp -> {
                LOGGER.error(exp);
                System.exit(1);
            });
        } catch (AwsServiceException | SdkClientException e) {
            LOGGER.error(String.format("stack update failed for stack %s: received exception %s:",stackName, e.getMessage()));
            throw new RuntimeException(e);
        }
    }

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
            waiterResponse.matched().response().ifPresent(LOGGER::info);
        } catch (CloudFormationException | SdkClientException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}

