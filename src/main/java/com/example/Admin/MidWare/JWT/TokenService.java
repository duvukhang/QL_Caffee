package com.example.Admin.MidWare.JWT;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TokenService {

    // 🛠️ ĐÃ FIX: Đồng bộ tuyệt đối biến môi trường giống JwtTokenProvider
    @Value("${jwt.secret:DayLaMotKhoaBiMatDuPhongNeuKhongCoFileEnv123456}")
    private String jwtSecret;

    @Value("${jwt.issuer:QuanLySinhVien}")
    private String issuer;

    @Value("${jwt.audience:QuanLySinhVienAudience}")
    private String audience;

    @Value("${jwt.accessTokenMinutes:600}") // Tăng thời gian sống lên 10 tiếng để dễ Test
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
        return createTokenPair(String.valueOf(userID), role, Collections.emptyMap());
    }

    public TokenPair createTokenPair(String subject, String role) {
        return createTokenPair(subject, role, Collections.emptyMap());
    }

    public TokenPair createTokenPair(String subject, String role, Map<String, Object> extraClaims) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(jwtSecret.getBytes(StandardCharsets.UTF_8));
            var signingKey = Keys.hmacShaKeyFor(keyBytes);

            String normalizedRole = "";
            if (role != null && !role.isEmpty()) {
                normalizedRole = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
            }

            Map<String, Object> claims = new HashMap<>();
            if (extraClaims != null) {
                claims.putAll(extraClaims);
            }
            claims.put("role", normalizedRole); 

            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + (long) accessTokenMinutes * 60 * 1000);

            String accessTokenString = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject) 
                    .setId(UUID.randomUUID().toString()) 
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
