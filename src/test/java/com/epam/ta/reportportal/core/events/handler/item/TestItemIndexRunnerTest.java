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

package com.epam.ta.reportportal.core.events.handler.item;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.events.activity.item.IssueResolvedEvent;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.reportportal.model.project.AnalyzerConfig;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class TestItemIndexRunnerTest {

  private final LogIndexer logIndexer = mock(LogIndexer.class);

  private final TestItemIndexRunner runner = new TestItemIndexRunner(logIndexer);

  @Test
  void shouldInvokeIndexer() {

    final IssueResolvedEvent event = new IssueResolvedEvent(3L, 2L, 1L);

    final Map<String, String> projectConfig = ImmutableMap.<String, String>builder()
        .put(ProjectAttributeEnum.AUTO_ANALYZER_ENABLED.getAttribute(), "false")
        .build();

    final List<Long> itemIds = List.of(event.itemId());

    runner.handle(event, projectConfig);

    verify(logIndexer, times(1)).indexItemsLogs(eq(event.projectId()),
        eq(event.launchId()),
        eq(itemIds),
        any(AnalyzerConfig.class)
    );
  }

}
