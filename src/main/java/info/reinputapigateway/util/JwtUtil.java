package info.reinputapigateway.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class JwtUtil {
    @Value("${jwt.secret:IP0AIZ+wFl4KGo9nCrqnJWERHeU+ZAal7sgDvtF9Lq4=}")
    private String jwtSecret;

    public boolean validateToken(String token) {
        try{
            Jwts.parserBuilder()
                    .setSigningKey(jwtSecret.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex){
            logError(ex);
            return false;
        }
    }

    public Long extractUserId(String token){
        try{
            return Long.parseLong(Jwts.parserBuilder()
                    .setSigningKey(jwtSecret.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject());
        } catch (SecurityException ex){
            logError(ex);
            return null;
        }
    }

    private void logError(SecurityException e){
        log.error("Invalid JWT token: {}", e.getMessage());
    }
}
