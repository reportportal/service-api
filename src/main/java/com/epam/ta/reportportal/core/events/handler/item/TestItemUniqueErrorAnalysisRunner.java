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

import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils.getAnalyzerConfig;
import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils.getUniqueErrorConfig;

import com.epam.ta.reportportal.core.events.activity.item.IssueResolvedEvent;
import com.epam.ta.reportportal.core.events.handler.ConfigurableEventHandler;
import com.epam.ta.reportportal.core.launch.cluster.ClusterGenerator;
import com.epam.ta.reportportal.core.launch.cluster.config.ClusterEntityContext;
import com.epam.ta.reportportal.core.launch.cluster.config.GenerateClustersConfig;
import com.epam.ta.reportportal.model.project.UniqueErrorConfig;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class TestItemUniqueErrorAnalysisRunner
    implements ConfigurableEventHandler<IssueResolvedEvent, Map<String, String>> {

  private final ClusterGenerator clusterGenerator;

  public TestItemUniqueErrorAnalysisRunner(
      @Qualifier("uniqueErrorGenerator") ClusterGenerator clusterGenerator) {
    this.clusterGenerator = clusterGenerator;
  }

  @Override
  public void handle(IssueResolvedEvent event, Map<String, String> projectConfig) {
    final UniqueErrorConfig uniqueErrorConfig = getUniqueErrorConfig(projectConfig);

    if (uniqueErrorConfig.isEnabled()) {
      final GenerateClustersConfig clustersConfig = new GenerateClustersConfig();
      clustersConfig.setForUpdate(true);
      clustersConfig.setCleanNumbers(uniqueErrorConfig.isRemoveNumbers());

      final AnalyzerConfig analyzerConfig = getAnalyzerConfig(projectConfig);
      clustersConfig.setAnalyzerConfig(analyzerConfig);

      final ClusterEntityContext entityContext =
          ClusterEntityContext.of(event.getLaunchId(), event.getProjectId(),
              List.of(event.getItemId())
          );
      clustersConfig.setEntityContext(entityContext);

      clusterGenerator.generate(clustersConfig);
    }
  }
}
