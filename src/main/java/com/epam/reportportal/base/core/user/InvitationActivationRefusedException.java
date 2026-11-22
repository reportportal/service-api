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

package com.epam.reportportal.base.core.user;

import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.io.Serial;

/**
 * Refusal of an invitation activation for an address that already belongs to an account, when the caller did not prove
 * ownership of an active internal account. One message for every reason (wrong password, external account, inactive
 * account), so the response does not tell which.
 *
 * <p>Thrown only after the failed attempt has been recorded on the invitation and before anything else is written, so
 * the activation transaction commits instead of rolling back ({@code noRollbackFor}) and the failure count survives.
 */
public class InvitationActivationRefusedException extends ReportPortalException {

  @Serial
  private static final long serialVersionUID = 1L;

  public static final String MESSAGE =
      "The invitation cannot be accepted with these credentials. If an account with this email already exists,"
          + " enter its current password, or ask the inviter to add the existing account.";

  public InvitationActivationRefusedException() {
    super(ErrorType.ACCESS_DENIED, MESSAGE);
  }
}
