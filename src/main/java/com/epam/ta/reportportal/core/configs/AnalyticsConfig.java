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

package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.core.analytics.AnalyticsObjectType;
import com.epam.ta.reportportal.core.analytics.AnalyticsStrategy;
import com.epam.ta.reportportal.core.analytics.AnalyzerManualStart;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The AnalyticsConfig class holds the configuration for the different analytics strategies. *
 *
 * @author Siarhei Hrabko
 */
@Configuration
public class AnalyticsConfig {

  private final AnalyzerManualStart analyzerManualStart;

  @Autowired
  AnalyticsConfig(AnalyzerManualStart analyzerManualStart) {
    this.analyzerManualStart = analyzerManualStart;
  }

  /**
   * This method returns a map of AnalyticsObjectType to AnalyticsStrategy.
   *
   * @return map of AnalyticsObjectType to AnalyticsStrategy.
   */
  @Bean
  public Map<AnalyticsObjectType, AnalyticsStrategy> analyticsStrategies() {
    return ImmutableMap.<AnalyticsObjectType, AnalyticsStrategy>builder()
        .put(AnalyticsObjectType.ANALYZER_MANUAL_START, analyzerManualStart)
        .build();
  }
}
