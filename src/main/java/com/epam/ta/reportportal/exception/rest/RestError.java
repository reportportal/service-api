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

import com.epam.reportportal.rules.exception.ErrorRS;
import com.epam.reportportal.rules.exception.ErrorType;
import org.springframework.http.HttpStatus;

/**
 * Rest Error representation. Contains rest error template and real exception data.
 *
 * @param httpStatus HTTP Status
 * @author Andrei Varabyeu
 */
public record RestError(HttpStatus httpStatus, ErrorRS errorRs) {

  /**
   * Builder for Rest Error.
   *
   * @author Andrei Varabyeu
   */
  public static class Builder {

    private HttpStatus status;
    private ErrorType error;
    private String message;
    private String stackTrace;

    /**
     * Sets the HTTP status for the REST error.
     *
     * @param status the HTTP status to set
     * @return the current Builder instance
     */
    public Builder setStatus(HttpStatus status) {
      this.status = status;
      return this;
    }

    /**
     * Sets the error type for the REST error.
     *
     * @param error the error type to set
     * @return the current Builder instance
     */
    public Builder setError(ErrorType error) {
      this.error = error;
      return this;
    }

    /**
     * Sets the error message for the REST error.
     *
     * @param message the error message to set
     * @return the current Builder instance
     */
    public Builder setMessage(String message) {
      this.message = message;
      return this;
    }

    /**
     * Sets the stack trace for the REST error.
     *
     * @param stackTrace the stack trace to set
     * @return the current Builder instance
     */
    public Builder setStackTrace(String stackTrace) {
      this.stackTrace = stackTrace;
      return this;
    }

    public RestError build() {
      ErrorRS errorRs = new ErrorRS();
      errorRs.setMessage(message);
      errorRs.setStackTrace(stackTrace);
      errorRs.setErrorType(error);

      return new RestError(status, errorRs);
    }
  }
}
