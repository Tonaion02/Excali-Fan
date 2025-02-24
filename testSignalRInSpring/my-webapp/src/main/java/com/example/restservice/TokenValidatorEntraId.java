package com.example.restservice;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import com.auth0.jwk.*;

import java.net.URL;



import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;







public class TokenValidatorEntraId {
    private static final String JWKS_URL = "https://login.microsoftonline.com/common/discovery/v2.0/keys";
    private static final String EXPECTED_AUDIENCE = "b1453203-8719-4a2a-8cc6-96bf883a7e65";
    
    public static boolean validateToken(String token) {
        try {
            JwkProvider provider = new UrlJwkProvider(new URL(JWKS_URL));
            String kid = Jwts.parser().parseClaimsJws(token).getHeader().getKeyId();
            Jwk jwk = provider.get(kid);

            System.out.println("kid: " + kid);
            
            RSAPublicKey publicKey = (RSAPublicKey) jwk.getPublicKey();
            
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(publicKey)
                    .requireAudience(EXPECTED_AUDIENCE)
                    .requireIssuer("https://login.microsoftonline.com/common/v2.0")
                    .parseClaimsJws(token);
            
            System.out.println("Token valido: " + claims.getBody());
            return true;
        } catch (Exception e) {
            System.err.println("Token non valido: " + e.getMessage());
            return false;
        }
    }
}