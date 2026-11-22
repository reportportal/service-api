/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.reporting.async.exception;

/**
 * A LOG message failed after its log row was committed. Handling the message again would insert the log a second time
 * ({@code log.uuid} is not unique), so the failure is never retried. For the same reason a message parked with this
 * exception must not be replayed from the parking lot as it is.
 */
public class LogAlreadySavedException extends RuntimeException {

  public LogAlreadySavedException(String logUuid, RuntimeException cause) {
    super("Log '" + logUuid + "' is saved, but a later step failed: " + cause.getMessage(), cause);
  }
}
