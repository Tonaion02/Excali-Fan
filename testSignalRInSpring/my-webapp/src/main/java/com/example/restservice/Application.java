package com.example.restservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {

        // T: URL of Azure Key Vault
        String keyVaultUrl = "https://testkeyvault10000.vault.azure.net/";

        // T: Create a SecretClient using DefaultAzureCredential
        SecretClient secretClient = new SecretClientBuilder()
                .vaultUrl(keyVaultUrl)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        // T: Retrieve the key for Azure SignalR
        String secretValueForSignalR = secretClient.getSecret(Keys.secretNameKeySignalR).getValue();
        Keys.keySignalR = secretValueForSignalR;

        System.out.println("Azure SignalR Key: " + secretValueForSignalR);

        SpringApplication.run(Application.class, args);
    }
}
