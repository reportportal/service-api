/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze;

import static java.util.stream.Collectors.toList;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.TestItem;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
@Service
public class IgnoreImmediateCollector implements AnalyzeItemsCollector {

  protected static final String IMMEDIATE_AUTO_ANALYSIS = "immediateAutoAnalysis";

  private TestItemRepository testItemRepository;

  @Autowired
  public IgnoreImmediateCollector(TestItemRepository testItemRepository) {
    this.testItemRepository = testItemRepository;
  }

  @Override
  public List<Long> collectItems(Long projectId, Long launchId, ReportPortalUser user) {
    return testItemRepository.findItemsForAnalyze(launchId)
        .stream()
        .filter(ti -> !ti.getItemResults().getIssue().getIgnoreAnalyzer())
        .filter(this::skipImmediateAA)
        .map(TestItem::getItemId)
        .collect(toList());
  }

  private boolean skipImmediateAA(TestItem item) {
    return item.getAttributes().stream()
        .filter(at -> !at.getTestItem().getItemResults().getIssue().getIgnoreAnalyzer())
        .noneMatch(at -> IMMEDIATE_AUTO_ANALYSIS.equals(at.getKey()) && Boolean.parseBoolean(
            at.getValue()) && at.isSystem());
  }
}
