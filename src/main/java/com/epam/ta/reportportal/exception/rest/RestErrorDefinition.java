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

package com.epam.ta.reportportal.exception.rest;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.exception.message.ExceptionMessageBuilder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * REST Error template. Created to be able to configure error templates in Spring's IoC container
 *
 * @author Andrei Varabyeu
 */
public class RestErrorDefinition<T extends Exception> {

  @Getter
  private final HttpStatus httpStatus;
  @Getter
  private final com.epam.reportportal.rules.exception.ErrorType error;
  private final ExceptionMessageBuilder<T> exceptionMessageBuilder;

  /**
   * Creates a new REST error definition with specified HTTP status, error type and message builder.
   *
   * @param httpStatus              HTTP status code to be returned
   * @param error                   Error type defining the specific error category
   * @param exceptionMessageBuilder Builder for constructing error messages from exceptions
   */
  public RestErrorDefinition(HttpStatus httpStatus, ErrorType error,
      ExceptionMessageBuilder<T> exceptionMessageBuilder) {
    super();
    this.httpStatus = httpStatus;
    this.error = error;
    this.exceptionMessageBuilder = exceptionMessageBuilder;
  }

  /**
   * Alternative constructor that accepts the HTTP status as an integer.
   *
   * @param httpStatus              HTTP status code as an integer
   * @param error                   Error type defining the specific error category
   * @param exceptionMessageBuilder Builder for constructing error messages from exceptions
   */
  public RestErrorDefinition(int httpStatus, com.epam.reportportal.rules.exception.ErrorType error,
      ExceptionMessageBuilder<T> exceptionMessageBuilder) {
    this(HttpStatus.valueOf(httpStatus), error, exceptionMessageBuilder);
  }

  /**
   * Builds an error message from the provided exception using the configured message builder.
   *
   * @param e The exception from which to build the error message
   * @return A string containing the formatted error message
   */
  public String getExceptionMessage(T e) {
    return exceptionMessageBuilder.buildMessage(e);
  }

  public ExceptionMessageBuilder<? extends Exception> getExceptionMessageBuilder() {
    return exceptionMessageBuilder;
  }
}
