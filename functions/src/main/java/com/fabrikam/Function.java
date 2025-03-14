package com.fabrikam;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import reactor.core.publisher.Mono;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.core.credential.AccessToken;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Optional;
import java.util.Map;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;





public class Function {

    public Function() {
        this.secret = "gervaso";
    }

    public static final String secretNameKeySignalR = "keyForSignalR";
    public static final String storageAccountName = "excalifunstorage";
    public static final String secretNameBlobStorageAccount = "keyForBlobStorage";
    public static final String keyVaultUrl = "https://testkeyvault10000.vault.azure.net";

    private static final String containerName = "boardstorage";

    private static String secret;
    private static String keySignalR = null;
    private static String accountKeyBlobStorage = null;

    @FunctionName("HttpExample")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                // HttpRequestMessage<Optional<String>> request,
                HttpRequestMessage<Map<String, String>> request,
            final ExecutionContext context) {

        // try {
        //     context.getLogger().info("Java HTTP trigger processed a request.");

        //     ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().build();

        //     String resource = "https://vault.azure.net";
        //     // TokenRequestContext requestContext = new TokenRequestContext()
        //     //     .setScopes(Collections.singletonList(resource));
        //     TokenRequestContext requestContext = new TokenRequestContext()
        //         .setScopes(Collections.singletonList(resource));

        //     String token = credential.getToken(requestContext).block().getToken();
        //     context.getLogger().info("token: " + token);



        //     // SecretClient secretClient = null;
        //     // if(keySignalR == null || accountKeyBlobStorage == null) {
        //     //     secretClient = new SecretClientBuilder()
        //     //     .vaultUrl(keyVaultUrl)
        //     //     .credential(credential)
        //     //     // .credential(new ManagedIdentityCredentialBuilder().build())
        //     //     .buildClient();
        //     // }
        //     SecretClient secretClient = new SecretClientBuilder()
        //     .vaultUrl(keyVaultUrl)
        //     .credential(new TokenCredential() {
        //         @Override
        //         public Mono<AccessToken> getToken(TokenRequestContext request) {
        //             return Mono.just(new AccessToken(token, OffsetDateTime.now().plusHours(1)));
        //         }
        //     })
        //     .buildClient();

            

        //     if(keySignalR == null) {
        //         String secretValueForSignalR = secretClient.getSecret(secretNameKeySignalR).getValue();
        //         keySignalR = secretValueForSignalR;
        //     }

        //     // if(accountKeyBlobStorage == null) {
        //     //     String secretValueForAzureBlobStorage = secretClient.getSecret(secretNameBlobStorageAccount).getValue();
        //     //     accountKeyBlobStorage = secretValueForAzureBlobStorage;
        //     // }

        //     // Parse query parameter
        //     final String query = request.getQueryParameters().get("name");
        //     final String name = request.getBody().orElse(query);

        //     if (name == null) {
        //         return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        //     } else {
        //         return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name + " secret: " + secret).build();
        //     }
        // } catch(Exception e) {
        //     context.getLogger().info("Error: " + e.getMessage());
        //     for(Object o : e.getStackTrace()) {
        //         context.getLogger().info(o.toString());
        //     }
        //     return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage() + ": \n" + e.getStackTrace()).build();
        // }
        



        context.getLogger().info("Starting building BoardStorage");
        String connectionString = "DefaultEndpointsProtocol=https;AccountName=" + storageAccountName + ";AccountKey=" + accountKeyBlobStorage + ";EndpointSuffix=core.windows.net"; 

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        context.getLogger().info("PORCODIO!!!!!!!!!!!!!!");

        BlobContainerClient containerClient = serviceClient.getBlobContainerClient(containerName);
        
        if (!containerClient.exists()) {
            context.getLogger().info("PORCODIO2!!!!!!!!!!!!!!");
            containerClient.create();
        }
        
        context.getLogger().info("Ended building BoardStorage");
        
        String email = request.getBody().get("email");
        String boardStorageId = request.getBody().get("boardStorageId");
        String boardJson = request.getBody().get("boardJson");
        
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

        return request.createResponseBuilder(HttpStatus.OK).body("default return").build();
    }
}