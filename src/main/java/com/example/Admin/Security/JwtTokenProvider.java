package com.example.Admin.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // 🛠️ ĐÃ FIX: Khớp chuỗi bí mật với TokenService
    @Value("${jwt.secret:DayLaMotKhoaBiMatDuPhongNeuKhongCoFileEnv123456}")
    private String rawSecretKey;

    @Value("${jwt.issuer:QuanLySinhVien}")
    private String expectedIssuer;

    @Value("${jwt.audience:QuanLySinhVienAudience}")
    private String expectedAudience;

    private SecretKey getSigningKey() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(rawSecretKey.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            throw new RuntimeException("Không thể khởi tạo khóa mã hóa JWT SHA-256", e);
        }
    }

    public Claims validateAndParseToken(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey()) 
                .parseClaimsJws(token)          
                .getBody();                     
    }

    public boolean isTokenValid(Claims claims) {
        String issuer = claims.getIssuer();
        String audience = claims.getAudience(); 
        Date expiration = claims.getExpiration();

        boolean isExpired = expiration != null && expiration.before(new Date());
        
        return expectedIssuer.equals(issuer) 
                && expectedAudience.equals(audience) 
                && !isExpired;
    }
}