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

package com.epam.reportportal.base.core.launch.cluster;

import com.epam.reportportal.base.core.analyzer.auto.impl.AnalyzerStatusCache;
import com.epam.reportportal.base.core.launch.cluster.config.GenerateClustersConfig;
import com.epam.reportportal.base.pipeline.PipelineConstructor;
import com.epam.reportportal.base.pipeline.TransactionalPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

/**
 * Asynchronous unique-error cluster generation.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class UniqueErrorGeneratorAsync extends UniqueErrorGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(UniqueErrorGeneratorAsync.class);

  private final TaskExecutor logClusterExecutor;

  @Autowired
  public UniqueErrorGeneratorAsync(AnalyzerStatusCache analyzerStatusCache,
      PipelineConstructor<GenerateClustersConfig> generateClustersPipelineConstructor,
      TransactionalPipeline transactionalPipeline,
      @Qualifier(value = "logClusterExecutor") TaskExecutor logClusterExecutor) {
    super(analyzerStatusCache, generateClustersPipelineConstructor, transactionalPipeline);
    this.logClusterExecutor = logClusterExecutor;
  }

  @Override
  protected void generateClusters(GenerateClustersConfig config) {
    try {
      logClusterExecutor.execute(() -> super.generateClusters(config));
    } catch (Exception ex) {
      LOGGER.error(ex.getMessage(), ex);
      cleanCache(config.getEntityContext());
    }
  }
}
