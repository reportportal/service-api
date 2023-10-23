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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.ta.reportportal.core.events.activity.item.IssueResolvedEvent;
import com.epam.ta.reportportal.core.launch.cluster.ClusterGenerator;
import com.epam.ta.reportportal.core.launch.cluster.config.GenerateClustersConfig;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class TestItemUniqueErrorAnalysisRunnerTest {

  private final ClusterGenerator clusterGenerator = mock(ClusterGenerator.class);
  private final TestItemUniqueErrorAnalysisRunner runner = new TestItemUniqueErrorAnalysisRunner(
      clusterGenerator);

  @Test
  void shouldAnalyzeWhenEnabled() {

    final IssueResolvedEvent event = new IssueResolvedEvent(3L, 2L, 1L);

    final Map<String, String> projectConfig = ImmutableMap.<String, String>builder()
        .put(ProjectAttributeEnum.AUTO_UNIQUE_ERROR_ANALYZER_ENABLED.getAttribute(), "true")
        .put(ProjectAttributeEnum.UNIQUE_ERROR_ANALYZER_REMOVE_NUMBERS.getAttribute(), "true")
        .build();

    runner.handle(event, projectConfig);

    final ArgumentCaptor<GenerateClustersConfig> configArgumentCaptor = ArgumentCaptor.forClass(
        GenerateClustersConfig.class);
    verify(clusterGenerator, times(1)).generate(configArgumentCaptor.capture());

    final GenerateClustersConfig config = configArgumentCaptor.getValue();

    assertEquals(event.getLaunchId(), config.getEntityContext().getLaunchId());
    assertEquals(event.getProjectId(), config.getEntityContext().getProjectId());
    assertEquals(event.getItemId(), config.getEntityContext().getItemIds().get(0));
    assertTrue(config.isForUpdate());
    assertTrue(config.isCleanNumbers());
  }

  @Test
  void shouldNotAnalyzeWhenDisabled() {

    final IssueResolvedEvent event = new IssueResolvedEvent(3L, 2L, 1L);

    final Map<String, String> projectConfig = ImmutableMap.<String, String>builder()
        .put(ProjectAttributeEnum.AUTO_UNIQUE_ERROR_ANALYZER_ENABLED.getAttribute(), "false")
        .put(ProjectAttributeEnum.UNIQUE_ERROR_ANALYZER_REMOVE_NUMBERS.getAttribute(), "true")
        .build();

    runner.handle(event, projectConfig);

    verify(clusterGenerator, times(0)).generate(any(GenerateClustersConfig.class));

  }

}