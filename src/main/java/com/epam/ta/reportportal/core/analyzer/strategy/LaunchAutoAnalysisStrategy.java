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

package com.epam.ta.reportportal.core.analyzer.strategy;

import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils.getAnalyzerConfig;
import static com.epam.reportportal.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.rules.exception.ErrorType.LAUNCH_NOT_FOUND;
import static com.epam.reportportal.rules.exception.ErrorType.PROJECT_NOT_FOUND;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.starter.LaunchAutoAnalysisStarter;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.analyzer.config.StartLaunchAutoAnalysisConfig;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.AnalyzeMode;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.model.launch.AnalyzeLaunchRQ;
import com.epam.reportportal.model.project.AnalyzerConfig;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LaunchAutoAnalysisStrategy extends AbstractLaunchAnalysisStrategy {

  private final LaunchAutoAnalysisStarter manualAnalysisStarter;

  @Autowired
  public LaunchAutoAnalysisStrategy(ProjectRepository projectRepository,
      LaunchRepository launchRepository, LaunchAutoAnalysisStarter manualAnalysisStarter) {
    super(projectRepository, launchRepository);
    this.manualAnalysisStarter = manualAnalysisStarter;
  }

  public void analyze(AnalyzeLaunchRQ analyzeRQ, ReportPortalUser.ProjectDetails projectDetails,
      ReportPortalUser user) {

    final AnalyzeMode analyzeMode = AnalyzeMode.fromString(analyzeRQ.getAnalyzerHistoryMode())
        .orElseThrow(
            () -> new ReportPortalException(BAD_REQUEST_ERROR, analyzeRQ.getAnalyzerHistoryMode()));
    final Set<AnalyzeItemsMode> analyzeItemsModes = getAnalyzeItemsModes(analyzeRQ);

    if (analyzeItemsModes.isEmpty()) {
      return;
    }

    Launch launch = launchRepository.findById(analyzeRQ.getLaunchId())
        .orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, analyzeRQ.getLaunchId()));
    validateLaunch(launch, projectDetails);

    Project project = projectRepository.findById(projectDetails.getProjectId()).orElseThrow(
        () -> new ReportPortalException(PROJECT_NOT_FOUND, projectDetails.getProjectId()));

    AnalyzerConfig analyzerConfig = getAnalyzerConfig(project);
    analyzerConfig.setAnalyzerMode(analyzeMode.getValue());

    final StartLaunchAutoAnalysisConfig autoAnalysisConfig =
        StartLaunchAutoAnalysisConfig.of(launch.getId(), analyzerConfig, analyzeItemsModes, user);

    manualAnalysisStarter.start(autoAnalysisConfig);
  }

  private LinkedHashSet<AnalyzeItemsMode> getAnalyzeItemsModes(AnalyzeLaunchRQ analyzeRQ) {
    return analyzeRQ.getAnalyzeItemsModes().stream().map(AnalyzeItemsMode::fromString)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

}
