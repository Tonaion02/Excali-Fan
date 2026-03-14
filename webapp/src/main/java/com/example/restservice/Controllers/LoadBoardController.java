package com.example.restservice.LoadBoardController;

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
public class LoadBoardController {

    public final BoardsRuntimeStorage boards;

    @Autowired
    public LoadBoardController(BoardsRuntimeStorage boards) {
        this.boards = boards;
        System.out.println("boards: " + boards);
    }

    public static class RequestBodyBlobToLoad {
        public RequestBodyBlobToLoad(String blobName, String userId) {
            this.blobName = blobName;
            this.userId = userId;
        }

        public String getBlobName() {
            return blobName;
        }

        public void setBlobName(String blobName) {
            this.blobName = blobName;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserId() {
            return userId;
        }

        private String blobName;
        private String userId;
    }

    public static class LoadBoardResult {
        public LoadBoardResult(String boardSessionId, String boardJson) {
            this.boardSessionId = boardSessionId;
            this.boardJson = boardJson;
        }

        public String boardSessionId;
        public String boardJson;
    }

    // T: This private api is used to load the Blob of a Board
    // identified by its name. The api return the board formatted
    // like a json and then load it in the "remote boards"(boards stored 
    // in central memory of the Server).
    // T: WARNING remember to return the new BoardSessionId or find
    // another solution
    @PostMapping("/api/loadBoard")
    public LoadBoardResult loadBoard(@RequestHeader("Authorization") String accessToken, @RequestBody RequestBodyBlobToLoad requestBody) {
        
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
            return null;
        }
        System.out.println("email of user retrieved from token: " + email);
        // T: Retrieve email from token (END)
        
        
        
        String boardJson = null;
        
        BoardStorage boardStorage = new BoardStorage();
        boardJson = boardStorage.loadBoard(requestBody.blobName, email);



        // T: WARNING: you can substitute that with UserId(email) + timestamp
        // int numericBoardSessionId = Math.abs(ThreadLocalRandom.current().nextInt());
        // String boardSessionId = Integer.toString(numericBoardSessionId);
        String boardSessionId = BoardIdGenerator.generateIdBoard(requestBody.userId);



        // T: autojoin the new group (START)
        System.out.println("adding to group");

        String hubUrl = Keys.signalRServiceBaseEndpoint + "/api/v1/hubs/" + hubName + "/groups/" + boardSessionId + "/users/" + requestBody.userId;
        String accessKey = generateJwt(hubUrl, email);

        HttpResponse<String> response = Unirest.put(hubUrl)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + accessKey)
            .asString();

        System.out.println("addgroup: " + response.getStatus());
        System.out.println("addgroup: " + response.getBody());        
        // T: autojoin the new group (END)



        // T: parse the board in a board object from json (START)
        Board board = null;
        board = boardStorage.parseBoardFromJson(boardJson);

        if(board == null)
        {
            System.out.println("FAILED TO LOAD BOARD");
            return new LoadBoardResult(boardSessionId, boardJson);
        }
        // T: parse the board in a board object from json (END)

        

        // T: Set the right hostUserId
        board.setHostUserId(requestBody.userId);

        // T: Set the last instant of modification
        board.setInstantLastMod(Instant.now());

        // T: Add board to the collection of boards
        boards.boards.put(boardSessionId, board);

        
        System.out.println("Succsefully added board in the server");


        return new LoadBoardResult(boardSessionId, boardJson);
    }
}