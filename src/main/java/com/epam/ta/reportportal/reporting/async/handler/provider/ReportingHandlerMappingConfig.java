/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.reporting.async.handler.provider;

import com.epam.ta.reportportal.reporting.async.config.RequestType;
import com.epam.ta.reportportal.reporting.async.handler.LaunchFinishMessageHandler;
import com.epam.ta.reportportal.reporting.async.handler.LaunchStartMessageHandler;
import com.epam.ta.reportportal.reporting.async.handler.LogMessageHandler;
import com.epam.ta.reportportal.reporting.async.handler.ReportingMessageHandler;
import com.epam.ta.reportportal.reporting.async.handler.TestItemFinishMessageHandler;
import com.epam.ta.reportportal.reporting.async.handler.TestItemStartMessageHandler;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Configuration
public class ReportingHandlerMappingConfig implements ApplicationContextAware {

  private ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Bean
  public Map<RequestType, ReportingMessageHandler> reportingHandlerMap() {
    return ImmutableMap.<RequestType, ReportingMessageHandler>builder()
        .put(RequestType.START_LAUNCH, applicationContext.getBean(
            LaunchStartMessageHandler.class))
        .put(RequestType.FINISH_LAUNCH,
            applicationContext.getBean(LaunchFinishMessageHandler.class))
        .put(RequestType.START_TEST,
            applicationContext.getBean(TestItemStartMessageHandler.class))
        .put(RequestType.FINISH_TEST,
            applicationContext.getBean(TestItemFinishMessageHandler.class))
        .put(RequestType.LOG, applicationContext.getBean(LogMessageHandler.class))
        .build();

  }

}
