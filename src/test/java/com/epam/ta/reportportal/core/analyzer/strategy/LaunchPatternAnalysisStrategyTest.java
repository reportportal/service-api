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

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.analyzer.pattern.service.LaunchPatternAnalyzer;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.attribute.Attribute;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.model.launch.AnalyzeLaunchRQ;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class LaunchPatternAnalysisStrategyTest {

  private final Launch launch = mock(Launch.class);
  private final Project project = mock(Project.class);

  private final ProjectRepository projectRepository = mock(ProjectRepository.class);
  private final LaunchRepository launchRepository = mock(LaunchRepository.class);
  private final LaunchPatternAnalyzer launchPatternAnalyzer = mock(LaunchPatternAnalyzer.class);

  private final LaunchPatternAnalysisStrategy launchPatternAnalysisStrategy =
      new LaunchPatternAnalysisStrategy(projectRepository, launchRepository, launchPatternAnalyzer);

  @Test
  void analyzeTest() {

    when(launchRepository.findById(1L)).thenReturn(Optional.of(launch));
    when(launch.getProjectId()).thenReturn(1L);
    when(launch.getMode()).thenReturn(LaunchModeEnum.DEFAULT);
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

    ProjectAttribute projectAttribute = new ProjectAttribute();
    projectAttribute.setValue("true");
    Attribute attribute = new Attribute();
    projectAttribute.setAttribute(attribute);

    when(project.getProjectAttributes()).thenReturn(Sets.newHashSet(projectAttribute));

    ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L);
    ReportPortalUser.ProjectDetails projectDetails =
        new ReportPortalUser.ProjectDetails(1L, "name", ProjectRole.PROJECT_MANAGER);
    AnalyzeLaunchRQ analyzeLaunchRQ = new AnalyzeLaunchRQ();
    analyzeLaunchRQ.setLaunchId(1L);
    analyzeLaunchRQ.setAnalyzeItemsModes(Lists.newArrayList("TO_INVESTIGATE"));
    analyzeLaunchRQ.setAnalyzerTypeName("patternAnalyzer");
    launchPatternAnalysisStrategy.analyze(analyzeLaunchRQ, projectDetails, user);

    verify(launchPatternAnalyzer, times(1)).analyzeLaunch(launch,
        Sets.newHashSet(AnalyzeItemsMode.TO_INVESTIGATE)
    );

  }
}