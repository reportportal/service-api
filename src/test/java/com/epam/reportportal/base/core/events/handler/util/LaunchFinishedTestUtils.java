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

package com.epam.reportportal.base.core.events.handler.util;

import com.epam.reportportal.base.infrastructure.persistence.entity.attribute.Attribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LogicalOperator;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.ProjectAttributeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.SendCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.email.SenderCase;
import com.google.common.base.Suppliers;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class LaunchFinishedTestUtils {

  private static Supplier<Set<String>> recipientsSupplier = Suppliers.memoize(LaunchFinishedTestUtils::getRecipients);
  private static Supplier<Set<String>> launchNamesSupplier = Suppliers.memoize(LaunchFinishedTestUtils::getLaunchNames);

  public static Set<ProjectAttribute> getProjectAttributes(Map<ProjectAttributeEnum, String> mapping) {
    return mapping.entrySet().stream().map(entry -> {
      Attribute attribute = new Attribute();
      attribute.setName(entry.getKey().getAttribute());
      ProjectAttribute projectAttribute = new ProjectAttribute();
      projectAttribute.setAttribute(attribute);
      projectAttribute.setValue(entry.getValue());
      return projectAttribute;
    }).collect(Collectors.toSet());
  }

  public static Set<SenderCase> getSenderCases() {
    return Arrays.stream(SendCase.values())
        .map(sc -> new SenderCase("rule", recipientsSupplier.get(), launchNamesSupplier.get(),
            Collections.emptySet(), sc, true, "email", LogicalOperator.AND))
        .collect(Collectors.toSet());
  }

  private static Set<String> getRecipients() {
    return Sets.newHashSet("first@mail.com", "second@mail.com");
  }

  private static Set<String> getLaunchNames() {
    return Sets.newHashSet("name1", "name2");
  }
}
