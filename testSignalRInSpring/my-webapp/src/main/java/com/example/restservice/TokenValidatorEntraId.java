package com.example.restservice;

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

    
    /**
     * T: validate token through the following controls:
     * 1. Parsing of the token
     * 2. Retrieve the public key
     * 3. Check sign
     * 4. Check if is expired (exp)
     * 5. Check the issuer (iss)
     * 6. Check for the audience (aud)
     *
     * @param token The JWT token to check
     * @return true if the token is valid, false otherwise
     */
    public static boolean validateToken(String token) {
        try {
            // T: parsing of the token
            SignedJWT signedJWT = SignedJWT.parse(token);
            System.out.println("Token parsato con successo.");


            System.out.println("Algoritmo del token: " + signedJWT.getHeader().getAlgorithm());

            // T: retrieve the correct public 
            RSAPublicKey publicKey = getPublicKey(signedJWT.getHeader().getKeyID());
            if (publicKey == null) {
                System.out.println("Chiave pubblica non trovata per il token.");
                return false;
            }

            // Verifica della firma
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            if (!signedJWT.verify(verifier)) {
                System.out.println("Firma non valida.");
                return false;
            }
            System.out.println("Firma verificata con successo.");

            // Controllo della scadenza (exp)
            if (isTokenExpired(signedJWT)) {
                System.out.println("Token scaduto.");
                return false;
            }
            System.out.println("Token non scaduto.");

            // Controllo dell'issuer (iss)
            if (!isIssuerValid(signedJWT)) {
                System.out.println("Issuer non valido.");
                return false;
            }
            System.out.println("Issuer valido.");

            // Controllo dell’audience (aud)
            if (!isAudienceValid(signedJWT)) {
                System.out.println("Audience non valida.");
                return false;
            }
            System.out.println("Audience valida.");

            System.out.println("Token valido.");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Errore durante la validazione del token: " + e.getMessage());
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
    private static RSAPublicKey getPublicKey(String kid) throws IOException, ParseException {
        JWKSet jwkSet = JWKSet.load(new URL(Keys.JWKS_URL_V1));

        try {
            for (JWK jwk : jwkSet.getKeys()) {
                if (jwk.getKeyID().equals(kid) && jwk instanceof RSAKey) {
                    System.out.println("Chiave pubblica trovata per KID: " + kid);
                    return ((RSAKey) jwk).toRSAPublicKey();
                }
            }
        } catch(Exception e) {
            System.out.println("A maronn i l'eccezion: " + e.getMessage());
        }

        System.out.println("Nessuna chiave pubblica corrispondente trovata per KID: " + kid);
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
        return audience != null && audience.contains(Keys.EXPECTED_AUDIENCE);
    }
}
