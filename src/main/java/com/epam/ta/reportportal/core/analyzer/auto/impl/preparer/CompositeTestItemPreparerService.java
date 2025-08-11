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

package com.epam.ta.reportportal.core.analyzer.auto.impl.preparer;

import static java.util.stream.Collectors.partitioningBy;

import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.reportportal.model.analyzer.IndexTestItem;
import com.epam.reportportal.model.project.AnalyzerConfig;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Composite service that delegates test item preparation to appropriate strategies
 * based on analyzer configuration and test item characteristics.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class CompositeTestItemPreparerService implements TestItemPreparerService {

  private final TestItemPreparationStrategy standardStrategy;
  private final TestItemPreparationStrategy retryStrategy;

  public CompositeTestItemPreparerService(
      @Qualifier("standardTestItemPreparerService") TestItemPreparationStrategy standardStrategy,
      @Qualifier("testItemWithRetriesPreparerService") TestItemPreparationStrategy retryStrategy) {
    this.standardStrategy = standardStrategy;
    this.retryStrategy = retryStrategy;
  }

  @Override
  public List<IndexTestItem> prepare(Long launchId, Collection<TestItem> testItems) {
    return standardStrategy.prepare(launchId, testItems);
  }

  @Override
  public List<IndexTestItem> prepare(Long launchId) {
    // Delegate to StandardTestItemPreparerService for loading from repository
    if (standardStrategy instanceof StandardTestItemPreparerService) {
      return ((StandardTestItemPreparerService) standardStrategy).prepareFromRepository(launchId);
    }
    return standardStrategy.prepare(launchId, List.of());
  }

  /**
   * Prepares test items with analyzer configuration consideration.
   * If largestRetryPriority is enabled, separates items by retry status
   * and applies appropriate strategies.
   *
   * @param launchId       the launch ID
   * @param testItems      collection of test items to prepare
   * @param analyzerConfig analyzer configuration
   * @return prepared list of {@link IndexTestItem} for indexing
   */
  public List<IndexTestItem> prepare(Long launchId, Collection<TestItem> testItems,
      AnalyzerConfig analyzerConfig) {
    if (analyzerConfig != null && analyzerConfig.isLargestRetryPriority()) {
      return prepareWithRetryPriority(launchId, testItems);
    }
    return prepare(launchId, testItems);
  }

  private List<IndexTestItem> prepareWithRetryPriority(Long launchId,
      Collection<TestItem> testItems) {
    Map<Boolean, List<TestItem>> partitionedItems = testItems.stream()
        .collect(partitioningBy(TestItem::isHasRetries));

    List<IndexTestItem> result = new ArrayList<>();

    List<TestItem> itemsWithRetries = partitionedItems.get(true);
    if (!itemsWithRetries.isEmpty()) {
      result.addAll(retryStrategy.prepare(launchId, itemsWithRetries));
    }

    List<TestItem> itemsWithoutRetries = partitionedItems.get(false);
    if (!itemsWithoutRetries.isEmpty()) {
      result.addAll(standardStrategy.prepare(launchId, itemsWithoutRetries));
    }

    return result;
  }
}
