package com.pdfwordeditor.app.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  private final String secret;
  private final long expirationMs;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public JwtUtil(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.expiration-ms}") long expirationMs) {
    this.secret = secret;
    this.expirationMs = expirationMs;
  }

  public String generate(String username) {
    long now = System.currentTimeMillis();
    String header = b64url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
    String payload = b64url("{\"sub\":\"" + username
      + "\",\"iat\":" + (now / 1000)
      + ",\"exp\":" + ((now + expirationMs) / 1000) + "}");
    String signature = sign(header + "." + payload);
    return header + "." + payload + "." + signature;
  }

  public String validateAndGetUsername(String token) {
    String[] parts = token.split("\\.");
    if (parts.length != 3) {
      throw new IllegalArgumentException("Invalid token");
    }
    String expected = sign(parts[0] + "." + parts[1]);
    if (!MessageDigest.isEqual(
        expected.getBytes(StandardCharsets.UTF_8),
        parts[2].getBytes(StandardCharsets.UTF_8))) {
      throw new IllegalArgumentException("Invalid token signature");
    }
    try {
      String json = new String(decodeB64url(parts[1]), StandardCharsets.UTF_8);
      JsonNode node = objectMapper.readTree(json);
      long exp = node.get("exp").asLong();
      if (exp * 1000 < System.currentTimeMillis()) {
        throw new IllegalArgumentException("Token expired");
      }
      return node.get("sub").asText();
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid token", e);
    }
  }

  private String sign(String data) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      return b64url(raw);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to sign JWT", e);
    }
  }

  private String b64url(String value) {
    return b64url(value.getBytes(StandardCharsets.UTF_8));
  }

  private String b64url(byte[] bytes) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private byte[] decodeB64url(String value) {
    return Base64.getUrlDecoder().decode(value);
  }
}
