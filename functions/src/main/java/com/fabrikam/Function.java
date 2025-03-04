package com.fabrikam;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Optional;
import java.util.Scanner;

import org.json.JSONObject;
import java.net.URL;





public class Function {

    public Function() {
        this.secret = "gervaso";


    }

    public static final String secretNameKeySignalR = "keyForSignalR";
    public static final String storageAccountName = "excalifunstorage";
    public static final String secretNameBlobStorageAccount = "keyForBlobStorage";
    public static final String keyVaultUrl = "https://testkeyvault10000.vault.azure.net/";

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

        // try {
        //     context.getLogger().info("Java HTTP trigger processed a request.");

        //     ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().build();

        //     SecretClient secretClient = null;
        //     if(keySignalR == null || accountKeyBlobStorage == null) {
        //         secretClient = new SecretClientBuilder()
        //         .vaultUrl(keyVaultUrl)
        //         .credential(credential)
        //         // .credential(new ManagedIdentityCredentialBuilder().build())
        //         .buildClient();
        //     }

        //     if(keySignalR == null) {
        //         String secretValueForSignalR = secretClient.getSecret(secretNameKeySignalR).getValue();
        //         keySignalR = secretValueForSignalR;
        //     }

        //     if(accountKeyBlobStorage == null) {
        //         String secretValueForAzureBlobStorage = secretClient.getSecret(secretNameBlobStorageAccount).getValue();
        //         accountKeyBlobStorage = secretValueForAzureBlobStorage;
        //     }

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
        
        
        try {
            // 1. Ottenere il token di accesso dalla Managed Identity
            String token = getManagedIdentityToken();
            System.out.println("Access Token: " + token);

            // 2. Usare il token per recuperare un segreto da Key Vault
            String keyVaultName = "YOUR-KEYVAULT-NAME"; // Sostituisci con il tuo Key Vault
            String secretName = "YOUR-SECRET-NAME"; // Sostituisci con il nome del segreto
            String secretValue = getSecretFromKeyVault(keyVaultName, secretName, token);

            System.out.println("Secret Value: " + secretValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metodo per ottenere il token dalla Managed Identity
    public static String getManagedIdentityToken() throws IOException {
        String tokenUrl = "http://169.254.169.254/metadata/identity/oauth2/token"
                + "?api-version=2019-08-01"
                + "&resource=https://vault.azure.net";

        HttpURLConnection conn = (HttpURLConnection) new URL(tokenUrl).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Metadata", "true");
        
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Errore nel recupero del token: " + conn.getResponseCode());
        }

        // Leggere la risposta JSON
        Scanner scanner = new Scanner(conn.getInputStream());
        String response = scanner.useDelimiter("\\A").next();
        scanner.close();

        // Estrarre il token dalla risposta JSON
        JSONObject jsonResponse = new JSONObject(response);
        return jsonResponse.getString("access_token");
    }

    // Metodo per ottenere un segreto da Key Vault
    public static String getSecretFromKeyVault(String keyVaultName, String secretName, String accessToken) throws IOException {
        String secretUrl = "https://" + keyVaultName + ".vault.azure.net/secrets/" + secretName + "?api-version=7.3";

        HttpURLConnection conn = (HttpURLConnection) new URL(secretUrl).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Errore nel recupero del segreto: " + conn.getResponseCode());
        }

        // Leggere la risposta JSON
        Scanner scanner = new Scanner(conn.getInputStream());
        String response = scanner.useDelimiter("\\A").next();
        scanner.close();

        // Estrarre il valore del segreto dalla risposta JSON
        JSONObject jsonResponse = new JSONObject(response);
        return jsonResponse.getString("value");
    }
}