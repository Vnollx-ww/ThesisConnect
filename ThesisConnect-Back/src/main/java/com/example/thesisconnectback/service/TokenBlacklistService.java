package com.example.thesisconnectback.service;

/**
 * JWT 登出黑名单（Redis），使已注销 token 在过期前失效
 */
public interface TokenBlacklistService {

    void blacklist(String token);

    boolean isBlacklisted(String token);
}
