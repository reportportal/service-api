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

package com.epam.reportportal.base.ws.converter.utils.item.updater;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.reporting.TestItemResource;
import com.epam.reportportal.base.ws.converter.utils.ResourceUpdater;
import java.util.Map;

/**
 * Updates a {@link TestItemResource} by setting the analysis owner full name. The analysis owner is the user who last
 * performed analysis actions on the test item.
 *
 */
public class AnalysisOwnerUpdater implements ResourceUpdater<TestItemResource> {

  private final Map<Long, String> analysisOwnerMapping;

  private AnalysisOwnerUpdater(Map<Long, String> analysisOwnerMapping) {
    this.analysisOwnerMapping = analysisOwnerMapping;
  }

  /**
   * Factory method to create an {@code AnalysisOwnerUpdater} instance.
   *
   * @param mapping A mapping of test item IDs to analysis owner full names.
   * @return An instance of {@code AnalysisOwnerUpdater}.
   */
  public static AnalysisOwnerUpdater of(Map<Long, String> mapping) {
    return new AnalysisOwnerUpdater(mapping);
  }

  /**
   * Updates the {@code TestItemResource} by setting its {@code analysisOwner} property using the predefined mapping.
   *
   * @param resource The resource to be updated.
   */
  @Override
  public void updateResource(TestItemResource resource) {
    ofNullable(analysisOwnerMapping.get(resource.getItemId()))
        .ifPresent(resource::setAnalysisOwner);
  }
}
