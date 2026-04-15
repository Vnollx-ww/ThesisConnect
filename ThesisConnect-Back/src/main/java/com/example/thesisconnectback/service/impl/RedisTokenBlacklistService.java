package com.example.thesisconnectback.service.impl;

import com.example.thesisconnectback.service.TokenBlacklistService;
import com.example.thesisconnectback.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

/**
 * 使用 Redis 存储已注销 JWT 的摘要，TTL 与 token 剩余有效期一致
 */
@Service
public class RedisTokenBlacklistService implements TokenBlacklistService {

    private static final String PREFIX = "jwt:bl:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void blacklist(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        long sec = jwtUtil.getRemainingSeconds(token);
        if (sec <= 0) {
            return;
        }
        String key = PREFIX + sha256Hex(token);
        stringRedisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(sec));
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        String key = PREFIX + sha256Hex(token);
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    private static String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
