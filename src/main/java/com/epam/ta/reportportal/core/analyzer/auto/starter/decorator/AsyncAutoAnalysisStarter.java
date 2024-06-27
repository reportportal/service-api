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

package com.epam.ta.reportportal.core.analyzer.auto.starter.decorator;

import com.epam.ta.reportportal.core.analyzer.auto.starter.LaunchAutoAnalysisStarter;
import com.epam.ta.reportportal.core.analyzer.config.StartLaunchAutoAnalysisConfig;
import org.springframework.core.task.TaskExecutor;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class AsyncAutoAnalysisStarter implements LaunchAutoAnalysisStarter {

  private final TaskExecutor executor;
  private final LaunchAutoAnalysisStarter launchAutoAnalysisStarter;

  public AsyncAutoAnalysisStarter(TaskExecutor executor,
      LaunchAutoAnalysisStarter launchAutoAnalysisStarter) {
    this.executor = executor;
    this.launchAutoAnalysisStarter = launchAutoAnalysisStarter;
  }

  @Override
  public void start(StartLaunchAutoAnalysisConfig config, boolean isManual) {
    executor.execute(() -> launchAutoAnalysisStarter.start(config, isManual));
  }
}
