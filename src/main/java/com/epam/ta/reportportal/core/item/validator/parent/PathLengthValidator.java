/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.core.item.validator.parent;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

/**
 * @author Andrei Piankouski
 */
@Service
public class PathLengthValidator implements ParentItemValidator, Ordered {

  private static final int MAX_PATH_LENGTH = 64;

  @Override
  public void validate(StartTestItemRQ rq, TestItem parent) {
    expect(parent.getPath().split("\\.").length >= MAX_PATH_LENGTH,
        Predicates.equalTo(false)).verify(BAD_REQUEST_ERROR,
        "Exceeded nesting limit for test item. Max limit is " + MAX_PATH_LENGTH + "."
    );
  }

  @Override
  public int getOrder() {
    return 4;
  }
}
