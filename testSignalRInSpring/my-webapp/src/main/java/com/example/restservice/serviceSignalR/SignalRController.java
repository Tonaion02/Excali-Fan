package com.example.restservice.serviceSignalR;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.HttpHeaders;
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

import java.util.concurrent.ConcurrentHashMap;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.util.concurrent.ThreadLocalRandom;

import com.example.restservice.Board.BoardsRuntimeStorage;
import com.example.restservice.Board.Board;
import com.example.restservice.Board.Command;
import com.example.restservice.Keys;
import com.example.restservice.Board.*;

/**
 * SignalRController
 */
@RestController
public class SignalRController {

    private final BoardsRuntimeStorage boards;

    @Autowired
    public SignalRController(BoardsRuntimeStorage boards) {
        this.boards = boards;
    }

    // https://foo.service.signalr.net
    private String signalRServiceBaseEndpoint = "https://signalrresourceforspring.service.signalr.net";
    private String hubName = "board";

    @PostMapping("/signalr/negotiate")
    public SignalRConnectionInfo negotiate(@RequestParam String userId) {

        String hubUrl = signalRServiceBaseEndpoint + "/client/?hub=" + hubName;
        System.out.println("UserID: " + userId);
        String accessKey = generateJwt(hubUrl, userId);

        // T: WARNING temporary, we are adding the new board
        // to the global hashmap
        boards.boards.put(userId, new Board());        

        return new SignalRConnectionInfo(hubUrl, accessKey);
    }

    @PostMapping("/api/deleteLine")
    public void deleteLine(@RequestBody DeleteLineCommand command) {

        Board board = boards.boards.get(command.groupId);
        synchronized(board) {
            Line fakeLine = new Line("null", null, command.userIdOfLine, command.timestampOfLine);
            board.lines.remove(fakeLine);    
        }



        String hubUrl = signalRServiceBaseEndpoint + "/api/v1/hubs/" + hubName + "/groups/" + command.groupId;
        String accessKey = generateJwt(hubUrl, command.userId);



        HttpResponse<String> response =  Unirest.post(hubUrl)
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer " + accessKey)
        .body(new SignalRMessage("receiveDeleteLine", new Object[] { command }))
        .asString();

        System.out.println("sendMessage: " + response.getStatus());
        System.out.println("sendMessage: " + response.getBody());
    }

    @PostMapping("/api/createLine")
    public void createLine(@RequestBody CreateLineCommand command) {
        Board board = boards.boards.get(command.groupId);
        synchronized (board) {
            board.lines.add(command.line);
            System.out.println("number of lines: " + board.lines.size());        
        }
        

        System.out.println("timestamp of last line: " + command.line.timestamp);
        String hubUrl = signalRServiceBaseEndpoint + "/api/v1/hubs/" + hubName + "/groups/" + command.groupId;
        String accessKey = generateJwt(hubUrl, command.userId);



        HttpResponse<String> response =  Unirest.post(hubUrl)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + accessKey)
            .body(new SignalRMessage("receiveCreateLine", new Object[] { command }))
            .asString();

        System.out.println("sendMessage: " + response.getStatus());
        System.out.println("sendMessage: " + response.getBody());
    }



    // @PostMapping("/api/messages")
    // public void sendMessage(@RequestBody Command command) {

    //     Board board = boards.boards.get(command.groupId);
    //     synchronized (board) {
    //         board.commands.add(command);
    //         System.out.println("number of lines: " + board.commands.size());        
    //     }
        

        
    //     String hubUrl = signalRServiceBaseEndpoint + "/api/v1/hubs/" + hubName + "/groups/" + command.groupId;
    //     String accessKey = generateJwt(hubUrl, command.userId);



    //     // System.out.print("List: ");
    //     // for(var point : command.points) {
    //     //     System.out.print("(" + point.first + "," + point.second +")");
    //     // }
    //     // System.out.println("");



    //     HttpResponse<String> response =  Unirest.post(hubUrl)
    //         .header("Content-Type", "application/json")
    //         .header("Authorization", "Bearer " + accessKey)
    //         .body(new SignalRMessage("newMessage", new Object[] { command }))
    //         .asString();

    //     System.out.println("sendMessage: " + response.getStatus());
    //     System.out.println("sendMessage: " + response.getBody());
    // }

    



    public static class Login {
        public Login(String userId) {
            this.userId = userId;
        }

        public String userId;
    }

    // T: WARNING temporary, for now returns a temporary userId
    @GetMapping("/api/templogin")
    public Login Login() {
        int randomUserId = Math.abs(ThreadLocalRandom.current().nextInt());
        String userId = Integer.toString(randomUserId);

        return new Login(userId);
    }

    // T: WARNING temporary, for now it permits to add a user to a group
    @GetMapping("/api/addgroup")
    public void addToGroup(@RequestParam String groupId, @RequestParam String userId) {

        System.out.println("adding to group");

        String hubUrl = signalRServiceBaseEndpoint + "/api/v1/hubs/" + hubName + "/groups/" + groupId + "/users/" + userId;
        String accessKey = generateJwt(hubUrl, userId);

        HttpResponse<String> response = Unirest.put(hubUrl)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + accessKey)
            .asString();

        System.out.println("addgroup: " + response.getStatus());
        System.out.println("addgroup: " + response.getBody());
    }

    @GetMapping("/api/isingroup")
    public void isUserInGroup(@RequestParam String groupId, @RequestParam String userId) {
        String hubUrl = signalRServiceBaseEndpoint + "/api/v1/hubs/" + hubName + "/groups/" + groupId + "/users/" + userId;
        String accessKey = generateJwt(hubUrl, userId);

        HttpResponse<String> response = Unirest.get(hubUrl)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + accessKey)
            // .body(new SignalRMessage("newMessage", new Object[] { message }))
            .asString();

        System.out.println("userInGroup: " + response.getStatus());
        System.out.println("userInGroup: " + response.getBody());
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
