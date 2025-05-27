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

import com.epam.reportportal.rules.exception.ReportPortalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Resolver for ReportPortal Exceptions.
 *
 * @author Andrei Varabyeu
 */
@Service
public class ReportPortalExceptionResolver implements ErrorResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReportPortalExceptionResolver.class);

  @Override
  public RestError resolveError(Exception ex) {
    LOGGER.error("ReportPortalExceptionResolver > {}", ex.getMessage(), ex);

    ReportPortalException currentException = (ReportPortalException) ex;
    RestError.Builder builder = new RestError.Builder();
    builder.setMessage(currentException.getMessage())
        // .setStackTrace(errors.toString())
        .setStatus(StatusCodeMapping.getHttpStatus(currentException.getErrorType(), HttpStatus.INTERNAL_SERVER_ERROR))
        .setError(currentException.getErrorType());
    return builder.build();
  }
}
