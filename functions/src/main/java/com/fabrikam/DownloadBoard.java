package com.fabrikam;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobDownloadContentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.nimbusds.jwt.SignedJWT;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.azure.core.util.BinaryData;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fabrikam.Constants; 





public class DownloadBoard {
        
    // public static final String storageAccountName = "excalifunstorage";
    // public static final String secretNameBlobStorageAccount = "keyForBlobStorage";
    // public static final String keyVaultUrl = "https://testkeyvault10000.vault.azure.net";

    // private static final String containerName = "boardstorage";

    // private static final String accountKeyBlobStorage = null;

    public static class parameter {
        parameter() {}

        public String getBoardStorageId() {
            return boardStorageId;
        }

        public void setBoardStorageId(String boardStorageId) {
            this.boardStorageId = boardStorageId;
        }

        public String boardStorageId;
    }



    @FunctionName("downloadBoard")
    public HttpResponseMessage run(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.POST},
            authLevel = AuthorizationLevel.ANONYMOUS)
        HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context       
    ) {
        // T: Token validation (START)
        String loginToken = request.getHeaders().get("authorization");
        for(String key : request.getHeaders().keySet()) {   
            context.getLogger().info("header: " + key);
        }
        if(loginToken == null || loginToken.isEmpty() || !TokenValidatorEntraId.validateToken(loginToken, context)) {
            context.getLogger().info("Invalid token:" + loginToken);
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Error: invalid token").build();
        }
        else {
            context.getLogger().info("Valid token");
        }
        // T: Token validation (END)

        // T: retrieve email from token (START)
        String email = null;
        try {
            SignedJWT signedJwt = SignedJWT.parse(loginToken);
            email = signedJwt.getJWTClaimsSet().getStringClaim("email");
        } catch(Exception e) {
            context.getLogger().info("signedJwt exception: " + e.getMessage());
            for(Object o : e.getStackTrace()) {
                context.getLogger().info(o.toString());
            }            
        }
        if(email == null) {
            context.getLogger().info("email retrieved is null");
            return null;
        }
        context.getLogger().info("email of user retrieved from token: " + email);
        // T: retrieve email from token (END)
        


        String connectionString = "DefaultEndpointsProtocol=https;AccountName=" + Constants.storageAccountName + ";AccountKey=" + Constants.accountKeyBlobStorage + ";EndpointSuffix=core.windows.net"; 

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        // T: DEBUG
        context.getLogger().info("Here 1");

        BlobContainerClient containerClient = serviceClient.getBlobContainerClient(Constants.containerName);

        // T: DEBUG
        context.getLogger().info("Here 2");

        if(!containerClient.exists()) {
            containerClient.create();
        }

        String bodyJson = request.getBody().get();
        ObjectMapper objectMapper = new ObjectMapper();
        parameter par = null;
        try {
            par = objectMapper.readValue(bodyJson, parameter.class);
        } catch(Exception e) {
            context.getLogger().info("Error: " + e.getMessage());
            for(Object o : e.getStackTrace()) {
                context.getLogger().info(o.toString());
            }
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage() + ": \n" + e.getStackTrace()).build();            
        }
        String boardStorageId = par.boardStorageId;

        byte[] fileData = null;
        try {
            BlobClient blobClient = containerClient.getBlobClient(email + "/" + boardStorageId);
            BinaryData binaryData = blobClient.downloadContent();
            fileData = binaryData.toBytes(); 
        } catch(Exception e) {
            context.getLogger().info("Error: " + e.getMessage());
            for(Object o : e.getStackTrace()) {
                context.getLogger().info(o.toString());
            }
        }

        return request.
        createResponseBuilder(HttpStatus.OK).
        body(fileData).
        header("Content-Type", "application/octet-stream").
        header("Content-Disposition", "attachment; filename=" + boardStorageId).
        build();
    }
}