# Description:
This command line application generates all the aws api calls  invoked by the create/update/delete handlers. It uses cloudtrail trail to generates the cloudtrail event logs & processes them to extract the api calls.

Example Output:-
```
{
  "delete-handler-events" : [ "DeleteBucket" ],
  "create-handler-events" : [ "GetBucketAnalyticsConfiguration", "GetBucketLogging", "GetBucketInventoryConfiguration", "GetBucketMetricsConfiguration", "GetBucketNotification", "GetAccelerateConfiguration", "GetBucketEncryption", "GetBucketObjectLockConfiguration", "GetBucketIntelligentTieringConfiguration", "GetBucketCors", "GetBucketLifecycle", "GetBucketOwnershipControls", "GetBucketPublicAccessBlock", "GetBucketReplication", "GetBucketTagging", "GetBucketVersioning", "GetBucketWebsite", "CreateBucket" ],
  "update-handler-events" : [ "PutBucketTagging", "GetBucketAnalyticsConfiguration", "GetBucketEncryption", "GetAccelerateConfiguration", "GetBucketCors", "GetBucketIntelligentTieringConfiguration", "GetBucketInventoryConfiguration", "GetBucketMetricsConfiguration", "GetBucketLifecycle", "GetBucketLogging", "GetBucketNotification", "GetBucketPublicAccessBlock", "GetBucketObjectLockConfiguration", "GetBucketTagging", "GetBucketVersioning", "GetBucketReplication", "GetBucketWebsite", "GetBucketOwnershipControls" ]
}
```
# How to Run:
From the package root:  
`mvn package`  
`java -jar target/cloudtrail-event-parser-1.0-SNAPSHOT.jar`