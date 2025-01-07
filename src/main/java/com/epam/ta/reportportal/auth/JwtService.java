package com.epam.ta.reportportal.auth;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  @Autowired
  private JwtDecoder jwtDecoder;

  public String extractUserName(String token) {
    return (String) jwtDecoder.decode(token)
        .getClaims()
        .get("user_name");
  }

  public boolean isValid(String token) {
    return Instant.now()
        .isBefore(requireNonNull(jwtDecoder.decode(token).getExpiresAt()));
  }

}
