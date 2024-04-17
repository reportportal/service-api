/*
 * Copyright 2021 EPAM Systems
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

package com.epam.ta.reportportal.core.item.validator.state;

import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.ta.reportportal.entity.item.TestItem;
import java.util.Objects;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class NotRetryValidator implements TestItemValidator, Ordered {

  @Override
  public boolean validate(TestItem item) {
    return Objects.isNull(item.getRetryOf()) && Objects.nonNull(item.getLaunchId());
  }

  @Override
  public String provide(TestItem item) {
    return Suppliers.formattedSupplier("Test item = {} is a retry", item.getItemId()).get();
  }

  @Override
  public int getOrder() {
    return 2;
  }
}
