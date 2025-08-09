package com.example.restservice;

public class Keys {
    // public static final String keyVaultUrl = "testkeyvault10000";
    public static final String keyVaultUrl = "https://keyvaultexcalifan.vault.azure.net/";
    // public static final String signalRServiceBaseEndpoint = "https://signalrresourceforspring.service.signalr.net"; // T: TODO: substitute_constant
    public static final String signalRServiceBaseEndpoint = "https://excalifansignalr.service.signalr.net";


    public static String keySignalR = null; 
    public static final String secretNameKeySignalR = "keyForSignalR";     
    public static final String storageAccountName = "excalifunstorage"; // T: TODO substitute_constant
    public static final String secretNameBlobStorageAccount = "keyForBlobStorage";
    public static String accountKeyBlobStorage = null;
}