package com.amazon.parser.wrappers;

import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.AttachRolePolicyRequest;
import software.amazon.awssdk.services.iam.model.CreateRoleRequest;
import software.amazon.awssdk.services.iam.model.CreateRoleResponse;
import software.amazon.awssdk.services.iam.model.DeleteRoleRequest;
import software.amazon.awssdk.services.iam.model.GetRoleRequest;
import software.amazon.awssdk.services.iam.model.IamException;

public class IAMHandler {
    private final IamClient iam;
    public IAMHandler() {
        this.iam = IamClient.builder()
                .region(Region.AWS_GLOBAL)
                .httpClient(ApacheHttpClient.builder().build())
                .build();
    }

    public IamClient getIAMClient() { return this.iam; }
    public void createRole(String roleName, String rolePolicy) {
        CreateRoleRequest request = CreateRoleRequest.builder()
                .roleName(roleName)
                .assumeRolePolicyDocument(rolePolicy)
                .description("temp role to invoke legacy handler")
                .build();
        try {
            CreateRoleResponse response = iam.createRole(request);
            System.out.println("Role arn :" + response.role().arn());
        } catch (IamException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    public void attachRolePolicy(String roleName, String rolePolicyArn) {
        AttachRolePolicyRequest request = AttachRolePolicyRequest.builder()
                .roleName(roleName)
                .policyArn(rolePolicyArn)
                .build();
        try {
            iam.attachRolePolicy(request);
        } catch (IamException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    public String getRoleArnFromName(String roleName) {
        GetRoleRequest request = GetRoleRequest.builder()
                .roleName(roleName)
                .build();
        String roleArn = "";
        try {
            roleArn = iam.getRole(request).role().arn();
        } catch (IamException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return roleArn;
    }

    public void deleteRole(String roleName) {
        DeleteRoleRequest request = DeleteRoleRequest.builder()
                .roleName(roleName)
                .build();
        try {
            iam.deleteRole(request);
        } catch (IamException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }
}
