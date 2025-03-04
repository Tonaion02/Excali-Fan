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
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

import java.util.Optional;





public class Function {

    public Function() {
        this.secret = "gervaso";

        SecretClient secretClient = null;
        if(keySignalR == null || accountKeyBlobStorage == null) {
            secretClient = new SecretClientBuilder()
            .vaultUrl(keyVaultUrl)
            .credential(new DefaultAzureCredentialBuilder().build())
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
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        final String query = request.getQueryParameters().get("name");
        final String name = request.getBody().orElse(query);

        if (name == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name + " secret: " + this.secret).build();
        }
    }

    @FunctionName("otherfunction")
    public HttpResponseMessage otherfunction(
        @HttpTrigger(
            name="req2",
            methods = {HttpMethod.GET, HttpMethod.POST},
            authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request");

        // T: Parse request (START)
        final String query = request.getQueryParameters().get("parameter");
        final String parameter = request.getBody().orElse(query);
        // T: Parse request (END)
        
        return request.createResponseBuilder(HttpStatus.OK).body("parameter: " + parameter + " signalR: " + keySignalR + " blob: " + accountKeyBlobStorage).build();
    }
}
