package com.example.demo.Security;

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

    @Value("${jwt.secret}")
    private String rawSecretKey;

    @Value("${jwt.issuer}")
    private String expectedIssuer;

    @Value("${jwt.audience}")
    private String expectedAudience;

    // 🛠️ ĐỒNG BỘ 1-1 THUẬT TOÁN SHA-256 TỪ C# PROGRAM.CS
    private SecretKey getSigningKey() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(rawSecretKey.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            throw new RuntimeException("Không thể khởi tạo khóa mã hóa JWT SHA-256", e);
        }
    }

    // 🛠️ ĐÃ FIX: Đổi sang cú pháp setSigningKey() và getBody() của phiên bản cũ
    public Claims validateAndParseToken(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey()) // Thay cho verifyWith()
                .parseClaimsJws(token)          // Thay cho parseSignedClaims()
                .getBody();                     // Thay cho getPayload()
    }

    // 🛠️ ĐÃ FIX: Ở bản cũ, getAudience() trả về thẳng kiểu String chứ không phải một Set
    public boolean isTokenValid(Claims claims) {
        String issuer = claims.getIssuer();
        String audience = claims.getAudience(); // Đã sửa: Gọi trực tiếp chuỗi String
        Date expiration = claims.getExpiration();

        boolean isExpired = expiration != null && expiration.before(new Date());
        
        return expectedIssuer.equals(issuer) 
                && expectedAudience.equals(audience) 
                && !isExpired;
    }
}