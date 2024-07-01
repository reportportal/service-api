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
import com.epam.ta.reportportal.reporting.async.handler.ReportingMessageHandler;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class ReportingHandlerProvider {

  private final Map<RequestType, ReportingMessageHandler> reportingHandlerMap;

  public ReportingHandlerProvider(Map<RequestType, ReportingMessageHandler> reportingHandlerMap) {
    this.reportingHandlerMap = reportingHandlerMap;
  }

  public Optional<ReportingMessageHandler> provideHandler(RequestType requestType) {
    return Optional.ofNullable(reportingHandlerMap.get(requestType));
  }
}
