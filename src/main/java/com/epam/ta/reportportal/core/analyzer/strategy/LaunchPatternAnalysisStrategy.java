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

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.LAUNCH_NOT_FOUND;
import static java.util.stream.Collectors.toSet;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.analyzer.pattern.LaunchPatternAnalyzer;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.launch.AnalyzeLaunchRQ;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LaunchPatternAnalysisStrategy extends AbstractLaunchAnalysisStrategy {

  private final LaunchPatternAnalyzer launchPatternAnalyzer;

  @Autowired
  public LaunchPatternAnalysisStrategy(ProjectRepository projectRepository,
      LaunchRepository launchRepository,
      LaunchPatternAnalyzer launchPatternAnalyzer) {
    super(projectRepository, launchRepository);
    this.launchPatternAnalyzer = launchPatternAnalyzer;
  }

  public void analyze(AnalyzeLaunchRQ analyzeRQ, ReportPortalUser.ProjectDetails projectDetails,
      ReportPortalUser user) {

    Set<AnalyzeItemsMode> analyzeItemsModes = analyzeRQ.getAnalyzeItemsModes()
        .stream()
        .map(AnalyzeItemsMode::fromString)
        .collect(toSet());

    expect(analyzeItemsModes, CollectionUtils::isNotEmpty).verify(ErrorType.PATTERN_ANALYSIS_ERROR,
        "No analyze item mode specified.");

    Launch launch = launchRepository.findById(analyzeRQ.getLaunchId())
        .orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, analyzeRQ.getLaunchId()));
    validateLaunch(launch, projectDetails);

    launchPatternAnalyzer.analyzeLaunch(launch, analyzeItemsModes);

  }
}
