package com.amazon.parser.wrappers;

import com.amazon.parser.factories.ClientFactory;
import lombok.NonNull;
import org.apache.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.util.List;

public class S3Handler {
    private static final Logger LOGGER = Logger.getLogger(S3Handler.class);
    private static final String NO_SUCH_BUCKET_MSG = "Bucket %s does not exist. Ignoring";
    private final S3Client s3Client;

    public S3Handler(ClientFactory clientFactory, String region) {
        this.s3Client = (S3Client) clientFactory.getClient(Region.of(region), S3Client.builder());
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

    public void deleteObjectsWithPrefix(@NonNull String bucketName, @NonNull String prefix) {
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();
        ListObjectsV2Iterable listObjectsV2Iterable = this.s3Client.listObjectsV2Paginator(listObjectsV2Request);
        for(ListObjectsV2Response listObjectsV2Response : listObjectsV2Iterable) {
            List<S3Object> s3Objects = listObjectsV2Response.contents();
            List<ObjectIdentifier> objectIdentifiers = s3Objects.stream()
                    .map(o -> ObjectIdentifier.builder().key(o.key()).build())
                    .toList();
            if(objectIdentifiers.isEmpty()) {
                LOGGER.info("Bucket " + bucketName + " does not contain any objects");
                continue;
            }
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
