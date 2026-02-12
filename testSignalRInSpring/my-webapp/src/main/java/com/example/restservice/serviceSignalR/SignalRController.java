package com.example.restservice.serviceSignalR;

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
import com.example.restservice.TokenValidatorEntraId;
import com.example.restservice.Board.*;

import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;





@RestController
public class SignalRController {

    private final BoardsRuntimeStorage boards;
    private String hubName = "board";



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

    @PostMapping("/api/deleteLine")
    public void deleteLine(@RequestBody DeleteLineCommand command) {

        Board board = boards.boards.get(command.groupId);
        synchronized(board) {
            Line fakeLine = new Line("null", null, command.userIdOfLine, command.timestampOfLine);
            board.lines.remove(fakeLine);    
        }



        String hubUrl = Keys.signalRServiceBaseEndpoint + "/api/v1/hubs/" + hubName + "/groups/" + command.groupId;
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
        try {        
            Board board = boards.boards.get(command.groupId);
            synchronized (board) {
                board.lines.add(command.line);
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

    // T: TODO Substitute this objcet with a real command to close the board
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
        int randomNumericBoardId = Math.abs(ThreadLocalRandom.current().nextInt());
        String boardId = Integer.toString(randomNumericBoardId);

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
        boards.boards.put(boardId, board);

        return boardId;
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
            
            // T: TODO try to optimize this thing
            synchronized (board)
            {
                return board;
            }
        }
        catch(RuntimeException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    // T: This api is used to test if the validation of the Token works
    // T: The status of the HTTP response is:
    // - 200 if the token is valid
    // - 201 if the token is not valid
    @PostMapping("/publicApi/verifyLoginToken")
    public void verifyLoginToken(HttpServletRequest request, HttpServletResponse response) {
        String loginToken = request.getHeader("Authorization");
        if(!TokenValidatorEntraId.validateToken(loginToken)) {
            System.out.println("Invalid token");
            response.setStatus(201);
        } else {
            System.out.println("Valid token");
            response.setStatus(200);
        }
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
        int randomNumericBoardId = Math.abs(ThreadLocalRandom.current().nextInt());
        String boardId = Integer.toString(randomNumericBoardId);
        


        Board board = new Board();
        board.setOwnerUserId(email);
        board.setHostUserId(lr.userId);
        boards.boards.put(boardId, board);     

        

        return boardId;
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
        int numericBoardSessionId = Math.abs(ThreadLocalRandom.current().nextInt());
        String boardSessionId = Integer.toString(numericBoardSessionId);



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

        // T: Add board to the collection of boards
        boards.boards.put(boardSessionId, board);

        
        System.out.println("Succsefully added board in the server");


        return new LoadBoardResult(boardSessionId, boardJson);
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

    @PostMapping("/api/listBoards")
    public List<String> listBoards(@RequestHeader("Authorization") String accessToken) {
        
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



        // T: Retrieve the list of boards (START)
        BoardStorage boardStorage = new BoardStorage();
        List<String> boardsList;
        try {
            boardsList = boardStorage.listBoards(email);
        } catch(Exception e) {
            System.out.println("Error during saving of Board: " + e.getMessage());
            e.printStackTrace();

            return null;
        }
        // T: Retrieve the list of boards (END)

        return boardsList;
    }
    


    








    // T: WARNING temporary, for now returns a temporary userId
    public static class Login {
        public Login(String userId) {
            this.userId = userId;
        }

        public String userId;
    }

    @GetMapping("/publicApi/templogin")
    public Login TempLogin() {
        int randomUserId = Math.abs(ThreadLocalRandom.current().nextInt());
        String userId = Integer.toString(randomUserId);

        return new Login(userId);
    }




    @GetMapping("/api/addgroup")
    // T: This function create the association between a userId and a group with a put request
    public void addToGroup(@RequestParam String groupId, @RequestParam String userId) {

        System.out.println("adding to group");

        String hubUrl = Keys.signalRServiceBaseEndpoint + "/api/v1/hubs/" + hubName + "/groups/" + groupId + "/users/" + userId;
            String accessKey = generateJwt(hubUrl, userId);

        HttpResponse<String> response = Unirest.put(hubUrl)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + accessKey)
            .asString();

        System.out.println("addgroup: " + response.getStatus());
        System.out.println("addgroup: " + response.getBody());
    }

    @GetMapping("/api/rmgroup")
    // T: This function remove a user from a group
    public void rmGroup(@RequestParam String groupId, @RequestParam String userId) {

        System.out.println("adding to group");

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




    // T: TEMPORARY for testing of blobStorage (START)
    @PostMapping("/publicApi/testBlobStorage")
    public TestBlob testBlobStorage() {
        TestBlob testBlob = null;
        try {
            BoardStorage boardStorage = new BoardStorage();
            System.out.println("Starting to extract bloab");
            testBlob = boardStorage.loadTestBlob("t.json");
        } catch(RuntimeException e) {
            e.printStackTrace();
            System.out.println("error:" + e.getMessage());
        }
        catch(Exception e) {
            System.out.println("error:" + e.getMessage());
        }
        return testBlob;
    }

    public static class WrapperString {
        private String blobName;
    
        public WrapperString() {}
    
        public WrapperString(String blobName) {
            this.blobName = blobName;
        }
    
        public String getBlobName() {
            return blobName;
        }
    
        public void setBlobName(String blobName) {
            this.blobName = blobName;
        }
    }

    // @PostMapping("/api/readFromBlobStorage")
    // public String readFromBlobStorage(@RequestHeader("Authorization") String accessToken, @RequestBody WrapperString ws) {
    //     String result = null;
    //     try {
    //         BoardStorage boardStorage = new BoardStorage();
    //         result = boardStorage.loadBlob(ws.blobName);
    //     } catch(Exception e) {
    //         e.printStackTrace();
    //     }
    //     return result;
    // }

    // @PostMapping("/api/saveToBlobStorage")
    // public void saveToBlobStorage(@RequestHeader("Authorization") String accessToken, @RequestBody RequestBodyBlobToSave requestBody, HttpServletResponse response) {
    //     BoardStorage boardStorage = new BoardStorage();

    //     try {
    //         boardStorage.saveBlob(requestBody.blobName, requestBody.dataToSave);
    //     } catch(Exception e)
    //     {
    //         e.printStackTrace();
    //         response.setStatus(201);
    //     }
    // }
    // T: TEMPORARY for testing of blobStorage (END)







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
