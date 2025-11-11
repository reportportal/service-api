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

package com.epam.reportportal.core.item.validator.parent;

import static com.epam.reportportal.infrastructure.persistence.commons.Predicates.equalTo;
import static com.epam.reportportal.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.infrastructure.rules.exception.ErrorType.BAD_REQUEST_ERROR;

import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.reporting.StartTestItemRQ;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

@Service
public class NestedStepConditionValidator implements ParentItemValidator, Ordered {

  @Override
  public void validate(StartTestItemRQ rq, TestItem parent) {
    if (!parent.isHasStats()) {
      expect(rq.isHasStats(), equalTo(Boolean.FALSE)).verify(BAD_REQUEST_ERROR,
          Suppliers.formattedSupplier(
                  "Unable to add a not nested step item, because parent item with ID = '{}' is a nested step",
                  parent.getItemId()
              )
              .get()
      );
    }
  }

  @Override
  public int getOrder() {
    return 1;
  }
}
