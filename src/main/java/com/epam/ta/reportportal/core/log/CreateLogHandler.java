/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.log;

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;

import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.ws.reporting.EntryCreatedAsyncRS;
import com.epam.ta.reportportal.ws.reporting.SaveLogRQ;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.springframework.web.multipart.MultipartFile;

public interface CreateLogHandler {

  /**
   * Creates a new Log
   *
   * @param createLogRQ    Log details
   * @param file           file with log
   * @param membershipDetails Membership details
   * @return EntryCreatedRS
   */
  @Nonnull
  EntryCreatedAsyncRS createLog(@Nonnull SaveLogRQ createLogRQ, @Nullable MultipartFile file,
      @Nullable MembershipDetails membershipDetails);

  /**
   * Validates business rules related to test item of this log
   *
   * @param saveLogRQ Save log request
   */
  default void validate(SaveLogRQ saveLogRQ) {
    // todo : seems we need to loosen (throw out) this time check
//		expect(saveLogRQ.getLogTime(), Preconditions.sameTimeOrLater(testItem.getStartTime())).verify(
//				ErrorType.LOGGING_IS_NOT_ALLOWED,
//				Suppliers.formattedSupplier("Log has incorrect log time. Log time should be after parent item's start time.")
//		);
    expect(LogLevel.toCustomLogLevel(saveLogRQ.getLevel()), Predicates.notNull()).verify(
        ErrorType.BAD_SAVE_LOG_REQUEST,
        Suppliers.formattedSupplier("Cannot convert '{}' to valid 'LogLevel'", saveLogRQ.getLevel())
    );
  }
}
