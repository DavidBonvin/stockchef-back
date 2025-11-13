package com.stockchef.stockchefback.service;

import com.stockchef.stockchefback.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service pour la gestion des JSON Web Tokens (JWT)
 */
@Service
public class JwtService {

    // Clé secrète pour signer les tokens (devrait être dans les variables d'environnement en production)
    @Value("${jwt.secret:StockChefSecretKeyForDevelopment2024ThisShouldBeLongerInProductionAndStoredInEnvironmentVariables}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 24 heures par défaut
    private long jwtExpiration;

    /**
     * Génère un token JWT pour l'utilisateur
     */
    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId().toString());
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("fullName", user.getFirstName() + " " + user.getLastName());
        
        return generateToken(extraClaims, user);
    }

    /**
     * Génère un token avec des claims additionnels
     */
    public String generateToken(Map<String, Object> extraClaims, User user) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrait l'email (subject) du token
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrait le rôle du token
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extrait un claim spécifique du token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrait un claim spécifique par nom
     */
    public String extractClaim(String token, String claimName) {
        return extractClaim(token, claims -> claims.get(claimName, String.class));
    }

    /**
     * Valide si le token est valide pour l'utilisateur
     */
    public boolean isTokenValid(String token, User user) {
        try {
            final String email = extractEmail(token);
            return (email.equals(user.getEmail())) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérifie si le token est expiré
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrait la date d'expiration du token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrait tous les claims du token
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Obtient la clé de signature
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(java.util.Base64.getEncoder().encodeToString(secretKey.getBytes()));
        return Keys.hmacShaKeyFor(keyBytes);
    }
}