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

package com.epam.reportportal.base.core.analyzer.auto.strategy.analyze;

import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
@RequiredArgsConstructor
public class ToInvestigateCollector implements AnalyzeItemsCollector {

  private final TestItemRepository testItemRepository;

  @Override
  public List<Long> collectItems(Long projectId, Long launchId, Long userId, String userLogin) {
    return testItemRepository.findAllInIssueGroupByLaunch(launchId, TestItemIssueGroup.TO_INVESTIGATE)
        .stream()
        .filter(it -> !it.getItemResults().getIssue().getIgnoreAnalyzer())
        .map(TestItem::getItemId)
        .toList();
  }
}
