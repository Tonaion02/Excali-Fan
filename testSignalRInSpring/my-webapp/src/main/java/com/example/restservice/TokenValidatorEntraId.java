package com.example.restservice;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import com.auth0.jwk.*;

import java.net.MalformedURLException;
import java.net.URL;



import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.*;
import com.nimbusds.jwt.SignedJWT;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;





public class TokenValidatorEntraId {
    private static final String COMMON_KEYS_URL = "https://login.microsoftonline.com/common/discovery/v2.0/keys";
    private static JWKSource<SecurityContext> keySource;

    static {
        try {
            keySource = new RemoteJWKSet<>(new URL(COMMON_KEYS_URL));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error initializing JWKSource", e);
        }
    }

    /**
     * Valida un token JWT contro il tenant /common di Microsoft Entra ID.
     * @param token Il token JWT da validare.
     * @return true se il token è valido, false altrimenti.
     */
    public static boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String issuer = signedJWT.getJWTClaimsSet().getIssuer();

            System.out.println("issuer: " + issuer);
            // Controlla che il token sia emesso dal tenant /common
            if (!"https://login.microsoftonline.com/common/v2.0".equals(issuer)) {
                System.out.println("errore in sta merda");
                return false;
            }

            // Configura il processore JWT
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(
                    signedJWT.getHeader().getAlgorithm(), keySource);
            jwtProcessor.setJWSKeySelector(keySelector);

            // Verifica il token
            jwtProcessor.process(token, null);
            return true; // Token valido

        } catch (ParseException | BadJOSEException | JOSEException e) {
            System.out.println("error message: " + e.getMessage());
            return false; // Token non valido
        }
    }
}