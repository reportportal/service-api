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

import com.epam.reportportal.model.analyzer.IndexTestItem;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.item.TestItem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Strategy for preparing test items with retries for analysis. This is a placeholder implementation
 * that will be extended with specific retry handling logic in the future.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Slf4j
@Service("testItemWithRetriesPreparerService")
@RequiredArgsConstructor
public class TestItemWithRetriesPreparerService implements TestItemPreparationStrategy {

  private final TestItemRepository testItemRepository;
  private final LogRepository logRepository;
  private final StandardTestItemPreparerService standardTestItemPreparerService;

  @Override
  public List<IndexTestItem> prepare(Long launchId, Collection<TestItem> testItems) {
    List<IndexTestItem> results = new ArrayList<>();
    testItems.forEach(latestRetry -> {
      Long retryWithMaxStepsId = testItemRepository.findIdWithMaxStepsBeforeFailed(
          latestRetry.getItemId());
      if (retryWithMaxStepsId != null) {
        testItemRepository.findById(retryWithMaxStepsId).ifPresent(maxStepsRetry -> {
          log.info("Found retry {} with largest amount of nested steps", maxStepsRetry.getItemId());
          IndexTestItem res = AnalyzerUtils.fromTestItem(maxStepsRetry);
          if (latestRetry.getItemResults().getIssue() != null) {
            res.setIssueTypeLocator(
                latestRetry.getItemResults().getIssue().getIssueType().getLocator());
            res.setAutoAnalyzed(latestRetry.getItemResults().getIssue().getAutoAnalyzed());
          }
          res.setLogs(
              new HashSet<>(logRepository.findNestedLogsWithItemPathPattern(retryWithMaxStepsId,
                  "*." + retryWithMaxStepsId + ".*", LogLevel.ERROR.toInt())));
          results.add(res);
        });
      } else {
        results.addAll(standardTestItemPreparerService.prepare(launchId,
            Collections.singletonList(latestRetry)));
      }
    });
    return results;
  }
}
