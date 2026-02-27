package com.example.auth.service;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserAuthService {

    private final PasswordEncoder passwordEncoder;
    private final TimeBasedOneTimePasswordGenerator totp;
    private final Map<String, String> users = new ConcurrentHashMap<>();
    private final Map<String, String> userSecrets = new ConcurrentHashMap<>();

    public UserAuthService(PasswordEncoder passwordEncoder) throws Exception {
        this.passwordEncoder = passwordEncoder;
        this.totp = new TimeBasedOneTimePasswordGenerator();
        users.put("demo", passwordEncoder.encode("password123"));
        userSecrets.put("demo", "ZGVtby1zZWNyZXQta2V5LTEyMzQ1Ng==");
    }

    public boolean passwordValid(String username, String rawPassword) {
        return users.containsKey(username) && passwordEncoder.matches(rawPassword, users.get(username));
    }

    public boolean verifyTotp(String username, String code) {
        try {
            String secret = userSecrets.get(username);
            if (secret == null) return false;
            SecretKey key = new javax.crypto.spec.SecretKeySpec(Base64.getDecoder().decode(secret), totp.getAlgorithm());
            int currentCode = totp.generateOneTimePassword(key, Instant.now());
            int previousCode = totp.generateOneTimePassword(key, Instant.now().minus(totp.getTimeStep()));
            int nextCode = totp.generateOneTimePassword(key, Instant.now().plus(totp.getTimeStep()));
            int userCode = Integer.parseInt(code);
            return userCode == currentCode || userCode == previousCode || userCode == nextCode;
        } catch (Exception ex) {
            return false;
        }
    }

    public String getDemoSecretBase64() {
        return userSecrets.get("demo");
    }
}
