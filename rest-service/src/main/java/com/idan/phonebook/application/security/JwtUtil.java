package com.idan.phonebook.application.security;

import com.idan.phonebook.application.users.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256); //TODO: use a persistent key that survives crashes and suitable for docker.
    private static ApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        JwtUtil.applicationContext = applicationContext;
    }

    private static UserRepository getUserRepository() {
        return applicationContext.getBean(UserRepository.class);
    }

    //
    // Generates a JWT token for a given User
    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
//                .claim("roles", user.getRoles()) // Add additional claims if needed
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 1)) // 1 hour expiration
                .signWith(SECRET_KEY)
                .compact();
    }

    // Extracts the username from a JWT token
    public static String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

    }


    // Checks if the JWT token is expired
    public static boolean isTokenExpired(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }

    // Validates the JWT token
    public static boolean validateToken(String token) {
        try {
            if (!isTokenExpired(token)) {
                return true;
            }
        } catch (SignatureException | MalformedJwtException e) {
            return false;
        }
        return false;
    }
}