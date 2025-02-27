package com.example.restservice.BoardStorage;

import com.example.restservice.Keys;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;





// T: TODO This class must become a Bean that can be pooled
public class BoardStorage {

    private static String connectionString = null;
    private static final String containerName = "boardstorage";

    // T: These objects can't be static shared in the classes, it is
    // more useful to create a different object each time.
    private final BlobContainerClient containerClient;
    private final ObjectMapper objectMapper;

    public BoardStorage() {
        System.out.println("Starting building BoardStorage");

        // connectionString = "DefaultEndpointsProtocol=https;AccountName=TUO_ACCOUNT;AccountKey=TUO_KEY;EndpointSuffix=core.windows.net";
        // T: Build the connection string from the information of the storage account
        connectionString = "DefaultEndpointsProtocol=https;AccountName=" + Keys.storageAccountName + ";AccountKey=" + Keys.accountKeyBlobStorage + ";EndpointSuffix=core.windows.net"; 

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        System.out.println("PORCODIO!!!!!!!!!!!!!!");

        this.containerClient = serviceClient.getBlobContainerClient(containerName);
        
        if (!this.containerClient.exists()) {
            System.out.println("PORCODIO2!!!!!!!!!!!!!!");
            this.containerClient.create();
        }
        System.out.println("PORCODIO3!!!!!!!!!!!!!!");
        
        
        this.objectMapper = new ObjectMapper();
        System.out.println("Ended building BoardStorage");
    }

    // T: TEMPORARY to delete (START)
    public static class TestBlob {

        public TestBlob() {
            
        }

        public String getHello() {
            return hello;
        }
        public void setHello(String hello) {
            this.hello = hello;
        }

        private String hello;
    }

    public TestBlob loadTestBlob(String blobName) {
        System.out.println("Starting loading blob from BoardStorage");

        BlobClient blobClient = containerClient.getBlobClient(blobName);
        if (!blobClient.exists()) {
            throw new RuntimeException("The blob with name: " + blobName + " doesn't exist");
        }

        System.out.println("Starting with json");

        String json = new String(blobClient.downloadContent().toBytes(), StandardCharsets.UTF_8);
        TestBlob result = null;
        try {
            result = objectMapper.readValue(json, TestBlob.class);
        } catch(Exception e) {
            System.out.println("Error in blobstorage: " + e.getMessage());
        }
        return result;
    }
    // T: TEMPORARY to delete (END)

    public String loadBlob(String blobName) {

        BlobClient blobClient = containerClient.getBlobClient(blobName);
        if (!blobClient.exists()) {
            throw new RuntimeException("The blob with name: " + blobName + " doesn't exist");
        }

        String blob = new String(blobClient.downloadContent().toBytes(), StandardCharsets.UTF_8);

        return blob;
    }

    public void saveBlob(String blobName, String dataToSave) throws Exception {
        String json = objectMapper.writeValueAsString(dataToSave);
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
            blobClient.upload(dataStream, json.length(), true);
        }
    }
}