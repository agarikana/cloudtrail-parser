package org.example.factories;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.awscore.AwsClient;
import software.amazon.awssdk.awscore.client.builder.AwsSyncClientBuilder;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Generic ClientFactory
 */
public class ClientFactory {

    private final HashMap<String, AwsClient> clients = new HashMap<>();

    public <T extends AwsSyncClientBuilder<?,?>> AwsClient getClient(Region region, T clientBuilder) {
        String clientAndRegion = clientBuilder.getClass().getSimpleName()+"#"+region.toString();
        if(clients.containsKey(clientAndRegion)) {
            return clients.get(clientAndRegion);
        }
        AwsClient client;
        try {
            Method credentialsProvider = clientBuilder.getClass().getMethod("credentialsProvider", AwsCredentialsProvider.class);
            Method httpClient = clientBuilder.getClass().getMethod("httpClient", SdkHttpClient.class);
            httpClient.setAccessible(true);
            Method regionMethod = clientBuilder.getClass().getMethod("region", Region.class);
            Method build = clientBuilder.getClass().getMethod("build");
            credentialsProvider.invoke(clientBuilder, DefaultCredentialsProvider.builder().build());
            httpClient.invoke(clientBuilder, ApacheHttpClient.builder().build());
            regionMethod.invoke(clientBuilder, region);
            client = (AwsClient)build.invoke(clientBuilder);
            clients.put(clientAndRegion, client);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return client;
    }

}
