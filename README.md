# Description:
This command line application generates all the aws api calls  invoked by the create/update/delete cloudformation handlers. It sets up a cloudtrail trail to log the cloudtrail event logs & processes them to extract the api calls.

## What is does:
1. Sets up a cloudTrail trail to log cloudtrail events
2. Runs the cloudformation handlers via the input stacks.
3. Polls for notifications on queue setup in step1 for logs availability in s3 bucket. notifications are generated by cloudtrail.
4. Processes the logs from s3 bucket & extracts handler api calls & displays them.

Example Output:-
```
{
  "delete-handler-events" : [ "DeleteBucket" ],
  "create-handler-events" : [ "GetBucketAnalyticsConfiguration", "GetBucketLogging", "GetBucketInventoryConfiguration", "GetBucketMetricsConfiguration", "GetBucketNotification", "GetAccelerateConfiguration", "GetBucketEncryption", "GetBucketObjectLockConfiguration", "GetBucketIntelligentTieringConfiguration", "GetBucketCors", "GetBucketLifecycle", "GetBucketOwnershipControls", "GetBucketPublicAccessBlock", "GetBucketReplication", "GetBucketTagging", "GetBucketVersioning", "GetBucketWebsite", "CreateBucket" ],
  "update-handler-events" : [ "PutBucketTagging", "GetBucketAnalyticsConfiguration", "GetBucketEncryption", "GetAccelerateConfiguration", "GetBucketCors", "GetBucketIntelligentTieringConfiguration", "GetBucketInventoryConfiguration", "GetBucketMetricsConfiguration", "GetBucketLifecycle", "GetBucketLogging", "GetBucketNotification", "GetBucketPublicAccessBlock", "GetBucketObjectLockConfiguration", "GetBucketTagging", "GetBucketVersioning", "GetBucketReplication", "GetBucketWebsite", "GetBucketOwnershipControls" ]
}
```
# How to Run:
1. Create an input dir structure like below that contains all the create/update stack templates(these should cover 100% of the properties of the resource).  
These are usually the contract test inputs(converted to cfn templates)
```
inputs
└── input1
    ├── create.json
    └── update.json
...    
```
2. Run the cli.  
From the package root:  
`mvn package`  
`java -jar target/handler-permission-getter.jar --region <region> --templates-root-dir <root path of the stack templates>`  
Ex:-  
`java -jar target/handler-permission-getter.jar -r us-east-1 --d /Users/alexark/inputs`