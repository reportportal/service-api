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

package com.epam.ta.reportportal.core.configs.event.publisher;

import static org.springframework.context.support.AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME;

import com.epam.reportportal.extension.event.LaunchAutoAnalysisFinishEvent;
import com.epam.reportportal.extension.event.LaunchStartUniqueErrorAnalysisEvent;
import com.epam.reportportal.extension.event.LaunchUniqueErrorAnalysisFinishEvent;
import com.epam.ta.reportportal.core.events.multicaster.DelegatingApplicationEventMulticaster;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.util.ErrorHandler;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Configuration
public class EventPublisherConfig {

  private final ErrorHandler loggingEventErrorHandler;

  @Autowired
  public EventPublisherConfig(ErrorHandler loggingEventErrorHandler) {
    this.loggingEventErrorHandler = loggingEventErrorHandler;
  }

  @Bean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)
  public ApplicationEventMulticaster applicationEventMulticaster() {
    final DelegatingApplicationEventMulticaster eventMulticaster = new DelegatingApplicationEventMulticaster(
        Set.of(
            LaunchAutoAnalysisFinishEvent.class,
            LaunchUniqueErrorAnalysisFinishEvent.class,
            LaunchStartUniqueErrorAnalysisEvent.class
        ));
    eventMulticaster.setErrorHandler(loggingEventErrorHandler);
    return eventMulticaster;
  }

}
