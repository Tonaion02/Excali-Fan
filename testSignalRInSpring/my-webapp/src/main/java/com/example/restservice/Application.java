package com.example.restservice;

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
        
        var context = SpringApplication.run(Application.class, args);



        // T: Retrieve the key for Azure SignalR (START)

        // T: URL of Azure Key Vault
        String keyVaultUrl = "https://testkeyvault10000.vault.azure.net/";

        // T: Create a SecretClient using DefaultAzureCredential
        SecretClient secretClient = new SecretClientBuilder()
                .vaultUrl(keyVaultUrl)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        // T: Get secret from Key Vault
        String secretValueForSignalR = secretClient.getSecret(Keys.secretNameKeySignalR).getValue();
        Keys.keySignalR = secretValueForSignalR;

        System.out.println("Azure SignalR Key: " + secretValueForSignalR);
        // T: Retrieve the key for Azure SignalR (END)



        // T: Create interceptor to test if the user is logged (START)
        HandlerInterceptor customInterceptor = new TokenValidatorInterceptor();
        WebMvcConfigurer configurer = new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(customInterceptor).addPathPatterns("/**");
            }
        };
        
        context.getBeanFactory().registerSingleton("customWebConfig", configurer);
        // T: Create interceptor to test if the user is logged (END)
    }
}
