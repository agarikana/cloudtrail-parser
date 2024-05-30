package org.example.wrappers;

import lombok.NonNull;
import org.apache.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.util.List;
import java.util.stream.Collectors;

public class S3Handler {
    private static final Logger LOGGER = Logger.getLogger(S3Handler.class);
    private static final String NO_SUCH_BUCKET_MSG = "Bucket %s does not exist. Ignoring";
    private final S3Client s3Client;
    public S3Handler(S3Client s3Client) {
        this.s3Client = s3Client;
    }
    public S3Handler(String region, AwsCredentialsProvider awsCredentialsProvider) {
        assert region != null && awsCredentialsProvider != null;
        this.s3Client = S3Client.builder()
                        .httpClient(ApacheHttpClient.builder().build())
                .region(Region.of(region))
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }

    /**
     * Delete an empty Bucket
     * @param bucketName name of the bucket
     */
    public void deleteBucketSilently(@NonNull String bucketName) {
        try {
            this.s3Client.deleteBucket(DeleteBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
        } catch (NoSuchBucketException e) {
            LOGGER.error(NO_SUCH_BUCKET_MSG);
        }
    }

    public void emptyBucket(@NonNull String bucketName) {
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder().bucket(bucketName).build();
        ListObjectsV2Iterable listObjectsV2Iterable = this.s3Client.listObjectsV2Paginator(listObjectsV2Request);
        for(ListObjectsV2Response listObjectsV2Response : listObjectsV2Iterable) {
            List<S3Object> s3Objects = listObjectsV2Response.contents();
            List<ObjectIdentifier> objectIdentifiers = s3Objects.stream().
                    map(o -> ObjectIdentifier.builder().key(o.key()).build())
                    .toList();
            DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder().objects(objectIdentifiers).build())
                    .build();
            this.s3Client.deleteObjects(deleteObjectsRequest);
        }
    }

    public boolean doesBucketExist(@NonNull String bucketName) {
        try {
            this.s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        }
    }
}
