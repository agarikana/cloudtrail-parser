package com.amazon.parser.wrappers;

import com.amazon.parser.factories.ClientFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class S3HandlerTests {

    @Mock
    ClientFactory clientFactory;
    @Mock
    S3Client s3Client;

    @BeforeEach
    public void setUp() {
        clientFactory = mock(ClientFactory.class);
        s3Client = mock(S3Client.class);
        when(clientFactory.getClient(any(Region.class), any(S3ClientBuilder.class))).thenReturn(s3Client);

    }

    @Test
    void testDoesBucketExistSuccess() {
        S3Handler s3Handler = new S3Handler(clientFactory, "us-west-2");
        String bucketName = "test-bucket";
        when(s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build())).thenReturn(HeadBucketResponse.builder().build());
        Assertions.assertTrue(s3Handler.doesBucketExist(bucketName));
    }

    @Test
    void testDoesBucketExistFailure() {
        S3Handler s3Handler = new S3Handler(clientFactory, "us-west-2");
        String bucketName = "test-bucket";
        when(s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build())).thenThrow(NoSuchBucketException.class);
        Assertions.assertFalse(s3Handler.doesBucketExist(bucketName));
    }

}
