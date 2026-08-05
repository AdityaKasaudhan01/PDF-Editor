package com.pdfwordeditor.app.security;

public final class AuthDtos {
  private AuthDtos() {
  }

  public record RegisterRequest(String username, String password, String email) {}

  public record LoginRequest(String username, String password) {}

  public record AuthResponse(String token, String username) {}
}
