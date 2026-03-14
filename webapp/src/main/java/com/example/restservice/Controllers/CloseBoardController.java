package com.example.restservice.CloseBoardController;

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
public class CloseBoardController {

    public final BoardsRuntimeStorage boards;

    @Autowired
    public CloseBoardController(BoardsRuntimeStorage boards) {
        this.boards = boards;
        System.out.println("boards: " + boards);
    }

    public static class RequestCloseBoard
    {
        private String groupId;
        private String userId;

        public RequestCloseBoard(){}

        public RequestCloseBoard(String groupId, String userId) {
            this.groupId = groupId;
            this.userId = userId;
        }

        public String getGroupId() {
            return this.groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getUserId() {
            return this.userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }

    @PostMapping("/api/closeBoard")
    public String closeBoard(@RequestBody RequestCloseBoard command) {

        // T: DEBUG
        System.out.println("Close the board");

        try {
            Board board = boards.boards.get(command.groupId);

            if(board != null) {
                synchronized(board) {
                    // T: DEBUG
                    System.out.println("Trying to close the board: " + command.groupId);
                    System.out.println("hostUserId: " + board.getHostUserId());
                    System.out.println("userId: " + command.userId);

                    if(command.userId.equals(board.getHostUserId()))
                    {
                        String hubUrl = Keys.signalRServiceBaseEndpoint + "/api/v1/hubs/" + hubName + "/groups/" + command.groupId;
                        String accessKey = generateJwt(hubUrl, command.userId);
    
                        HttpResponse<String> response =  Unirest.post(hubUrl)
                            .header("Content-Type", "application/json")
                            .header("Authorization", "Bearer " + accessKey)
                            .body(new SignalRMessage("receiveCloseBoard", new Object[] { command }))
                            .asString();
                        
                        // T: DEBUG
                        System.out.println("result of trying to send the receiveCloseBoard message: " + response.getStatus());
    
                        // T: Remove the board, because is disconnect
                        board = boards.boards.remove(command.groupId);

                        // T: TODO evaluate if it's useful to save the board like a temporary board...
                    }
                    else
                    {
                        // T: DEBUG
                        System.out.println("Failed to close the board: " + command.groupId);
                    }
                }
            }
            else
            {
                System.out.println("Not found the board: " + command.groupId);
            }
        } catch(RuntimeException e) {
            e.printStackTrace();
        }

        return command.userId;
    }
}