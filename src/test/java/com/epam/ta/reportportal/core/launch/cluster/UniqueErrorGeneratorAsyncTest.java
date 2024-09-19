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

package com.epam.ta.reportportal.core.launch.cluster;

import static com.epam.ta.reportportal.core.launch.cluster.utils.ConfigProvider.getConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.core.launch.cluster.config.ClusterEntityContext;
import com.epam.ta.reportportal.core.launch.cluster.config.GenerateClustersConfig;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.pipeline.PipelineConstructor;
import com.epam.ta.reportportal.pipeline.TransactionalPipeline;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SyncTaskExecutor;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class UniqueErrorGeneratorAsyncTest {

  private final SyncTaskExecutor logClusterExecutor = mock(SyncTaskExecutor.class);

  private final AnalyzerStatusCache analyzerStatusCache = mock(AnalyzerStatusCache.class);

  private final PipelineConstructor<GenerateClustersConfig> pipelineConstructor = (PipelineConstructor<GenerateClustersConfig>) mock(
      PipelineConstructor.class);

  private final TransactionalPipeline transactionalPipeline = mock(TransactionalPipeline.class);

  private final UniqueErrorGeneratorAsync clusterGenerator = new UniqueErrorGeneratorAsync(
      analyzerStatusCache,
      pipelineConstructor,
      transactionalPipeline,
      logClusterExecutor
  );

  @Test
  void shouldFailWhenCacheContainsLaunchId() {
    when(analyzerStatusCache.containsLaunchId(anyString(), anyLong())).thenReturn(true);

    final GenerateClustersConfig config = getConfig(false);

    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> clusterGenerator.generate(config));
    assertEquals("Impossible interact with integration. Clusters creation is in progress.",
        exception.getMessage());

    final ClusterEntityContext entityContext = config.getEntityContext();
    verify(analyzerStatusCache, times(0)).analyzeStarted(AnalyzerStatusCache.CLUSTER_KEY,
        entityContext.getLaunchId(),
        entityContext.getProjectId()
    );
  }

  @Test
  void shouldGenerate() {
    when(analyzerStatusCache.containsLaunchId(anyString(), anyLong())).thenReturn(false);
    doCallRealMethod().when(logClusterExecutor).execute(any(Runnable.class));

    final GenerateClustersConfig config = getConfig(false);

    clusterGenerator.generate(config);

    final ClusterEntityContext entityContext = config.getEntityContext();
    verify(analyzerStatusCache, times(1)).analyzeStarted(AnalyzerStatusCache.CLUSTER_KEY,
        entityContext.getLaunchId(),
        entityContext.getProjectId()
    );
    verify(pipelineConstructor, times(1)).construct(config);
    verify(transactionalPipeline, times(1)).run(anyList());
    verify(analyzerStatusCache, times(1)).analyzeFinished(AnalyzerStatusCache.CLUSTER_KEY,
        entityContext.getLaunchId());
  }

  @Test
  void shouldCleanCacheWhenExceptionThrown() {
    when(analyzerStatusCache.containsLaunchId(anyString(), anyLong())).thenReturn(false);
    doCallRealMethod().when(logClusterExecutor).execute(any(Runnable.class));

    final GenerateClustersConfig config = getConfig(false);

    doThrow(new RuntimeException("Exception during generation")).when(transactionalPipeline)
        .run(anyList());

    clusterGenerator.generate(config);

    final ClusterEntityContext entityContext = config.getEntityContext();
    verify(analyzerStatusCache, times(1)).analyzeStarted(AnalyzerStatusCache.CLUSTER_KEY,
        entityContext.getLaunchId(),
        entityContext.getProjectId()
    );
    verify(pipelineConstructor, times(1)).construct(config);
    verify(transactionalPipeline, times(1)).run(anyList());
    verify(analyzerStatusCache, times(1)).analyzeFinished(AnalyzerStatusCache.CLUSTER_KEY,
        entityContext.getLaunchId());
  }

  @Test
  void shouldCleanCacheWhenExceptionThrownDuringTaskSubmit() {
    when(analyzerStatusCache.containsLaunchId(anyString(), anyLong())).thenReturn(false);

    final GenerateClustersConfig config = getConfig(false);

    doThrow(new RuntimeException("Exception during generation")).when(logClusterExecutor)
        .execute(any(Runnable.class));

    clusterGenerator.generate(config);

    final ClusterEntityContext entityContext = config.getEntityContext();
    verify(analyzerStatusCache, times(1)).analyzeStarted(AnalyzerStatusCache.CLUSTER_KEY,
        entityContext.getLaunchId(),
        entityContext.getProjectId()
    );
    verify(pipelineConstructor, times(0)).construct(any(GenerateClustersConfig.class));
    verify(transactionalPipeline, times(0)).run(anyList());
    verify(analyzerStatusCache, times(1)).analyzeFinished(AnalyzerStatusCache.CLUSTER_KEY,
        entityContext.getLaunchId());
  }

}