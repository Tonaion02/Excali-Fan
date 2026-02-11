package com.fabrikam;

import com.azure.core.http.rest.PagedIterable;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.nimbusds.jose.shaded.ow2asm.ConstantDynamic;
import com.nimbusds.jwt.SignedJWT;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.HttpRequestMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import com.fabrikam.Constants;





public class DeleteBoard {

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



    
    @FunctionName("deleteBoard")
    public HttpResponseMessage run(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.POST},
            authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        // T: DEBUG
        context.getLogger().info("Starting deleteing board");
        
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
            System.out.println("signedJwt exception: " + e.getMessage());
            e.printStackTrace();
        }
        if(email == null) {
            System.out.println("email retrieved is null");
            return null;
        }
        System.out.println("email of user retrieved from token: " + email);
        // T: retrieve email from token (END)



        // T: Connect to Azure (START)
        System.out.println("Arrived to connection");
        String connectionString = "DefaultEndpointsProtocol=https;AccountName=" + Constants.storageAccountName + ";AccountKey=" + Constants.accountKeyBlobStorage + ";EndpointSuffix=core.windows.net"; 

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        // T: Connect to Azure (END)

        System.out.println("Created client");

        // T: Check if the container exist (START)
        BlobContainerClient containerClient = serviceClient.getBlobContainerClient(Constants.containerName);
        
        if (!containerClient.exists()) {
            // containerClient.create();
            context.getLogger().info("container not created");
        }

        System.out.println("Created connection");
        // T: Check if the container exist (END)

        

        // T: Retrieve name of file from request (START)
        System.out.println("Started parsing the request");

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

        System.out.println("boardStorageId of board to delete: " + boardStorageId);
        // T: Retrieve name of file from request (END)
        

        // T: Delete the board (START)
        try {
            BlobClient blobClient = containerClient.getBlobClient(email + "/" + boardStorageId);

            boolean deleted = blobClient.deleteIfExists();
        
            if(deleted)
            {
                // T: DEBUG
                System.out.println("Board deleted");
            }
            else
            {
                // T: DEBUG
                System.out.println("Board doesn't exist");
            }
        }
        catch(Exception e) {
            context.getLogger().info(e.getMessage());
        }
        // T: Delete the board (END)

        return request.createResponseBuilder(HttpStatus.OK).body("OK deleting board").build();
    }
}