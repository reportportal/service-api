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

import static com.epam.reportportal.infrastructure.persistence.commons.Predicates.isNull;
import static com.epam.reportportal.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.infrastructure.rules.exception.ErrorType.UNABLE_TO_SAVE_CHILD_ITEM_FOR_THE_RETRY;

import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.reporting.StartTestItemRQ;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

@Service
public class RetryConditionValidator implements ParentItemValidator, Ordered {

  @Override
  public void validate(StartTestItemRQ rq, TestItem parent) {
    if (rq.isHasStats()) {
      expect(parent.getRetryOf(), isNull()::test).verify(UNABLE_TO_SAVE_CHILD_ITEM_FOR_THE_RETRY,
          parent.getItemId());
    }
  }

  @Override
  public int getOrder() {
    return 2;
  }
}
