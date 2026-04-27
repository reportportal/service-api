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

package com.epam.reportportal.base.core.item.impl.merge.strategy;

import com.epam.reportportal.base.core.item.merge.LaunchMergeStrategy;
import java.util.Map;

/**
 * Produces a merge strategy for the requested type.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class LaunchMergeFactory {

  private Map<MergeStrategyType, LaunchMergeStrategy> mergeStrategyMapping;

  public LaunchMergeFactory(Map<MergeStrategyType, LaunchMergeStrategy> mergeStrategyMapping) {
    this.mergeStrategyMapping = mergeStrategyMapping;
  }

  public LaunchMergeStrategy getLaunchMergeStrategy(MergeStrategyType type) {
    return mergeStrategyMapping.get(type);
  }
}
