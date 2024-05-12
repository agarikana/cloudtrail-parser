package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * to generate all the permissions required for
 * a legacy handler
 */
public class HandlerPermissionGetter {

    public static String IAM_ROLE_POLICY = "{\n" +
            "\t\"Version\": \"2012-10-17\",\n" +
            "\t\"Statement\": [\n" +
            "\t\t{\n" +
            "\t\t\t\"Effect\": \"Allow\",\n" +
            "\t\t\t\"Action\": \"*\",\n" +
            "\t\t\t\"Resource\": \"*\"\n" +
            "\t\t}\n" +
            "\t]\n" +
            "}";

    public static String IAM_TRUST_POLICY = "{\n" +
            "    \"Version\": \"2012-10-17\",\n" +
            "    \"Statement\": [\n" +
            "        {\n" +
            "            \"Sid\": \"\",\n" +
            "            \"Effect\": \"Allow\",\n" +
            "            \"Principal\": {\n" +
            "                \"AWS\": \"arn:aws:iam::727820809195:root\"\n" +
            "            },\n" +
            "            \"Action\": \"sts:AssumeRole\",\n" +
            "            \"Condition\": {\n" +
            "                \"StringEquals\": {\n" +
            "                    \"sts:ExternalId\": \"IsengardExternalIdta5BY4Vq7zo1\"\n" +
            "                }\n" +
            "            }\n" +
            "        },\n" +
            "        {\n" +
            "            \"Sid\": \"LambdaAssumeRole\",\n" +
            "            \"Effect\": \"Allow\",\n" +
            "            \"Principal\": {\n" +
            "                \"Service\": \"lambda.amazonaws.com\"\n" +
            "            },\n" +
            "            \"Action\": \"sts:AssumeRole\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"Sid\": \"UserAssumeRole\",\n" +
            "            \"Effect\": \"Allow\",\n" +
            "            \"Principal\": {\n" +
            "                \"AWS\": \"arn:aws:iam::609231357994:root\"\n" +
            "            },\n" +
            "            \"Action\": \"sts:AssumeRole\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";
    public static String IAM_ROLE_NAME = "handler-permissions-getter";
    public static String SESSION_NAME = "my-session";
    public static String IAM_ROLE_POLICY_ARN = "arn:aws:iam::aws:policy/AdministratorAccess";
    public static String REGION = "us-east-1";
    public static String TEMPLATE_FILE_NAME = "Bucket.json";
    public static String STACK_TEMPLATE = new BufferedReader(new InputStreamReader(
            HandlerPermissionGetter.class.getClassLoader().getResourceAsStream(TEMPLATE_FILE_NAME),
            StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));


    public static void main(String[] args) {
//        IAMHandler iamHandler = new IAMHandler();
        CloudTrailEventSelector ctEventSelector = new CloudTrailEventSelector(REGION);
        CloudTrailEventSelector.getAllAPICalls(ctEventSelector.byUserName(SESSION_NAME).select())
                .forEach(System.out::println);
//        //iamHandler.createRole(IAM_ROLE_NAME, IAM_TRUST_POLICY);
//        //iamHandler.attachRolePolicy(IAM_ROLE_NAME, IAM_ROLE_POLICY_ARN);
//        String roleArn = iamHandler.getRoleArnFromName(IAM_ROLE_NAME);
//        StackHandler stackHandler = new StackHandler(REGION, roleArn);
//        stackHandler.createStack("test-ct-bucket1", STACK_TEMPLATE);
//        ctEventSelector.byRoleName(IAM_ROLE_NAME).select();
//        stackHandler.deleteStack("test-ct-bucket1");
    }
}
