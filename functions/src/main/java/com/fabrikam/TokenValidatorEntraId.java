package com.fabrikam;

import com.microsoft.azure.functions.ExecutionContext;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import java.io.IOException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.List;





public class TokenValidatorEntraId {

    // URL per recuperare le chiavi pubbliche di Azure AD v1.0
    private static final String JWKS_URL_V1 = "https://login.microsoftonline.com/common/discovery/v2.0/keys";
    
    // Client ID dell'applicazione
    private static final String EXPECTED_AUDIENCE = "b1453203-8719-4a2a-8cc6-96bf883a7e65"; 

    /**
     * Valida il token JWT eseguendo i seguenti controlli:
     * 1. Parsing del token
     * 2. Recupero della chiave pubblica
     * 3. Verifica della firma
     * 4. Controllo della scadenza (exp)
     * 5. Controllo dell'issuer (iss)
     * 6. Controllo dell'audience (aud)
     *
     * @param token Il token JWT da verificare
     * @return true se il token è valido, false altrimenti
     */
    public static boolean validateToken(String token, final ExecutionContext context) {
        try {
            // Parsing del token JWT
            SignedJWT signedJWT = SignedJWT.parse(token);
            context.getLogger().info("Token parsato con successo.");


            context.getLogger().info("Algoritmo del token: " + signedJWT.getHeader().getAlgorithm());

            // Recupero della chiave pubblica corretta
            RSAPublicKey publicKey = getPublicKey(signedJWT.getHeader().getKeyID(), context);
            if (publicKey == null) {
                context.getLogger().info("Chiave pubblica non trovata per il token.");
                return false;
            }

            // Verifica della firma
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            if (!signedJWT.verify(verifier)) {
                context.getLogger().info("Firma non valida.");
                return false;
            }
            context.getLogger().info("Firma verificata con successo.");

            // Controllo della scadenza (exp)
            if (isTokenExpired(signedJWT)) {
                context.getLogger().info("Token scaduto.");
                return false;
            }
            context.getLogger().info("Token non scaduto.");

            // Controllo dell'issuer (iss)
            if (!isIssuerValid(signedJWT)) {
                context.getLogger().info("Issuer non valido.");
                return false;
            }
            context.getLogger().info("Issuer valido.");

            // Controllo dell’audience (aud)
            if (!isAudienceValid(signedJWT)) {
                context.getLogger().info("Audience non valida.");
                return false;
            }
            context.getLogger().info("Audience valida.");

            context.getLogger().info("Token valido.");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            context.getLogger().info("Errore durante la validazione del token: " + e.getMessage());
            return false;
        }
    }

    /**
     * Recupera la chiave pubblica per verificare la firma del token.
     *
     * @param kid Il key ID della chiave pubblica
     * @return La chiave pubblica RSA, o null se non trovata
     * @throws IOException    Se c'è un problema nel recupero della chiave
     * @throws ParseException Se la risposta del server non è valida
     */
    private static RSAPublicKey getPublicKey(String kid, final ExecutionContext context) throws IOException, ParseException {
        JWKSet jwkSet = JWKSet.load(new URL(JWKS_URL_V1));

        try {
            for (JWK jwk : jwkSet.getKeys()) {
                if (jwk.getKeyID().equals(kid) && jwk instanceof RSAKey) {
                    context.getLogger().info("Chiave pubblica trovata per KID: " + kid);
                    return ((RSAKey) jwk).toRSAPublicKey();
                }
            }
        } catch(Exception e) {
            context.getLogger().info("A maronn i l'eccezion: " + e.getMessage());
        }

        context.getLogger().info("Nessuna chiave pubblica corrispondente trovata per KID: " + kid);
        return null;
    }

    /**
     * Controlla se il token è scaduto.
     *
     * @param signedJWT Il token JWT
     * @return true se il token è scaduto, false altrimenti
     * @throws ParseException Se il claim "exp" non è valido
     */
    private static boolean isTokenExpired(SignedJWT signedJWT) throws ParseException {
        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        return expirationTime == null || new Date().after(expirationTime);
    }

    /**
     * Verifica che l'issuer (iss) sia valido per Azure AD v1.0.
     *
     * @param signedJWT Il token JWT
     * @return true se l'issuer è valido, false altrimenti
     * @throws ParseException Se il claim "iss" non è valido
     */
    private static boolean isIssuerValid(SignedJWT signedJWT) throws ParseException {
        String issuer = signedJWT.getJWTClaimsSet().getIssuer();
        return issuer != null &&
               issuer.startsWith("https://login.microsoftonline.com/") &&
               issuer.endsWith("/v2.0");
    }

    /**
     * Controlla se l'audience (aud) corrisponde al client ID atteso.
     *
     * @param signedJWT Il token JWT
     * @return true se l'audience è valida, false altrimenti
     * @throws ParseException Se il claim "aud" non è valido
     */
    private static boolean isAudienceValid(SignedJWT signedJWT) throws ParseException {
        List<String> audience = signedJWT.getJWTClaimsSet().getAudience();
        return audience != null && audience.contains(EXPECTED_AUDIENCE);
    }
}

