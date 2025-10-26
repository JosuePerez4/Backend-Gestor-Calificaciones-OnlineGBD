package gestor.calificaciones.gestorcalificaciones.security.JWT;

import io.jsonwebtoken.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final String SECRET_KEY = "*Vw45Ni*bBa^!vx&3w6W@va!v*gkKN95";

    private SecretKey getSignInKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return createToken(new HashMap<>(), userDetails.getUsername());
    }
    
    public String generateTokenFromUserDetails(UserDetails userDetails) {
        // Extract the user ID from CustomUserDetails
        if (userDetails instanceof gestor.calificaciones.gestorcalificaciones.security.User.CustomUserDetails) {
            gestor.calificaciones.gestorcalificaciones.security.User.CustomUserDetails customUserDetails = 
                (gestor.calificaciones.gestorcalificaciones.security.User.CustomUserDetails) userDetails;
            return createToken(new HashMap<>(), customUserDetails.getId().toString());
        }
        return createToken(new HashMap<>(), userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        // Token válido por 1 hora
        long expiration = 1000 * 60 * 60 * 1;

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public long getExpiration(String token) {
        return extractClaim(token, Claims::getExpiration).getTime();
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
