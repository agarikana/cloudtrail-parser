package com.amazon.parser.wrappers;

import org.junit.Test;
import org.junit.Ignore;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class S3HandlerTests {
    @Ignore
    @Test public void testEmptyBucket() {
        S3Handler s3Handler = new S3Handler(S3Client.builder()
                .httpClient(ApacheHttpClient.builder().build())
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .build());

        s3Handler.deleteObjectsWithPrefix("aws-cloudtrail-logs-609231357994-c54137bb", "AWSLogs");
    }
}
