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

package com.epam.reportportal.base.core.analyzer.auto.impl;

import com.epam.reportportal.base.core.analyzer.auto.AnalyzerService;
import com.epam.reportportal.base.core.analyzer.auto.AnalyzerServiceAsync;
import com.epam.reportportal.base.infrastructure.model.project.AnalyzerConfig;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

/**
 * Async implementation that delegates to the synchronous analyzer service.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class AnalyzerServiceAsyncImpl implements AnalyzerServiceAsync {

  private final AnalyzerService analyzerService;
  private final TaskExecutor autoAnalyzeTaskExecutor;

  @Autowired
  public AnalyzerServiceAsyncImpl(AnalyzerService analyzerService,
      @Qualifier("autoAnalyzeTaskExecutor") TaskExecutor autoAnalyzeTaskExecutor) {
    this.analyzerService = analyzerService;
    this.autoAnalyzeTaskExecutor = autoAnalyzeTaskExecutor;
  }

  @Override
  public CompletableFuture<Void> analyze(Launch launch, List<Long> itemIds,
      AnalyzerConfig analyzerConfig) {
    return CompletableFuture.runAsync(
        () -> analyzerService.runAnalyzers(launch, itemIds, analyzerConfig),
        autoAnalyzeTaskExecutor);
  }

  @Override
  public boolean hasAnalyzers() {
    return analyzerService.hasAnalyzers();
  }

}
