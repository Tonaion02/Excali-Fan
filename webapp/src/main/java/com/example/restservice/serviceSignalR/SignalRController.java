package com.example.restservice.serviceSignalR;

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
public class SignalRController {

    public final BoardsRuntimeStorage boards;
    // T: Must become a constant
    // private final String hubName = "board";



    @Autowired
    public SignalRController(BoardsRuntimeStorage boards) {
        this.boards = boards;
        System.out.println("boards: " + boards);
    }

    @PostMapping("/signalr/negotiate")
    public SignalRConnectionInfo negotiate(@RequestParam String userId) {
        String hubUrl = Keys.signalRServiceBaseEndpoint + "/client/?hub=" + hubName;
        System.out.println("UserSessionID: " + userId);
        String accessKey = generateJwt(hubUrl, userId);

        return new SignalRConnectionInfo(hubUrl, accessKey);
    }

    @GetMapping("/api/addgroup")
    // T: This function create the association between a userId and a group with a put request
    public Boolean addToGroup(@RequestParam String groupId, @RequestParam String userId) {

        System.out.println("adding to group");

        Board board = boards.boards.get(groupId);
        if(board != null)
        {
            String hubUrl = Keys.signalRServiceBaseEndpoint + "/api/v1/hubs/" + hubName + "/groups/" + groupId + "/users/" + userId;
            String accessKey = generateJwt(hubUrl, userId);

            HttpResponse<String> response = Unirest.put(hubUrl)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessKey)
                .asString();

            System.out.println("addgroup: " + response.getStatus());
            System.out.println("addgroup: " + response.getBody());

            return true;
        }
        else
        {
            System.out.println("Group: " + groupId + " doesn't exist");

            return false;
        }
    }

    @GetMapping("/api/rmgroup")
    // T: This function remove a user from a group
    public void rmGroup(@RequestParam String groupId, @RequestParam String userId) {

        System.out.println("removing from a group");

        String hubUrl = Keys.signalRServiceBaseEndpoint + "/api/v1/hubs/" + hubName + "/groups/" + groupId + "/users/" + userId;
        String accessKey = generateJwt(hubUrl, userId);

        HttpResponse<String> response = Unirest.delete(hubUrl)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + accessKey)
            .asString();

        System.out.println("rmgroup: " + response.getStatus());
        System.out.println("rmgroup: " + response.getBody());
    }


    @GetMapping("/publicApi/isingroup")
    public void isUserInGroup(@RequestParam String groupId, @RequestParam String userId) {
        String hubUrl = Keys.signalRServiceBaseEndpoint + "/api/v1/hubs/" + hubName + "/groups/" + groupId + "/users/" + userId;
        String accessKey = generateJwt(hubUrl, userId);

        HttpResponse<String> response = Unirest.get(hubUrl)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + accessKey)
            // .body(new SignalRMessage("newMessage", new Object[] { message }))
            .asString();

        System.out.println("userInGroup: " + response.getStatus());
        System.out.println("userInGroup: " + response.getBody());
    }



    @PostMapping("/publicApi/countBoards")
    public int countBoards() {
        int size = boards.boards.size();
        System.out.println("count boards: " + size);
        return size;
    }
}