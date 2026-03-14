package com.example.restservice.LoginController;

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
public class LoginController {

    public final BoardsRuntimeStorage boards;

    @Autowired
    public LoginController(BoardsRuntimeStorage boards) {
        this.boards = boards;
        System.out.println("boards: " + boards);
    }

    public static class LoginRequest {
        private String userId;
    
        public LoginRequest() {}
    
        public LoginRequest(String userId) {
            this.userId = userId;
        }
    
        public String getUserId() {
            return userId;
        }
    
        public void setUserId(String userId) {
            this.userId = userId;
        }
    }

    // T: This api permits to make the login the first time during
    // a session of using the application.
    @PostMapping("/publicApi/login")
    public String Login(@RequestBody LoginRequest lr, HttpServletRequest request, HttpServletResponse response) {
                
        // T: verify if the token is valid (START)
        String loginToken = request.getHeader("Authorization");
        if(!TokenValidatorEntraId.validateToken(loginToken)) {
            System.out.println("Invalid token used to try login");
            response.setStatus(201);
            return "#ERROR";
        } else {
            System.out.println("Valid token used to try login");
            response.setStatus(200);
        }
        // T: verify if the token is valid (END)



        // T: Retrieve email from token (START)
        String email = null;
        try {
            SignedJWT signedJwt = SignedJWT.parse(loginToken);
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
        


        // T: generate a randomic identifier to identify the board
        // T: NOTE: we use this number to identify the board in persistence and like session
        // to exchange messages from clients
        // T: WARNING: you can substitute that with UserId(email) + timestamp
        // int randomNumericBoardId = Math.abs(ThreadLocalRandom.current().nextInt());
        // String boardId = Integer.toString(randomNumericBoardId);
        String boardId = BoardIdGenerator.generateIdBoard(lr.userId);
        


        Board board = new Board();
        board.setOwnerUserId(email);
        board.setHostUserId(lr.userId);
        board.setInstantLastMod(Instant.now());
        boards.boards.put(boardId, board);     

        

        return boardId;
    }
}