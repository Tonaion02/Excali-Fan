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
    private static final String containerName = "board-storage";

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
        System.out.println("PORCODIO2!!!!!!!!!!!!!!");
        if (!this.containerClient.exists()) {
            this.containerClient.create();
        }
        System.out.println("PORCODIO3!!!!!!!!!!!!!!");
        
        
        this.objectMapper = new ObjectMapper();
        System.out.println("Ended building BoardStorage");
    }

    public String loadBlob(String blobName) {
        System.out.println("Starting loading blob from BoardStorage");

        BlobClient blobClient = containerClient.getBlobClient(blobName);
        if (!blobClient.exists()) {
            throw new RuntimeException("The blob with name: " + blobName + " doesn't exist");
        }

        System.out.println("Starting with json");

        String json = new String(blobClient.downloadContent().toBytes(), StandardCharsets.UTF_8);
        String result = null;
        try {
            result = objectMapper.readValue(json, String.class);
        } catch(Exception e) {
            System.out.println("Error in blobstorage: " + e.getMessage());
        }
        return result;
    }

    // public void saveBoard(Board board, String blobName) throws Exception {
    //     String json = objectMapper.writeValueAsString(board);
    //     BlobClient blobClient = containerClient.getBlobClient(blobName);

    //     try (ByteArrayInputStream dataStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
    //         blobClient.upload(dataStream, json.length(), true);
    //     }
    // }

    // public Board loadBoard(String blobName) throws Exception {
    //     BlobClient blobClient = containerClient.getBlobClient(blobName);
    //     if (!blobClient.exists()) {
    //         throw new RuntimeException("Il blob non esiste!");
    //     }

    //     String json = new String(blobClient.downloadContent().toBytes(), StandardCharsets.UTF_8);
    //     return objectMapper.readValue(json, Board.class);
    // }

    // public static void main(String[] args) throws Exception {
    //     BoardStorage storage = new BoardStorage();

    //     // Creazione oggetto di test
    //     List<Pair> points = List.of(new Pair(10, 20), new Pair(30, 40));
    //     List<Line> lines = List.of(new Line("red", points, "user123", System.currentTimeMillis()));
    //     Board board = new Board();
    //     board.lines.addAll(lines);

    //     // Salvataggio e recupero
    //     storage.saveBoard(board, "board1.json");
    //     Board loadedBoard = storage.loadBoard("board1.json");

    //     System.out.println("Board caricata: " + loadedBoard.lines.size() + " linee");
    // }
}