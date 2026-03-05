/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.analyzer.auto.impl;

import com.epam.ta.reportportal.core.analyzer.auto.AnalyzerService;
import com.epam.ta.reportportal.core.analyzer.auto.AnalyzerServiceAsync;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.reportportal.model.project.AnalyzerConfig;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

/**
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
