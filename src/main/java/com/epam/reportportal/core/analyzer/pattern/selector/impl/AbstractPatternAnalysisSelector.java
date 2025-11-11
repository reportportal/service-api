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

package com.epam.reportportal.core.analyzer.pattern.selector.impl;

import com.epam.reportportal.core.analyzer.pattern.selector.PatternAnalysisSelector;
import com.epam.reportportal.core.log.LogService;
import com.epam.reportportal.infrastructure.persistence.dao.TestItemRepository;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractPatternAnalysisSelector implements PatternAnalysisSelector {

  protected final TestItemRepository testItemRepository;
  protected final LogService logService;

  public AbstractPatternAnalysisSelector(TestItemRepository testItemRepository,
      LogService logService) {
    this.testItemRepository = testItemRepository;
    this.logService = logService;
  }

  protected abstract List<Long> getItemsWithMatches(String pattern, Set<Long> itemIds);

  protected abstract List<Long> getItemsWithNestedStepsMatches(Long launchId, String pattern,
      List<Long> itemsWithNestedSteps);

  @Override
  public List<Long> selectItemsByPattern(Long launchId, Collection<Long> itemIds, String pattern) {
    final Set<Long> sourceIds = Sets.newHashSet(itemIds);
    final List<Long> itemsWithMatchedLogs = getItemsWithMatches(pattern, sourceIds);

    itemsWithMatchedLogs.forEach(sourceIds::remove);

    if (CollectionUtils.isNotEmpty(sourceIds)) {
      final List<Long> itemsWithNestedSteps = testItemRepository.selectIdsByHasDescendants(
          sourceIds);
      if (CollectionUtils.isNotEmpty(itemsWithNestedSteps)) {
        final List<Long> nestedStepsMatches = getItemsWithNestedStepsMatches(launchId, pattern,
            itemsWithNestedSteps);
        itemsWithMatchedLogs.addAll(nestedStepsMatches);
      }
    }

    return itemsWithMatchedLogs;
  }
}
