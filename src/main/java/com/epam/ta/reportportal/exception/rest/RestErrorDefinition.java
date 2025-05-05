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

  public RestErrorDefinition(HttpStatus httpStatus, ErrorType error,
      ExceptionMessageBuilder<T> exceptionMessageBuilder) {
    super();
    this.httpStatus = httpStatus;
    this.error = error;
    this.exceptionMessageBuilder = exceptionMessageBuilder;
  }

  public RestErrorDefinition(int httpStatus, com.epam.reportportal.rules.exception.ErrorType error,
      ExceptionMessageBuilder<T> exceptionMessageBuilder) {
    this(HttpStatus.valueOf(httpStatus), error, exceptionMessageBuilder);
  }

  public String getExceptionMessage(T e) {
    return exceptionMessageBuilder.buildMessage(e);
  }

  public ExceptionMessageBuilder<? extends Exception> getExceptionMessageBuilder() {
    return exceptionMessageBuilder;
  }
}
