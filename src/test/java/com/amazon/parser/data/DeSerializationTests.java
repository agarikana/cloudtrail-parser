package com.amazon.parser.data;

import com.amazon.parser.deserializers.DeSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DeSerializationTests {
    DeSerializer<Notification> notificationDeSerializer;
    DeSerializer<S3NotificationMessage> s3NotificationMessageDeSerializer;
    DeSerializer<CloudTrailEvent> cloudTrailEventDeSerializer;
    public static final String PRINCIPAL_ID = "garroshdidnothingwrong1";
    public static final String BUCKET_NAME = "naxaramas";
    public static final String BUCKET_OBJECT = "tirisfallglades/silverpineforest";

    String notification  = "{\n" +
            "    \"Type\" : \"Notification\",\n" +
            "    \"MessageId\" : \"b118b3d6-0d76-5532-b9ca-ed1e49e61040\",\n" +
            "    \"TopicArn\" : \"arn:aws:sns:us-west-2:609231357994:ct-setup-us-west-2-stack-TrailTopic-rMlaN8Hw31nb\",\n" +
            "    \"Message\" : \"{\\\"s3Bucket\\\":\\\""+BUCKET_NAME+"\\\",\\\"s3ObjectKey\\\":[\\\""+BUCKET_OBJECT+"\\\"]}\",\n" +
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
            "            \"principalId\": \""+PRINCIPAL_ID+"\",\n" +
            "            \"arn\": \"arn:aws:sts::609231357994:assumed-role/ct-setup-us-west-2-stack-HandlerInvocationRole-p1pPMVxSglgp/create-handler-permissions\",\n" +
            "            \"accountId\": \"609231357994\",\n" +
            "            \"accessKeyId\": \"ASIAY3WIEZAVLAVDGX4A\",\n" +
            "            \"sessionContext\": {\n" +
            "                \"sessionIssuer\": {\n" +
            "                    \"type\": \"Role\",\n" +
            "                    \"principalId\": \""+PRINCIPAL_ID+"\",\n" +
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

    @BeforeEach
    public void setUp() {
        notificationDeSerializer = new DeSerializer<>(){};
        s3NotificationMessageDeSerializer = new DeSerializer<>(){};
        cloudTrailEventDeSerializer = new DeSerializer<>(){};
    }
    @Test
    public void testDeSerializationNotification() {
        Notification n = notificationDeSerializer.deserializeToObject(notification, Notification.class);
        String message  = n.getMessage();
        Assertions.assertTrue(message.contains("s3Bucket"));
        S3NotificationMessage s3Message = s3NotificationMessageDeSerializer.deserializeToObject(message, S3NotificationMessage.class);
        Assertions.assertEquals(BUCKET_NAME, s3Message.getBucket());
        Assertions.assertEquals(BUCKET_OBJECT, s3Message.getObjects().get(0));
    }

    @Test
    public void testDeSerializationCloudTrailEvent() {
            CloudTrailEvent event = cloudTrailEventDeSerializer.deserializeToObject(cloudTrailEvent, CloudTrailEvent.class);
            Assertions.assertEquals(PRINCIPAL_ID, event.getUserIdentity().getPrincipalId());
    }

}
