package com.example.restservice;

import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;





@SpringBootApplication
public class Application {

    public static void main(String[] args) {

        // T: Load from .env file some basic information (START)
        Dotenv dotenv = Dotenv.load();

        Keys.keyVaultUrl = dotenv.get("keyVaultUrl");
        System.out.println("keyVaultUrl: " + Keys.keyVaultUrl);
        Keys.signalRServiceBaseEndpoint = dotenv.get("signalRServiceBaseEndpoint");
        System.out.println("signalRServiceBaseEndpoint: " + Keys.signalRServiceBaseEndpoint);
        Keys.storageAccountName = dotenv.get("storageAccountName");
        System.out.println("storageAccountName: " + Keys.storageAccountName);
        // T: Load from .env file some basic information (END)



        // T: Create a SecretClient using DefaultAzureCredential
        SecretClient secretClient = new SecretClientBuilder()
                .vaultUrl(Keys.keyVaultUrl)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        // T: Retrieve the key for Azure SignalR (START)
        String secretValueForSignalR = secretClient.getSecret(Keys.secretNameKeySignalR).getValue();
        Keys.keySignalR = secretValueForSignalR;

        System.out.println("Azure SignalR Key: " + secretValueForSignalR);
        // T: Retrieve the key for Azure SignalR (END)

        

        // T: Retrieve the key for Azure Blob Storage (START)
        String secretValueForAzureBlobStorage = secretClient.getSecret(Keys.secretNameBlobStorageAccount).getValue();
        Keys.accountKeyBlobStorage = secretValueForAzureBlobStorage;

        System.out.println("Azure Blob Storage Key: " + secretValueForAzureBlobStorage);
        // T: Retrieve the key for Azure Blob Storage (END)

        

        var context = SpringApplication.run(Application.class, args);
    }
}