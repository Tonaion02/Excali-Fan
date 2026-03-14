package com.example.restservice.SaveBoardController;

import com.example.restservice.serviceSignalR.SignalRMessage;
import static com.example.restservice.serviceSignalR.GenerateJwt.generateJwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.concurrent.ConcurrentHashMap;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.util.concurrent.ThreadLocalRandom;

import com.example.restservice.BoardStorage.BoardStorage;
import com.example.restservice.BoardStorage.BoardStorage.TestBlob;
import com.azure.core.annotation.Post;
import com.example.restservice.Keys;
import static com.example.restservice.Keys.hubName;
import com.example.restservice.TokenValidatorEntraId;
import com.example.restservice.Board.*;

import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;

import java.time.Instant;





@RestController
public class SaveBoardController {

    public final BoardsRuntimeStorage boards;

    @Autowired
    public SaveBoardController(BoardsRuntimeStorage boards) {
        this.boards = boards;
        System.out.println("boards: " + boards);
    }

    public static class RequestBodyBlobToSave {
        public RequestBodyBlobToSave(String blobName, String email, String boardSessionId, String precBoardStorageId) {
            this.blobName = blobName;
            this.email = email;
            this.boardSessionId = boardSessionId;
            this.precBoardStorageId = precBoardStorageId;
        }

        public RequestBodyBlobToSave() {

        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getEmail() {
            return email;
        }

        public void setBlobName(String blobName) {
            this.blobName = blobName;
        }

        public String getBlobName() {
            return blobName;
        }

        public void setBoardSessionId(String boardSessionId) {
            this.boardSessionId = boardSessionId;
        }

        public String getBoardSessionId() {
            return boardSessionId;
        }

        public String getPrecBoardStorageId() {
            return precBoardStorageId;
        }

        public void setPrecBoardStorageId(String precBoardStorageId) {
            this.precBoardStorageId = precBoardStorageId;
        }

        public String blobName;
        public String email;
        public String boardSessionId;
        public String precBoardStorageId;
    }

    // T: This private api is used to persist the replica of Board
    // that are saved on server on the Blob Storage.
    @PostMapping("/api/saveBoard")
    public void saveBoard(@RequestHeader("Authorization") String accessToken, @RequestBody RequestBodyBlobToSave requestBody, HttpServletResponse response) {

        System.out.println("precBlobName: " + requestBody.precBoardStorageId);
        System.out.println("blobName: " + requestBody.blobName);

        // T: Retrieve email from token (START)
        String email = null;
        try {
            SignedJWT signedJwt = SignedJWT.parse(accessToken);
            email = signedJwt.getJWTClaimsSet().getStringClaim("email");
        } catch(Exception e) {
            System.out.println("signedJwt exception: " + e.getMessage());
            e.printStackTrace();
        }
        if(email == null) {
            System.out.println("email retrieved is null");
        }
        System.out.println("email of user retrieved from token: " + email);
        // T: Retrieve email from token (END)



        Board board = boards.boards.get(requestBody.boardSessionId);



        // T: Check if the UserId is the owner (START)
        if(board.getOwnerUserId() == null || ! board.getOwnerUserId().equals(email)) {
            System.out.println("You: " + email + " don't have permission to save the board");
            response.setStatus(210);
            return;
        }
        // T: Check if the UserId is the owner (END)



        BoardStorage boardStorage = new BoardStorage();



        // T: Check if the boardStorageId collides with the boardStorageId of an existing board (START)
        if(! requestBody.precBoardStorageId.equals(requestBody.blobName)) {
            try {
                List<String> listOfBoards = boardStorage.listBoards(email);
                for(var boardStorageId : listOfBoards) {
                    if(boardStorageId.equals(requestBody.blobName)) {
                        System.out.println("The boardStorageId: " + boardStorageId + " is already in use");
                        response.setStatus(211);
                        return;
                    }
                }
            } catch(Exception e) {
                System.out.println("Error in retrieving the list of boards:");
                e.printStackTrace();            
            }
        }
        // T: Check if the boardStorageId collides with the boardStorageId of an existing board (END)



        try {
            boardStorage.saveBoard(requestBody.blobName, requestBody.precBoardStorageId, email, board);
        } catch(Exception e) {
            System.out.println("Error during saving of Board: " + e.getMessage());
            e.printStackTrace();

            response.setStatus(201);
        }
    }
}