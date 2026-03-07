import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;





public class HmacIdGenerator {
    // T: Method to encrypt the id
    public static String generateHmacId(String originalUserId) {
        try {
            // Set up the HMAC with SHA-256
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    Keys.keyEncryption.getBytes(StandardCharsets.UTF_8), 
                    "HmacSHA256"
            );
            mac.init(secretKeySpec);
            
            // Hash the email using the secret key
            byte[] hmacBytes = mac.doFinal(originalUserId.toLowerCase().trim().getBytes(StandardCharsets.UTF_8));
            
            // Encode the result to a URL-safe Base64 string so it can be used in APIs safely
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
            
        } catch (Exception e) {
            // In production, handle this exception properly based on your framework
            throw new RuntimeException("Failed to generate secure ID", e);
        }
    }

    // T: Method to generate the board session id
    public static String generateIdBoard(String userId) {
        int randomNumericBoardId = Math.abs(ThreadLocalRandom.current().nextInt());
        String boardId = userId + Integer.toString(randomNumericBoardId);

        boardId = HmacIdGenerator.generateHmacId(boardId);

        return boardId;
    }
}