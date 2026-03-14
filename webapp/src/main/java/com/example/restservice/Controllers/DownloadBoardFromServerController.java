package com.example.restservice.DownloadBoardFromServerController;

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
public class DownloadBoardFromServerController {

    public final BoardsRuntimeStorage boards;

    @Autowired
    public DownloadBoardFromServerController(BoardsRuntimeStorage boards) {
        this.boards = boards;
        System.out.println("boards: " + boards);
    }

    public static class RequestDownloadFromServer
    {
        private String groupId;

        public RequestDownloadFromServer(){}

        public RequestDownloadFromServer(String groupId) {
            this.groupId = groupId;
        }

        public String getGroupId() {
            return this.groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }
    }

    @PostMapping("/api/downloadBoardFromServer")
    public Board downloadBoardFromServer(@RequestBody RequestDownloadFromServer request) {        
        try
        {
            Board board = boards.boards.get(request.groupId);
            
            if(board != null)
            {
                // T: TODO try to optimize this thing
                synchronized (board)
                {
                    return board;
                }
            }
        }
        catch(RuntimeException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}