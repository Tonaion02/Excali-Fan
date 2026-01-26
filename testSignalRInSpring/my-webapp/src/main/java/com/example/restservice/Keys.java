package com.example.restservice;

public class Keys {
    public static String keyVaultUrl = "";
    public static String signalRServiceBaseEndpoint = "";
    public static String storageAccountName = "";
    
    public static String secretNameBlobStorageAccount = "keyForBlobStorage";
    public static String secretNameKeySignalR = "keyForSignalR";

    public static String keySignalR = null; 
    public static String accountKeyBlobStorage = null;

    // T: URL to retrieve public keys of Azure AD v1.0
    public static final String JWKS_URL_V1 = "https://login.microsoftonline.com/common/discovery/v2.0/keys";
    // T: Client ID of the Application
    public static final String EXPECTED_AUDIENCE = "b1453203-8719-4a2a-8cc6-96bf883a7e65";
}