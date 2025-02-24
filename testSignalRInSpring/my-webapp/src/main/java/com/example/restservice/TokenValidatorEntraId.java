package com.example.restservice;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class TokenValidatorEntraId {

    private static final String JWKS_URL_V1 = "https://login.microsoftonline.com/common/discovery/keys";
    private static final String JWKS_URL_V2 = "https://login.microsoftonline.com/common/discovery/v2.0/keys";
    private static final String EXPECTED_AUDIENCE = "b1453203-8719-4a2a-8cc6-96bf883a7e65"; // Sostituire con il client ID corretto

    public static boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            String version = signedJWT.getJWTClaimsSet().getStringClaim("ver");
            boolean isV2 = "2.0".equals(version);
            String jwksUrl = isV2 ? JWKS_URL_V2 : JWKS_URL_V1;

            if (!verifySignature(signedJWT, jwksUrl)) {
                System.out.println("Firma non valida");
                return false;
            }

            if (isTokenExpired(signedJWT)) {
                System.out.println("Token scaduto");
                return false;
            }

            if (!isIssuerValid(signedJWT, isV2)) {
                System.out.println("Issuer non valido");
                return false;
            }

            if (!isAudienceValid(signedJWT)) {
                System.out.println("Audience non valida");
                return false;
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean verifySignature(SignedJWT signedJWT, String jwksUrl) throws Exception {
        JWKSet jwkSet = JWKSet.load(new URL(jwksUrl));
        for (JWK jwk : jwkSet.getKeys()) {
            if (jwk instanceof RSAKey) {
                RSAPublicKey publicKey = ((RSAKey) jwk).toRSAPublicKey();
                JWSVerifier verifier = new RSASSAVerifier(publicKey);
                if (signedJWT.verify(verifier)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isTokenExpired(SignedJWT signedJWT) throws ParseException {
        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        return expirationTime == null || new Date().after(expirationTime);
    }

    private static boolean isIssuerValid(SignedJWT signedJWT, boolean isV2) throws ParseException {
        String issuer = signedJWT.getJWTClaimsSet().getIssuer();
        if (isV2) {
            return issuer != null &&
                   issuer.startsWith("https://login.microsoftonline.com/") &&
                   issuer.endsWith("/v2.0");
        } else {
            return issuer != null &&
                   issuer.startsWith("https://sts.windows.net/") &&
                   issuer.endsWith("/");
        }
    }

    private static boolean isAudienceValid(SignedJWT signedJWT) throws ParseException {
        List<String> audience = signedJWT.getJWTClaimsSet().getAudience();
        return audience != null && audience.contains(EXPECTED_AUDIENCE);
    }
}