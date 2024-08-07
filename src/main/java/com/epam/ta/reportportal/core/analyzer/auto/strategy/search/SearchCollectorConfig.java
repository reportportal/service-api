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

package com.epam.ta.reportportal.core.analyzer.auto.strategy.search;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Configuration
public class SearchCollectorConfig {

  private ApplicationContext applicationContext;

  public SearchCollectorConfig(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Bean("searchModeMapping")
  public Map<SearchLogsMode, SearchLaunchesCollector> getSearchModeMapping() {
    return ImmutableMap.<SearchLogsMode, SearchLaunchesCollector>builder()
        .put(SearchLogsMode.BY_LAUNCH_NAME,
            applicationContext.getBean(LaunchNameCollector.class)
        )
        .put(SearchLogsMode.CURRENT_LAUNCH,
            applicationContext.getBean(CurrentLaunchCollector.class))
        .put(SearchLogsMode.FILTER, applicationContext.getBean(FilterCollector.class))
        .build();
  }

  @Bean
  public SearchCollectorFactory searchCollectorFactory() {
    return new SearchCollectorFactory(getSearchModeMapping());
  }
}
