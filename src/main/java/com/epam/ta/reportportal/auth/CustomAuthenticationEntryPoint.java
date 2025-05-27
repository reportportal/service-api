/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  /**
   * Handles an authentication failure by sending an HTTP 401 Unauthorized response with a JSON body containing error details.
   *
   * @param request       the {@link HttpServletRequest} that resulted in an {@link AuthenticationException}
   * @param response      the {@link HttpServletResponse} to send the error response
   * @param authException the exception that caused the authentication failure
   * @throws IOException if an input or output error occurs while writing the response
   */
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
