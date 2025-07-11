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

package com.epam.ta.reportportal.util.email;

import static com.epam.reportportal.model.ValidationConstraints.MAX_LOGIN_LENGTH;
import static com.epam.reportportal.model.ValidationConstraints.MAX_NAME_LENGTH;
import static com.epam.reportportal.model.ValidationConstraints.MIN_LOGIN_LENGTH;
import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.rules.exception.ErrorType.USER_NOT_FOUND;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.entity.project.ProjectUtils.getOwner;
import static com.google.common.base.Strings.isNullOrEmpty;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.util.UserUtils;
import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import com.google.common.base.Function;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

/**
 * Validates email and launch name rules.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class EmailRulesValidator {

  private EmailRulesValidator() {
  }

  /**
   * Validates email address.
   *
   * @param project   Project
   * @param recipient Recipient email address
   */
  public static void validateRecipient(Project project, String recipient) {
    expect(recipient, notNull()).verify(BAD_REQUEST_ERROR,
        formattedSupplier("Provided recipient email '{}' is invalid", recipient));
    if (recipient.contains("@")) {
      expect(EmailValidator.getInstance().isValid(recipient), equalTo(true))
          .verify(BAD_REQUEST_ERROR,
              formattedSupplier("Provided recipient email '{}' is invalid", recipient));
    } else {
      final String login = recipient.trim();
      expect(MIN_LOGIN_LENGTH <= login.length() && login.length() <= MAX_LOGIN_LENGTH,
          equalTo(true)).verify(BAD_REQUEST_ERROR,
          "Acceptable login length  [" + MIN_LOGIN_LENGTH + ".." + MAX_LOGIN_LENGTH + "]"
      );
      if (!getOwner().equals(login)) {
        expect(ProjectUtils.doesHaveUser(project, login.toLowerCase()), equalTo(true)).verify(
            USER_NOT_FOUND,
            login,
            String.format("User not found in project %s", project.getId())
        );
      }
    }
  }

  /**
   * Validate launch name.
   *
   * @param name Launch name
   */
  public static void validateLaunchName(String name) {
    expect(StringUtils.isBlank(name), equalTo(false)).verify(BAD_REQUEST_ERROR,
        "Launch name values cannot be empty. Please specify it or not include in request."
    );
    expect(name.length() <= MAX_NAME_LENGTH, equalTo(true)).verify(BAD_REQUEST_ERROR,
        formattedSupplier(
            "One of provided launch names '{}' is too long. Acceptable name length is [1..256]",
            name)
    );
  }

  /**
   * Validate launch attribute.
   *
   * @param attribute Launch attribute
   */
  public static void validateLaunchAttribute(ItemAttributeResource attribute) {
    expect(attribute, notNull()).verify(ErrorType.BAD_REQUEST_ERROR,
        "Launch attribute cannot be null.");
    expect(isNullOrEmpty(attribute.getValue()), equalTo(false)).verify(BAD_REQUEST_ERROR,
        "Attribute' values cannot be empty. Please specify them or do not include in a request."
    );
  }

  public static final Function<String, String> NORMALIZE_EMAIL = email ->
      Optional.ofNullable(email)
          .map(String::trim)
          .map(EntityUtils::normalizeId)
          .filter(UserUtils::isEmailValid)
          .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR, "wrong email: " + email));

}
