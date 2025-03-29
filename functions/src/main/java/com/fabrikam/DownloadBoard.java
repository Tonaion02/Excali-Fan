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
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.HttpRequestMessage;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DownloadBoard {
        
    public static final String storageAccountName = "excalifunstorage";
    public static final String secretNameBlobStorageAccount = "keyForBlobStorage";
    public static final String keyVaultUrl = "https://testkeyvault10000.vault.azure.net";

    private static final String containerName = "boardstorage";

    private static final String accountKeyBlobStorage = null;



    public static class parameter {
        parameter() {

        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getBoardStorageId() {
            return boardStorageId;
        }

        public void setBoardStorageId(String boardStorageId) {
            this.boardStorageId = boardStorageId;
        }

        public String email;
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
        


        String connectionString = "DefaultEndpointsProtocol=https;AccountName=" + storageAccountName + ";AccountKey=" + accountKeyBlobStorage + ";EndpointSuffix=core.windows.net"; 

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        BlobContainerClient containerClient = serviceClient.getBlobContainerClient(containerName);

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
        String email = par.email;
        String boardStorageId = par.boardStorageId;



        BlobClient blobClient = containerClient.getBlobClient(email + "/" + boardStorageId);
        byte[] fileData = blobClient.downloadContent().toBytes();



        return request.
        createResponseBuilder(HttpStatus.OK).
        body(fileData).
        header("Content-Type", "application/octet-stream").
        header("Content-Disposition", "attachment; filename=" + boardStorageId).
        build();
    }
}