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

package com.epam.ta.reportportal.core.events.handler.launch;

import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.analyzer.pattern.LaunchPatternAnalyzer;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.events.handler.ConfigurableEventHandler;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.google.common.collect.Sets;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LaunchPatternAnalysisRunner implements
    ConfigurableEventHandler<LaunchFinishedEvent, Map<String, String>> {

  private final GetLaunchHandler getLaunchHandler;
  private final LaunchPatternAnalyzer launchPatternAnalyzer;

  @Autowired
  public LaunchPatternAnalysisRunner(GetLaunchHandler getLaunchHandler,
      LaunchPatternAnalyzer launchPatternAnalyzer) {
    this.getLaunchHandler = getLaunchHandler;
    this.launchPatternAnalyzer = launchPatternAnalyzer;
  }

  @Override
  @Transactional
  public void handle(LaunchFinishedEvent launchFinishedEvent, Map<String, String> projectConfig) {

    boolean isPatternAnalysisEnabled = BooleanUtils.toBoolean(
        projectConfig.get(ProjectAttributeEnum.AUTO_PATTERN_ANALYZER_ENABLED.getAttribute()));

    if (isPatternAnalysisEnabled) {
      final Launch launch = getLaunchHandler.get(launchFinishedEvent.getId());
      launchPatternAnalyzer.analyzeLaunch(launch,
          Sets.newHashSet(AnalyzeItemsMode.TO_INVESTIGATE, AnalyzeItemsMode.IGNORE_IMMEDIATE));
    }
  }

}
