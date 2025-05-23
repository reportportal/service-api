/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.exception.forwarding;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * {@link HandlerExceptionResolver} Checks of exception contains response of downstream service and copies it to
 * upstream response.
 *
 * @author Andrei Varabyeu
 */
@Setter
@Service
public class ClientResponseForwardingExceptionHandler implements Ordered {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientResponseForwardingExceptionHandler.class);

  private int order;

  /**
   * Resolves and forwards exceptions by converting them into appropriate HTTP responses. Attempts to cast the exception
   * to ResponseForwardingException and create a response with the original status code, content type and body.
   *
   * @param ex The exception to be resolved and forwarded
   * @return ResponseEntity containing the forwarded response, or null if forwarding fails
   */
  public ResponseEntity<String> resolveException(Exception ex) {
    try {
      ResponseForwardingException forwardingException = (ResponseForwardingException) ex;
      var body = new String(forwardingException.getBody(), StandardCharsets.UTF_8);
      return ResponseEntity
          .status(forwardingException.getStatus())
          .contentType(Objects.requireNonNull(forwardingException.getHeaders().getContentType()))
          .body(body);

    } catch (Exception e) {
      LOGGER.error("Cannot forward exception", e);
      return null;
    }
  }

  @Override
  public int getOrder() {
    return order;
  }

}
