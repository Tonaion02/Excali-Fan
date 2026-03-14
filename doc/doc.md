# DOCUMENTATION

# Change configuration of Azure SignalR and Azure App Service to use better WebSocket
From the documentation of Azure SignalR and Azure App Service, we have discovered that we can configure the communication between Azure SignalR and Azure App Service in multiple manners.

Currently, we are using the WebSocket connection between clients and server, only to send messages from Server to the clients.
It's also possible to send over this connection messages to the server.

This can be really useful to evitate to create a new connection each time we need to send an HTTP request from the client to the server.
We don't need to change the code of the application, it's only a matter of how we configured SignalR and Azure App Service.

Azure SignalR Service need to be in Serverless mode.
Then, you need to modify the negotiation point
```java
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SignalRNegotiateController {

    // These should be loaded from environment variables/Azure App Service settings
    private final String signalREndpoint = "https://<your-instance>.service.signalr.net";
    private final String hubName = "myHub"; 

    @PostMapping("/negotiate")
    public ResponseEntity<Map<String, String>> negotiate(@RequestParam String userId) {
        // 1. Construct the WebSocket URL that the client will connect to
        String clientUrl = signalREndpoint + "/client/?hub=" + hubName;

        // 2. Generate a JWT token using your Azure SignalR Access Key
        // (You will need a JWT library like 'jjwt' to generate this using HMAC-SHA256)
        String token = generateSignalRJwtToken(clientUrl, userId); 

        // 3. Return the URL and Token to the client
        Map<String, String> response = new HashMap<>();
        response.put("url", clientUrl);
        response.put("accessToken", token);
        
        return ResponseEntity.ok(response);
    }
}
```

Then you need to create a webhook
```java
@RestController
@RequestMapping("/api")
public class SignalRWebhookController {

    // Azure SignalR will send HTTP POSTs here when a client sends a WebSocket message
    @PostMapping("/webhook")
    public ResponseEntity<String> receiveMessageFromClient(
            @RequestHeader("ce-connectionid") String connectionId,
            @RequestHeader("ce-type") String eventType,
            @RequestBody String payload) {
        
        System.out.println("New WebSocket message from Connection ID: " + connectionId);
        System.out.println("Event Type: " + eventType); // e.g., "message" or "connected"
        System.out.println("Payload: " + payload);

        // Handle your business logic here!
        
        // Always return a 200 OK so Azure SignalR knows you received it
        return ResponseEntity.ok().build();
    }
}
```

At this point you can create from each client a WebSocket connection and relies on it to exchange these requests.