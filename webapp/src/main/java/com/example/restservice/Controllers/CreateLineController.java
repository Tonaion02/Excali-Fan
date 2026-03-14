package com.example.restservice.CreateLineController;

import com.example.restservice.serviceSignalR.SignalRMessage;

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
public class CreateLineController {

    public final BoardsRuntimeStorage boards;

    @Autowired
    public CreateLineController(BoardsRuntimeStorage boards) {
        this.boards = boards;
        System.out.println("boards: " + boards);
    }

    @PostMapping("/api/createLine")
    public void createLine(@RequestBody CreateLineCommand command) {
        try {        
            Board board = boards.boards.get(command.groupId);
            synchronized (board) {
                board.lines.add(command.line);

                if(command.userId.equals(board.hostUserId))
                {
                    board.instantLastMod = Instant.now();
                }

                System.out.println("number of lines: " + board.lines.size());
            }
            

            System.out.println("timestamp of last line: " + command.line.timestamp);
            String hubUrl = Keys.signalRServiceBaseEndpoint + "/api/v1/hubs/" + hubName + "/groups/" + command.groupId;
            String accessKey = generateJwt(hubUrl, command.userId);



            HttpResponse<String> response =  Unirest.post(hubUrl)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessKey)
                .body(new SignalRMessage("receiveCreateLine", new Object[] { command }))
                .asString();

            System.out.println("sendMessage: " + response.getStatus());
            System.out.println("sendMessage: " + response.getBody());
        } catch(RuntimeException e) {
            e.printStackTrace();
        }   
    }








    private String generateJwt(String audience, String userId) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        long expMillis = nowMillis + (30 * 30 * 1000);
        Date exp = new Date(expMillis);

        byte[] apiKeySecretBytes = Keys.keySignalR.getBytes(StandardCharsets.UTF_8);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        JwtBuilder builder = Jwts.builder()
            .setAudience(audience)
            .setIssuedAt(now)
            .setExpiration(exp)
            .signWith(signingKey);

        if (userId != null) {
            builder.claim("nameid", userId);
        }
        
        return builder.compact();
    }
}
