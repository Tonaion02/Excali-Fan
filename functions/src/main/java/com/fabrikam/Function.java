package com.fabrikam;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

import java.util.Optional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;






public class Function {

    public Function() {
        this.secret = "gervaso";


    }

    public static final String secretNameKeySignalR = "keyForSignalR";
    public static final String storageAccountName = "excalifunstorage";
    public static final String secretNameBlobStorageAccount = "keyForBlobStorage";
    public static final String keyVaultUrl = "https://testkeyvault10000.vault.azure.net";

    private static String secret;
    private static String keySignalR = null;
    private static String accountKeyBlobStorage = null;

    @FunctionName("HttpExample")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        try {
            context.getLogger().info("Java HTTP trigger processed a request.");

            // String urlString = "https://vault.keyvault.net";
            // URL url = new URL(urlString);

            // HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // connection.setRequestMethod("GET");
            
            // int responseCode = connection.getResponseCode();
            // context.getLogger().info("Response code: " + responseCode);

            // if (responseCode == HttpURLConnection.HTTP_OK) { // HTTP 200
            //     BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            //     String inputLine;
            //     StringBuffer response = new StringBuffer();
                
            //     while ((inputLine = in.readLine()) != null) {
            //         response.append(inputLine);
            //     }
            //     in.close();
                
            //     // Print the response
            //     // System.out.println("Response: " + response.toString());
            //     context.getLogger().info("Response: " + response.toString());
            // } else {
            //     context.getLogger().info("GET request failed. Response Code: " + responseCode);
            //     // System.out.println("GET request failed. Response Code: " + responseCode);
            // }

            // connection.disconnect();


        // Crea le credenziali
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
            .clientId(System.getenv("AZURE_CLIENT_ID"))
            .clientSecret(System.getenv("AZURE_CLIENT_SECRET"))
            .tenantId(System.getenv("AZURE_TENANT_ID"))
            .build();


            //ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().build();

            SecretClient secretClient = null;
            if(keySignalR == null || accountKeyBlobStorage == null) {
                secretClient = new SecretClientBuilder()
                .vaultUrl(keyVaultUrl)
                .credential(credential)
                // .credential(new ManagedIdentityCredentialBuilder().build())
                .buildClient();
            }

            if(keySignalR == null) {
                String secretValueForSignalR = secretClient.getSecret(secretNameKeySignalR).getValue();
                keySignalR = secretValueForSignalR;
            }

            if(accountKeyBlobStorage == null) {
                String secretValueForAzureBlobStorage = secretClient.getSecret(secretNameBlobStorageAccount).getValue();
                accountKeyBlobStorage = secretValueForAzureBlobStorage;
            }

            // Parse query parameter
            final String query = request.getQueryParameters().get("name");
            final String name = request.getBody().orElse(query);

            if (name == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
            } else {
                return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name + " secret: " + secret).build();
            }
        } catch(Exception e) {
            context.getLogger().info("Error: " + e.getMessage());
            for(Object o : e.getStackTrace()) {
                context.getLogger().info(o.toString());
            }
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage() + ": \n" + e.getStackTrace()).build();
        } 
    }
}