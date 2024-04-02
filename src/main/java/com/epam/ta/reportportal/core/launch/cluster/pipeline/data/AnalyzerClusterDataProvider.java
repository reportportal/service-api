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

package com.epam.ta.reportportal.core.launch.cluster.pipeline.data;

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;

import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.ClusterData;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.GenerateClustersRq;
import com.epam.ta.reportportal.core.launch.cluster.config.ClusterEntityContext;
import com.epam.ta.reportportal.core.launch.cluster.config.GenerateClustersConfig;
import com.epam.reportportal.model.analyzer.IndexLaunch;
import com.epam.reportportal.model.project.AnalyzerConfig;
import com.epam.ta.reportportal.ws.reporting.ErrorType;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AnalyzerClusterDataProvider implements ClusterDataProvider {

  private final AnalyzerServiceClient analyzerServiceClient;

  public AnalyzerClusterDataProvider(AnalyzerServiceClient analyzerServiceClient) {
    this.analyzerServiceClient = analyzerServiceClient;
  }

  protected abstract Optional<IndexLaunch> prepareIndexLaunch(GenerateClustersConfig config);

  @Override
  public Optional<ClusterData> provide(GenerateClustersConfig config) {
    expect(analyzerServiceClient.hasClients(), Predicate.isEqual(true)).verify(
        ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
        "There are no analyzer services are deployed."
    );
    return getGenerateRq(config).map(analyzerServiceClient::generateClusters);
  }

  private Optional<GenerateClustersRq> getGenerateRq(GenerateClustersConfig config) {
    return prepareIndexLaunch(config).map(indexLaunch -> {
      final GenerateClustersRq generateClustersRq = new GenerateClustersRq();
      generateClustersRq.setLaunch(indexLaunch);
      generateClustersRq.setCleanNumbers(config.isCleanNumbers());
      generateClustersRq.setForUpdate(config.isForUpdate());

      final ClusterEntityContext entityContext = config.getEntityContext();
      generateClustersRq.setProject(entityContext.getProjectId());

      final AnalyzerConfig analyzerConfig = config.getAnalyzerConfig();
      generateClustersRq.setNumberOfLogLines(analyzerConfig.getNumberOfLogLines());

      return generateClustersRq;
    });
  }

}
