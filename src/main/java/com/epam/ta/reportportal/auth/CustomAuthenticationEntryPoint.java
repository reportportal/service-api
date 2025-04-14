package com.epam.ta.reportportal.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Autowired
  ObjectMapper objectMapper;

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
      throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType("application/json");

    Map<String, String> map = new HashMap<>();
    map.put("error", "invalid_token");
    map.put("error_description", authException.getMessage());
    var body = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);

    response.getWriter().write(body);
    response.flushBuffer();
  }

}
