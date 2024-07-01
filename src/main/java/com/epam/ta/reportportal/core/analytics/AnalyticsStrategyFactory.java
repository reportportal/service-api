/*
 * Copyright 2024 EPAM Systems
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

package com.epam.ta.reportportal.core.analytics;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory for creating appropriate AnalyticsStrategy based on AnalyticsObjectType.
 *
 * @author Siarhei Hrabko
 */
@Component
public class AnalyticsStrategyFactory {

  private final Map<AnalyticsObjectType, AnalyticsStrategy> analyticsStrategies;

  @Autowired
  public AnalyticsStrategyFactory(Map<AnalyticsObjectType, AnalyticsStrategy> analyticsStrategies) {
    this.analyticsStrategies = analyticsStrategies;
  }

  public AnalyticsStrategy findStrategy(AnalyticsObjectType analyticsObjectType) {
    return analyticsStrategies.get(analyticsObjectType);
  }

}
