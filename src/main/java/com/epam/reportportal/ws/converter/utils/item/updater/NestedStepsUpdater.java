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

package com.epam.reportportal.ws.converter.utils.item.updater;

import com.epam.reportportal.reporting.TestItemResource;
import com.epam.reportportal.ws.converter.utils.ResourceUpdater;
import java.util.Map;

/**
 * Updates a {@link TestItemResource} by setting whether the associated test item has nested steps.
 *
 */
public class NestedStepsUpdater implements ResourceUpdater<TestItemResource> {

  private final Map<Long, Boolean> hasNestedStepsMapping;

  private NestedStepsUpdater(Map<Long, Boolean> hasNestedStepsMapping) {
    this.hasNestedStepsMapping = hasNestedStepsMapping;
  }

  /**
   * Factory method to create a {@code NestedStepsUpdater} instance.
   *
   * @param mapping A mapping of test item IDs to boolean values indicating nested step status.
   * @return An instance of {@code NestedStepsUpdater}.
   */
  public static NestedStepsUpdater of(Map<Long, Boolean> mapping) {
    return new NestedStepsUpdater(mapping);
  }

  /**
   * Updates the {@code TestItemResource} by setting its {@code hasNestedSteps} property using the predefined mapping.
   *
   * @param resource The resource to be updated.
   */
  @Override
  public void updateResource(TestItemResource resource) {
    Boolean hasNestedSteps = hasNestedStepsMapping.get(resource.getItemId());
    if (hasNestedSteps != null) {
      resource.setHasNestedSteps(hasNestedSteps);
    }
  }
}
