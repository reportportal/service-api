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

package com.epam.reportportal.base.core.project.validator.attribute;

import static com.epam.reportportal.base.infrastructure.persistence.commons.Predicates.equalTo;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.ProjectAttributeEnum.FOREVER_ALIAS;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.ProjectAttributeEnum;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.List;
import java.util.Map;

public class DelayBoundValidator {

  private final List<DelayBoundLessRule> rules;

  public DelayBoundValidator(List<DelayBoundLessRule> rules) {
    this.rules = rules;
  }

  public void validate(Map<String, String> currentAttributes,
      Map<ProjectAttributeEnum, Long> newAttributes) {
    rules.forEach(rule -> {
      Long lowerDelay = ofNullable(newAttributes.get(rule.getLower())).orElseGet(
          () -> getCurrentDelay(currentAttributes,
              rule.getLower()
          ));
      Long higherDelay = ofNullable(newAttributes.get(rule.getHigher())).orElseGet(
          () -> getCurrentDelay(currentAttributes,
              rule.getHigher()
          ));

      BusinessRule.expect(lowerDelay <= higherDelay, equalTo(Boolean.TRUE))
          .verify(BAD_REQUEST_ERROR,
              Suppliers.formattedSupplier("Delay of '{}' should not be higher than '{}'",
                  rule.getLower().getAttribute(),
                  rule.getHigher().getAttribute()
              ).toString()
          );
    });
  }

  private Long getCurrentDelay(Map<String, String> currentAttributes,
      ProjectAttributeEnum attribute) {
    return ofNullable(currentAttributes.get(attribute.getAttribute())).map(this::resolveDelay)
        .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
            Suppliers.formattedSupplier("Attribute - {} was not found"),
            attribute.getAttribute()
        ));
  }

  private Long resolveDelay(String value) {
    try {
      return FOREVER_ALIAS.equals(value) ? Long.MAX_VALUE : Long.parseLong(value);
    } catch (NumberFormatException exc) {
      throw new ReportPortalException(BAD_REQUEST_ERROR, exc.getMessage());
    }
  }
}
