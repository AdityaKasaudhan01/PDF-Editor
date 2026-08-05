package com.pdfwordeditor.app.security;

import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserRepository users;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
  }

  public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
    if (users.findByUsername(request.username()).isPresent()) {
      throw new AuthException("Username already taken");
    }
    UserEntity user = new UserEntity();
    user.setId(UUID.randomUUID().toString());
    user.setUsername(request.username());
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    user.setEmail(request.email());
    users.save(user);
    return new AuthDtos.AuthResponse(jwtUtil.generate(user.getUsername()), user.getUsername());
  }

  public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
    UserEntity user = users.findByUsername(request.username())
      .orElseThrow(() -> new AuthException("Invalid credentials"));
    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new AuthException("Invalid credentials");
    }
    return new AuthDtos.AuthResponse(jwtUtil.generate(user.getUsername()), user.getUsername());
  }
}
