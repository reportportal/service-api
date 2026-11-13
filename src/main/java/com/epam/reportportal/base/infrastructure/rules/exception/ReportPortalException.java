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

package com.epam.reportportal.base.infrastructure.rules.exception;

import static com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers.trimMessage;

import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;


/**
 * Base Report Portal Exception
 *
 * @author Andrei Varabyeu
 */
public class ReportPortalException extends RuntimeException {

  public static final int MAX_ERROR_MESSAGE_LENGTH = 10000;

  private static final long serialVersionUID = -7599195984281555977L;

  private ErrorType errorType;
  private Object[] parameters; //NOSONAR

  public ReportPortalException(String message) {
    super(message);
  }

  public ReportPortalException(ErrorType errorType, Object... parameters) {
    super(trimMessage(Suppliers.formattedSupplier(errorType.getDescription(), parameters).get(),
        MAX_ERROR_MESSAGE_LENGTH));
    this.errorType = errorType;
    this.parameters = parameters;
  }

  public ReportPortalException(String message, Throwable e) {
    super(trimMessage(message, MAX_ERROR_MESSAGE_LENGTH), e);
  }

  public ErrorType getErrorType() {
    return errorType;
  }

  public Object[] getParameters() {
    return parameters;
  }

}
