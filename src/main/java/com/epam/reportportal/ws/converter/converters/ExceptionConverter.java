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

package com.epam.reportportal.ws.converter.converters;

import com.epam.reportportal.infrastructure.rules.exception.ErrorRS;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import java.util.function.Function;

public class ExceptionConverter {

  private ExceptionConverter() {
    //static only
  }

  public static final Function<ReportPortalException, ErrorRS> TO_ERROR_RS = ex -> {
    ErrorRS errorResponse = new ErrorRS();
    errorResponse.setErrorType(ex.getErrorType());
    errorResponse.setMessage(ex.getMessage());
    return errorResponse;
  };
}
