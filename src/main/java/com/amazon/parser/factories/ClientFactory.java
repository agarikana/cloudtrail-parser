package com.amazon.parser.factories;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.awscore.AwsClient;
import software.amazon.awssdk.awscore.client.builder.AwsSyncClientBuilder;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.StsClientBuilder;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Generic ClientFactory
 */
public class ClientFactory {

    private final HashMap<String, AwsClient> clients = new HashMap<>();
    private final StsClientBuilder stsClientBuilder = StsClient.builder().httpClient(ApacheHttpClient.builder().build());

    public <T extends AwsSyncClientBuilder<?,?>> AwsClient getClient(Region region, T clientBuilder) {
        String clientKey = clientBuilder.getClass().getSimpleName()+"#"+region.toString();
        if(clients.containsKey(clientKey)) {
            return clients.get(clientKey);
        }
        AwsClient client = createClient(region, null, null, clientBuilder);
        clients.put(clientKey, client);
        return client;
    }

    public <T extends AwsSyncClientBuilder<?,?>> AwsClient getClientWithAssumedRole(Region region, String roleArn, String sessionName, T clientBuilder) {
        String clientKey = clientBuilder.getClass().getSimpleName()+"#"+region.toString()+"#"+roleArn+"#"+sessionName;
        if(clients.containsKey(clientKey)) {
            return clients.get(clientKey);
        }
        AwsClient client = createClient(region, roleArn, sessionName, clientBuilder);
        clients.put(clientKey, client);
        return client;
    }

    private <T extends AwsSyncClientBuilder<?,?>> AwsClient createClient(Region region, String roleArn, String sessionName, T clientBuilder) {

        AwsClient client;
        AwsCredentialsProvider awsCredentialsProvider;
        try {
            Method credentialsProvider = clientBuilder.getClass().getMethod("credentialsProvider", AwsCredentialsProvider.class);
            Method httpClient = clientBuilder.getClass().getMethod("httpClient", SdkHttpClient.class);
            httpClient.setAccessible(true);
            Method regionMethod = clientBuilder.getClass().getMethod("region", Region.class);
            Method build = clientBuilder.getClass().getMethod("build");
            if (roleArn != null && sessionName != null){
                AssumeRoleRequest req = AssumeRoleRequest.builder()
                        .roleArn(roleArn)
                        .roleSessionName(sessionName)
                        .build();
                awsCredentialsProvider = StsAssumeRoleCredentialsProvider.builder()
                        .refreshRequest(req)
                        .stsClient(stsClientBuilder.region(region).build())
                        .build();
            } else {
                awsCredentialsProvider = DefaultCredentialsProvider.builder().build();
            }
            credentialsProvider.invoke(clientBuilder, awsCredentialsProvider);
            httpClient.invoke(clientBuilder, ApacheHttpClient.builder().build());
            regionMethod.invoke(clientBuilder, region);
            client = (AwsClient)build.invoke(clientBuilder);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return client;
    }



}
