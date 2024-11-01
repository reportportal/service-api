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

package com.epam.ta.reportportal.core.configs.event.subscriber;

import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.events.activity.item.IssueResolvedEvent;
import com.epam.ta.reportportal.core.events.activity.item.TestItemFinishedEvent;
import com.epam.ta.reportportal.core.events.handler.item.TestItemAutoAnalysisRunner;
import com.epam.ta.reportportal.core.events.handler.item.TestItemPatternAnalysisRunner;
import com.epam.ta.reportportal.core.events.handler.item.TestItemIndexRunner;
import com.epam.ta.reportportal.core.events.handler.item.TestItemUniqueErrorAnalysisRunner;
import com.epam.ta.reportportal.core.events.handler.launch.LaunchAnalysisFinishEventPublisher;
import com.epam.ta.reportportal.core.events.handler.launch.LaunchAutoAnalysisRunner;
import com.epam.ta.reportportal.core.events.handler.launch.LaunchNotificationRunner;
import com.epam.ta.reportportal.core.events.handler.launch.LaunchPatternAnalysisRunner;
import com.epam.ta.reportportal.core.events.handler.launch.LaunchUniqueErrorAnalysisRunner;
import com.epam.ta.reportportal.core.events.subscriber.impl.delegate.ProjectConfigDelegatingSubscriber;
import com.epam.ta.reportportal.core.project.config.ProjectConfigProvider;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Configuration
public class EventSubscriberConfig {

  @Bean
  public ProjectConfigDelegatingSubscriber<LaunchFinishedEvent> launchFinishedDelegatingSubscriber(
      ProjectConfigProvider projectConfigProvider,
      LaunchAutoAnalysisRunner autoAnalysisEventHandler,
      LaunchUniqueErrorAnalysisRunner uniqueErrorAnalysisEventHandler,
      LaunchAnalysisFinishEventPublisher launchAnalysisFinishEventPublisher,
      LaunchPatternAnalysisRunner patternAnalysisEventHandler,
      LaunchNotificationRunner notificationEventHandler) {
    return new ProjectConfigDelegatingSubscriber<>(projectConfigProvider,
        List.of(patternAnalysisEventHandler,
            autoAnalysisEventHandler,
            uniqueErrorAnalysisEventHandler,
            launchAnalysisFinishEventPublisher,
            notificationEventHandler
        )
    );
  }

  @Bean
  public ProjectConfigDelegatingSubscriber<IssueResolvedEvent> itemIssueResolvedDelegatingSubscriber(
      ProjectConfigProvider projectConfigProvider, TestItemIndexRunner testItemIndexRunner,
      TestItemUniqueErrorAnalysisRunner testItemUniqueErrorAnalysisRunner) {
    return new ProjectConfigDelegatingSubscriber<>(projectConfigProvider,
        List.of(testItemIndexRunner, testItemUniqueErrorAnalysisRunner)
    );
  }

  @Bean
  public ProjectConfigDelegatingSubscriber<TestItemFinishedEvent> testItemFinishedDelegatingSubscriber(
      ProjectConfigProvider projectConfigProvider,
      TestItemPatternAnalysisRunner testItemPatternAnalysisRunner,
      TestItemAutoAnalysisRunner testItemAutoAnalysisRunner) {
    return new ProjectConfigDelegatingSubscriber<>(projectConfigProvider,
        List.of(testItemPatternAnalysisRunner, testItemAutoAnalysisRunner)
    );
  }

}
