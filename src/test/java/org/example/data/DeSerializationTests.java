package org.example.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class DeSerializationTests {
    String notification  = "{\n" +
            "    \"Type\" : \"Notification\",\n" +
            "    \"MessageId\" : \"b118b3d6-0d76-5532-b9ca-ed1e49e61040\",\n" +
            "    \"TopicArn\" : \"arn:aws:sns:us-west-2:609231357994:ct-setup-us-west-2-stack-TrailTopic-rMlaN8Hw31nb\",\n" +
            "    \"Message\" : \"{\\\"s3Bucket\\\":\\\"ct-setup-us-west-2-stack-trailbucket-otrb89kxachb\\\",\\\"s3ObjectKey\\\":[\\\"HandlerInvocations/AWSLogs/609231357994/CloudTrail/us-west-2/2024/05/22/609231357994_CloudTrail_us-west-2_20240522T1905Z_4q25LggTW2uvVIh9.json.gz\\\"]}\",\n" +
            "    \"Timestamp\" : \"2024-05-22T19:06:56.984Z\",\n" +
            "    \"SignatureVersion\" : \"1\",\n" +
            "    \"Signature\" : \"di+LzDT0vvSd1cFXJojCcv0gk8hvmp1pV9X3KYAQny1Igdx12Ga3eaS6BinHbAjT5zjI2NTNamckNuftfu9bB+V/jJOFFah/c9KFrlrGggpoJAF5mgUgnEd7RTe8C16AhbdzSv2Lh8ub5pqesXwjRQCCWttiJZYoauTfSjGyvhI7l0QFyazRTSN1bOKQ/Q+kLIApswlLh1hmbQXS4Mmr/Zd4NE2g/M72/i4Jk1hb85zkz3UtT/ZwlLd1FpfO1vBc9sK/Udi9XKWud31P3RPudfP2If75XM9TVijopgf0UlsZBFjiGc8s6o/T+GWTBuBgqODPI8ufmRFN2CKtJlaNPg==\",\n" +
            "    \"SigningCertURL\" : \"https://sns.us-west-2.amazonaws.com/SimpleNotificationService-60eadc530605d63b8e62a523676ef735.pem\",\n" +
            "    \"UnsubscribeURL\" : \"https://sns.us-west-2.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-west-2:609231357994:ct-setup-us-west-2-stack-TrailTopic-rMlaN8Hw31nb:f644181e-62b3-49a3-b13a-4d15b5ee3a4a\"\n" +
            "  }";
    @Test
    public void testDeSerialization() {
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

}
