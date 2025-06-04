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

package com.epam.ta.reportportal.exception.handler;

import java.net.URI;
import java.time.Instant;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

/**
 * RFC 7807 Problem Details exception handler for REST controllers.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7807">RFC 7807</a>
 */
@RestControllerAdvice(assignableTypes = {com.epam.ta.reportportal.ws.controller.GroupController.class})
@Log4j2
@Order(0)
public class ProblemDetailsExceptionHandler {

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ProblemDetail> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
    ProblemDetail problem = createProblemDetail(
        HttpStatus.valueOf(ex.getStatusCode().value()),
        ex.getReason(),
        ex.getMessage(),
        request
    );
    return ResponseEntity
        .status(ex.getStatusCode())
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problem);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleGeneralException(Exception ex, WebRequest request) {
    log.error("Unhandled exception", ex);
    ProblemDetail problem = createProblemDetail(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal Server Error",
        "An unexpected error occurred",
        request);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problem);
  }

  private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail, WebRequest request) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setType(URI.create("https://reportportal.io/errors/" + status));
    problem.setTitle(title);
    problem.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
    problem.setProperty("timestamp", Instant.now());
    return problem;
  }
}
