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
package com.epam.ta.reportportal.core.events.handler.item;

import com.epam.ta.reportportal.core.analyzer.auto.AnalyzerService;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.events.activity.item.TestItemFinishedEvent;
import com.epam.ta.reportportal.core.events.handler.ConfigurableEventHandler;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
@Component
public class TestItemIndexingRunner implements
    ConfigurableEventHandler<TestItemFinishedEvent, Map<String, String>> {

  private final LogIndexer logIndexer;

  public TestItemIndexingRunner(LogIndexer logIndexer) {
    this.logIndexer = logIndexer;
  }

  @Override
  public void handle(TestItemFinishedEvent testItemFinishedEvent,
      Map<String, String> projectConfig) {
    final AnalyzerConfig analyzerConfig = AnalyzerUtils.getAnalyzerConfig(projectConfig);
    TestItem testItem = testItemFinishedEvent.getTestItem();
    Launch launch = testItemFinishedEvent.getLaunch();
    logIndex(testItem, launch, testItemFinishedEvent.getProjectId(), analyzerConfig);
  }

  private void logIndex(TestItem testItem, Launch launch, Long projectId, AnalyzerConfig config) {
    logIndexer.indexItemLog(projectId, launch, testItem, config);
  }
}
