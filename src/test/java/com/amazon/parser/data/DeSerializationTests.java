package com.amazon.parser.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class DeSerializationTests {
    String notification  = "{\n" +
            "    \"Type\" : \"Notification\",\n" +
            "    \"MessageId\" : \"b118b3d6-0d76-5532-b9ca-ed1e49e61040\",\n" +
            "    \"TopicArn\" : \"arn:aws:sns:us-west-2:609231357994:ct-setup-us-west-2-stack-TrailTopic-rMlaN8Hw31nb\",\n" +
            "    \"Message\" : \"{\\\"s3Bucket\\\":\\\"ct-setup-us-west-2-stack-trailbucket-otrb89kxachb\\\",\\\"s3ObjectKey\\\":[\\\"HandlerInvocations/AWSLogs/609231357994/CloudTrailHandler/us-west-2/2024/05/22/609231357994_CloudTrail_us-west-2_20240522T1905Z_4q25LggTW2uvVIh9.json.gz\\\"]}\",\n" +
            "    \"Timestamp\" : \"2024-05-22T19:06:56.984Z\",\n" +
            "    \"SignatureVersion\" : \"1\",\n" +
            "    \"Signature\" : \"di+LzDT0vvSd1cFXJojCcv0gk8hvmp1pV9X3KYAQny1Igdx12Ga3eaS6BinHbAjT5zjI2NTNamckNuftfu9bB+V/jJOFFah/c9KFrlrGggpoJAF5mgUgnEd7RTe8C16AhbdzSv2Lh8ub5pqesXwjRQCCWttiJZYoauTfSjGyvhI7l0QFyazRTSN1bOKQ/Q+kLIApswlLh1hmbQXS4Mmr/Zd4NE2g/M72/i4Jk1hb85zkz3UtT/ZwlLd1FpfO1vBc9sK/Udi9XKWud31P3RPudfP2If75XM9TVijopgf0UlsZBFjiGc8s6o/T+GWTBuBgqODPI8ufmRFN2CKtJlaNPg==\",\n" +
            "    \"SigningCertURL\" : \"https://sns.us-west-2.amazonaws.com/SimpleNotificationService-60eadc530605d63b8e62a523676ef735.pem\",\n" +
            "    \"UnsubscribeURL\" : \"https://sns.us-west-2.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-west-2:609231357994:ct-setup-us-west-2-stack-TrailTopic-rMlaN8Hw31nb:f644181e-62b3-49a3-b13a-4d15b5ee3a4a\"\n" +
            "  }";

    String cloudTrailEvent = "{\n" +
            "        \"eventVersion\": \"1.09\",\n" +
            "        \"userIdentity\": {\n" +
            "            \"type\": \"AssumedRole\",\n" +
            "            \"principalId\": \"AROAY3WIEZAVIOXNK5OJP:create-handler-permissions\",\n" +
            "            \"arn\": \"arn:aws:sts::609231357994:assumed-role/ct-setup-us-west-2-stack-HandlerInvocationRole-p1pPMVxSglgp/create-handler-permissions\",\n" +
            "            \"accountId\": \"609231357994\",\n" +
            "            \"accessKeyId\": \"ASIAY3WIEZAVLAVDGX4A\",\n" +
            "            \"sessionContext\": {\n" +
            "                \"sessionIssuer\": {\n" +
            "                    \"type\": \"Role\",\n" +
            "                    \"principalId\": \"AROAY3WIEZAVIOXNK5OJP\",\n" +
            "                    \"arn\": \"arn:aws:iam::609231357994:role/ct-setup-us-west-2-stack-HandlerInvocationRole-p1pPMVxSglgp\",\n" +
            "                    \"accountId\": \"609231357994\",\n" +
            "                    \"userName\": \"ct-setup-us-west-2-stack-HandlerInvocationRole-p1pPMVxSglgp\"\n" +
            "                },\n" +
            "                \"attributes\": {\n" +
            "                    \"creationDate\": \"2024-05-23T07:25:13Z\",\n" +
            "                    \"mfaAuthenticated\": \"false\"\n" +
            "                }\n" +
            "            },\n" +
            "            \"invokedBy\": \"cloudformation.amazonaws.com\"\n" +
            "        },\n" +
            "        \"eventTime\": \"2024-05-23T07:25:26Z\",\n" +
            "        \"eventSource\": \"s3.amazonaws.com\",\n" +
            "        \"eventName\": \"GetBucketInventoryConfiguration\",\n" +
            "        \"awsRegion\": \"us-west-2\",\n" +
            "        \"sourceIPAddress\": \"cloudformation.amazonaws.com\",\n" +
            "        \"userAgent\": \"cloudformation.amazonaws.com\",\n" +
            "        \"requestParameters\": {\n" +
            "            \"bucketName\": \"ct-create-handler-us-west-2-stack-mybucket-dn7ogvta84wo\",\n" +
            "            \"Host\": \"ct-create-handler-us-west-2-stack-mybucket-dn7ogvta84wo.s3.us-west-2.amazonaws.com\",\n" +
            "            \"inventory\": \"\"\n" +
            "        },\n" +
            "        \"responseElements\": null,\n" +
            "        \"additionalEventData\": {\n" +
            "            \"SignatureVersion\": \"SigV4\",\n" +
            "            \"CipherSuite\": \"TLS_AES_128_GCM_SHA256\",\n" +
            "            \"bytesTransferredIn\": 0,\n" +
            "            \"AuthenticationMethod\": \"AuthHeader\",\n" +
            "            \"x-amz-id-2\": \"JFhg02h784W8sHNCrU1JYuEJ2blTg0975+4KynyDHDu0Ofh2Ed0Bj8CZBrhp47Y8Cu+m39yFuSnvUKaOQeRpGw==\",\n" +
            "            \"bytesTransferredOut\": 189\n" +
            "        },\n" +
            "        \"requestID\": \"F8T4YR9P54TCRGFZ\",\n" +
            "        \"eventID\": \"ef276487-3d82-462b-9eba-f80d36aa06ad\",\n" +
            "        \"readOnly\": true,\n" +
            "        \"resources\": [{\n" +
            "            \"accountId\": \"609231357994\",\n" +
            "            \"type\": \"AWS::S3::Bucket\",\n" +
            "            \"ARN\": \"arn:aws:s3:::ct-create-handler-us-west-2-stack-mybucket-dn7ogvta84wo\"\n" +
            "        }],\n" +
            "        \"eventType\": \"AwsApiCall\",\n" +
            "        \"managementEvent\": true,\n" +
            "        \"recipientAccountId\": \"609231357994\",\n" +
            "        \"eventCategory\": \"Management\"\n" +
            "    }";
    @Test
    public void testDeSerializationNotification() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Notification n = mapper.readValue(notification, Notification.class);
            String message  = n.getMessage();
            if(message.contains("s3Bucket")){
                S3NotificationMessage s3Message = mapper.readValue(message, S3NotificationMessage.class);
                System.out.println(s3Message.getBucket());
                System.out.println(s3Message.getObjects());
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testDeSerializationCloudTrailEvent() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            CloudTrailEvent event = mapper.readValue(cloudTrailEvent, CloudTrailEvent.class);
            System.out.println("Principal Id: "+ event.getUserIdentity().getPrincipalId());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

}
