package com.example.restservice.NewBoardController;

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
public class NewBoardController {

    public final BoardsRuntimeStorage boards;

    @Autowired
    public NewBoardController(BoardsRuntimeStorage boards) {
        this.boards = boards;
        System.out.println("boards: " + boards);
    }

    public static class RequestNewBoard {
        public String userId;

        public RequestNewBoard() {}

        public RequestNewBoard(String userId) {
            this.userId = userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserId() {
            return userId;
        }
    }

    // T: TODO Here, the problem is that you need to close the old board before opening another. Probably, is possible
    // to do this, re-communicating even the information like the session of the current board.
    @PostMapping("/api/newBoard")
    public String newBoard(@RequestHeader("Authorization") String accessToken, @RequestBody RequestNewBoard request) {
        // T: DEBUG
        System.out.println("New board");

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
            System.out.println("email retrieved from token is null");
            return null;
        }
        System.out.println("email of user retrieved from token: " + email);
        // T: Retrieve email from token (END)

        // T: Create the new board
        // int randomNumericBoardId = Math.abs(ThreadLocalRandom.current().nextInt());
        // String boardId = Integer.toString(randomNumericBoardId);
        String boardId = BoardIdGenerator.generateIdBoard(request.userId);

        // T: Autojoin a new group (START)
        System.out.println("adding to group");

        String hubUrl = Keys.signalRServiceBaseEndpoint + "/api/v1/hubs/" + hubName + "/groups/" + boardId + "/users/" + request.userId;
        String accessKey = generateJwt(hubUrl, email);

        HttpResponse<String> response = Unirest.put(hubUrl)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + accessKey)
            .asString();

        System.out.println("addgroup: " + response.getStatus());
        System.out.println("addgroup: " + response.getBody());
        // T: Autojoin a new group (END)

        Board board = new Board();
        board.setOwnerUserId(email);
        board.setHostUserId(request.userId);
        board.setInstantLastMod(Instant.now());
        boards.boards.put(boardId, board);

        return boardId;
    }
}