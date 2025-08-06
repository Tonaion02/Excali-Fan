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
import com.nimbusds.jwt.SignedJWT;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.HttpRequestMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;





public class UploadBoard {

    public static final String secretNameKeySignalR = "keyForSignalR";
    public static final String storageAccountName = "excalifunstorage";
    public static final String secretNameBlobStorageAccount = "keyForBlobStorage";
    public static final String keyVaultUrl = "https://testkeyvault10000.vault.azure.net";

    private static final String containerName = "boardstorage";

    private static String secret;
    private static String accountKeyBlobStorage = null;

    public static class parameter {
        parameter() {

        }

        public String getBoardStorageId() {
            return boardStorageId;
        }

        public void setBoardStorageId(String boardStorageId) {
            this.boardStorageId = boardStorageId;
        }

        public String getBoardJson() {
            return boardJson;
        }

        public void setBoardJson(String boardJson) {
            this.boardJson = boardJson;
        }

        public String boardStorageId;
        public String boardJson;
    }


    
    @FunctionName("uploadBoard")
    public HttpResponseMessage run(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.POST},
            authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
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
        String connectionString = "DefaultEndpointsProtocol=https;AccountName=" + storageAccountName + ";AccountKey=" + accountKeyBlobStorage + ";EndpointSuffix=core.windows.net"; 

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        // T: Connect to Azure (END)

        System.out.println("Created client");
        // T: Check if the container exist (START)
        BlobContainerClient containerClient = serviceClient.getBlobContainerClient(containerName);
        
        if (!containerClient.exists()) {
            containerClient.create();
        }

        System.out.println("Created connection");
        // T: Check if the container exist (END)

        

        // T: Retrieve name of file and content of file (START)
        System.out.println("Started parsing");

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
        String boardJson = par.boardJson;
        // T: Retrieve name of file and content of file (END)
        


        // T: Board name validation (START)
        // T: Check if the name of the board is already used

        // T: NOTES: This is the code of the example of the documentation, and doesn't work, GG Microsoft (START)
        // context.getLogger().info("arrivato all recupero boards");   

        // ListBlobsOptions options = new ListBlobsOptions().setPrefix(email); // your prefix
        // PagedIterable<BlobItem> blobs = containerClient.listBlobs();

        // for (BlobItem blobItem : blobs) {
        //     context.getLogger().info("Blob: " + blobItem.getName());
        //     if (blobItem.getName().equals(par.boardStorageId)) {
        //         context.getLogger().info("SALAMEEEEEEEEEEEEE");
        //         return request.createResponseBuilder(HttpStatus.IM_USED)
        //             .body("name of the board already used")
        //             .build();
        //     }
        // }
        // T: NOTES: This is the code of the example of the documentation, and doesn't work, GG Microsoft (END)

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(new URI("https://rest-service-1735827345127.azurewebsites.net/api/listBoards"))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", loginToken)
                .header("Accept", "application/json")
                .GET()
                .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            context.getLogger().info("Response from API: " + response.body());

        } catch (Exception e) {
            context.getLogger().severe("Error calling API: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to call external API")
                .build();
        }
        // T: Board name validation (END)



        // T: Effectively upload the file (START)
        BlobClient blobClient = containerClient.getBlobClient(email + "/" + boardStorageId);
        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(boardJson.getBytes(StandardCharsets.UTF_8))) {
            blobClient.upload(dataStream, boardJson.length(), true);
        } catch(Exception e) {
            context.getLogger().info("Error: " + e.getMessage());
            for(Object o : e.getStackTrace()) {
                context.getLogger().info(o.toString());
            }
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage() + ": \n" + e.getStackTrace()).build();
        }
        // T: Effectively upload the file (END)

        return request.createResponseBuilder(HttpStatus.OK).body("default return").build();
    }
}