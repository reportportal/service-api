package com.epam.ta.reportportal.ws.controller;

import com.epam.reportportal.rules.exception.ErrorRS;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.exception.ExceptionMappings;
import com.epam.ta.reportportal.exception.forwarding.ClientResponseForwardingExceptionHandler;
import com.epam.ta.reportportal.exception.forwarding.ResponseForwardingException;
import com.epam.ta.reportportal.exception.rest.DefaultErrorResolver;
import com.epam.ta.reportportal.exception.rest.ReportPortalExceptionResolver;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * Global exception handler for the application's REST controllers. Provides centralized handling of various exceptions
 * and maps them to appropriate HTTP responses.
 */
@RestControllerAdvice(value = {"com.epam.ta.reportportal.ws.controller",
    "com.epam.ta.reportportal.reporting.async.controller"})
@Log4j2
public class ErrorHandlingController {

  private final DefaultErrorResolver defaultErrorResolver;
  @Autowired
  private ReportPortalExceptionResolver reportPortalExceptionResolver;
  @Autowired
  ClientResponseForwardingExceptionHandler clientResponseForwardingExceptionHandler;

  public ErrorHandlingController() {
    this.defaultErrorResolver = new DefaultErrorResolver(ExceptionMappings.DEFAULT_MAPPING);
  }


  @ExceptionHandler({
      MethodArgumentNotValidException.class,
      MethodArgumentTypeMismatchException.class,
      HttpMessageNotReadableException.class,
      MissingServletRequestPartException.class,
      MissingServletRequestParameterException.class,
      RestClientException.class}
  )
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorRS handleCustomBadRequest(Exception exception) {
    return defaultErrorResolver.resolveError(exception).errorRs();
  }

  @ExceptionHandler(ReportPortalException.class)
  public ResponseEntity<ErrorRS> handle(ReportPortalException exception) {
    var rs = reportPortalExceptionResolver.resolveError(exception);
    return ResponseEntity
        .status(rs.httpStatus())
        .body(rs.errorRs());
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ErrorRS handle(AccessDeniedException exception) {
    return defaultErrorResolver.resolveError(exception).errorRs();
  }

  @ExceptionHandler({BadCredentialsException.class, LockedException.class})
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ErrorRS handle(BadCredentialsException exception) {
    return defaultErrorResolver.resolveError(exception).errorRs();
  }

  @ExceptionHandler(ResponseForwardingException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<String> handle(ResponseForwardingException exception) {
    return clientResponseForwardingExceptionHandler.resolveException(exception);
  }


  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorRS handle(Exception exception) {
    return defaultErrorResolver.resolveError(exception).errorRs();
  }

}
