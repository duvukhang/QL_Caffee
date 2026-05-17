package com.example.demo.MidWare.JWT;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TokenService {

    @Value("${env.JWT_secret:DayLaMotKhoaBiMatDuPhongNeuKhongCoFileEnv123456}")
    private String jwtSecret;

    @Value("${jwt.issuer:QuanLySinhVien}")
    private String issuer;

    @Value("${jwt.audience:QuanLySinhVienAudience}")
    private String audience;

    @Value("${jwt.accessTokenMinutes:15}")
    private int accessTokenMinutes;

    public static class TokenPair {
        private final String accessToken;

        public TokenPair(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getAccessToken() {
            return accessToken;
        }
    }

    public TokenPair createTokenPair(int userID, String role) {
        try {
            // TÁI HIỆN LOGIC C#: Băm chuỗi secret key bằng SHA-256 giống hệt C# cũ của bạn
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(jwtSecret.getBytes(StandardCharsets.UTF_8));
            var signingKey = Keys.hmacShaKeyFor(keyBytes);

            Map<String, Object> claims = new HashMap<>();
            claims.put("role", role); // Lưu Role vào claim (Spring Security Filter sẽ đọc ra)

            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + (long) accessTokenMinutes * 60 * 1000);

            String accessTokenString = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(String.valueOf(userID)) // Sub tương đương UserID
                    .setId(UUID.randomUUID().toString()) // Jti tương đương Guid.NewGuid()
                    .setIssuer(issuer)
                    .setAudience(audience)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(signingKey, SignatureAlgorithm.HS256)
                    .compact();

            return new TokenPair(accessTokenString);
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi khởi tạo thuật toán mã hóa SHA-256", e);
        }
    }
}
