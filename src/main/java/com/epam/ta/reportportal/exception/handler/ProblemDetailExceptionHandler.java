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

import com.epam.reportportal.rules.exception.ReportPortalException;
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
 * RFC 7807 Problem Details exception handler for new REST controllers.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7807">RFC 7807</a>
 * @see ProblemDetail
 */
@Log4j2
@Order(0)
@RestControllerAdvice(assignableTypes = {
    com.epam.ta.reportportal.ws.controller.GroupController.class,
    com.epam.ta.reportportal.ws.controller.OrganizationController.class,
    com.epam.ta.reportportal.ws.controller.OrganizationGroupController.class,
    com.epam.ta.reportportal.ws.controller.GeneratedProjectController.class,
//    com.epam.ta.reportportal.ws.controller.GeneratedUserController.class,
//    com.epam.ta.reportportal.ws.controller.OrganizationProjectController.class,
//    com.epam.ta.reportportal.ws.controller.OrganizationUsersController.class,
})
public class ProblemDetailExceptionHandler {

  private static final String TYPE_URL = "https://reportportal.io/docs/errors/";

  /**
   * Handles {@link ResponseStatusException} and returns a {@link ProblemDetail} response.
   *
   * @param e       The exception to handle.
   * @param request The current web request.
   * @return A {@link ResponseEntity} containing the {@link ProblemDetail}.
   */
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ProblemDetail> handleResponseStatusException(ResponseStatusException e, WebRequest request) {
    log.error("ResponseStatusException occurred", e);

    ProblemDetail problem = e.getBody();
    problem.setType(URI.create(TYPE_URL + e.getStatusCode().value()));
    problem.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
    problem.setProperty("timestamp", Instant.now());

    return ResponseEntity
        .status(e.getStatusCode())
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problem);
  }

  @ExceptionHandler(ReportPortalException.class)
  public ResponseEntity<ProblemDetail> handleReportPortalException(ReportPortalException e, WebRequest request) {
    log.error("ReportPortalException occurred", e);

    var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    problem.setType(URI.create(TYPE_URL + e.getErrorType().name()));
    problem.setTitle("Report Portal Exception");
    problem.setDetail(e.getMessage());
    problem.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
    problem.setProperty("timestamp", Instant.now());

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problem);
  }

  /**
   * Handles general exceptions and returns a {@link ProblemDetail} response.
   *
   * @param e       The exception to handle.
   * @param request The current web request.
   * @return A {@link ResponseEntity} containing the {@link ProblemDetail}.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleGeneralException(Exception e, WebRequest request) {
    log.error("Unhandled exception", e);

    var problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    problem.setType(URI.create(TYPE_URL + HttpStatus.INTERNAL_SERVER_ERROR.name()));
    problem.setTitle(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    problem.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
    problem.setProperty("timestamp", Instant.now());

    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problem);
  }
}
