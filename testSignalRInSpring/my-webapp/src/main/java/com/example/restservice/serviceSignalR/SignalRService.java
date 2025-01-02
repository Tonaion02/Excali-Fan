package com.example.restservice.serviceSignalR;

import org.springframework.stereotype.Service;


import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

// import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;



@Service
public class SignalRService {

    private static final String SIGNALR_URL = "https://testsignalrwithspring.service.signalr.net";
    private static final String ACCESS_KEY = "BxGCKbwTd0mqNGya6noR6iGTH2IRFQTdEpgx0FJY01RIMs0AtV4VJQQJ99BAAC5RqLJXJ3w3AAAAASRSKib0";
    // private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public void broadcastMessage(String hubName, String target, Object message) throws Exception {
        String url = SIGNALR_URL + "/api/v1/hubs/" + hubName;
        String sasToken = generateSasToken(url);

        // Create the payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("target", target);
        payload.put("arguments", new Object[]{message});

        // Send the HTTP POST request
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Authorization", sasToken);
            httpPost.setHeader("Content-Type", "application/json");
            // httpPost.setEntity(new StringEntity(OBJECT_MAPPER.writeValueAsString(payload), StandardCharsets.UTF_8));

            httpClient.execute(httpPost).close();
        }
    }

    private String generateSasToken(String url) {
        long expiry = System.currentTimeMillis() / 1000 + 3600; // 1 hour expiry
        String stringToSign = url + "\n" + expiry;

        String signature = hmacSHA256(stringToSign, ACCESS_KEY);
        return "SharedAccessSignature sr=" + url + "&sig=" + signature + "&se=" + expiry;
    }

    private String hmacSHA256(String data, String key) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(key.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC SHA256", e);
        }
    }
    
}
