package com.amazon.parser.factories;

import org.junit.Ignore;
import org.junit.Test;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;

@Ignore
public class ClientTests {

    @Test
    public void buildDefaultClient() {
        ClientFactory factory = new ClientFactory();
        CloudFormationClient cfnClient = (CloudFormationClient) factory.getClient(Region.of("us-west-2"), CloudFormationClient.builder());
        cfnClient.listStacks().stackSummaries()
                .forEach(stackSummary -> System.out.println(stackSummary.stackName()));

    }

    @Test
    public void buildAssumeRoleCredentialsClient() {
        ClientFactory factory = new ClientFactory();
        CloudFormationClient cfnClient = (CloudFormationClient) factory.getClientWithAssumedRole(Region.of("us-west-2"),
                "arn:aws:iam::609231357994:role/aws-s3-bucket-role-stack-ExecutionRole-SZEAMVBBEGRI",
                "temp-session-1", CloudFormationClient.builder());
        cfnClient.listStacks().stackSummaries()
                .forEach(stackSummary -> System.out.println(stackSummary.stackName()));
    }
}
