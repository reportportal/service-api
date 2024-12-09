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

package com.epam.ta.reportportal.core.admin;

import static com.epam.ta.reportportal.entity.ServerSettingsConstants.ANALYTICS_CONFIG_PREFIX;
import static com.epam.ta.reportportal.ws.converter.converters.ServerSettingsConverter.TO_RESOURCE;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.SettingsUpdatedEvent;
import com.epam.ta.reportportal.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.entity.ServerSettings;
import com.epam.ta.reportportal.model.settings.AnalyticsResource;
import com.epam.ta.reportportal.model.settings.ServerSettingsResource;
import com.epam.ta.reportportal.model.settings.UpdateSettingsRq;
import com.epam.ta.reportportal.ws.converter.converters.ServerSettingsConverter;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Basic implementation of server administration interface {@link ServerAdminHandler}
 *
 * @author Andrei_Ramanchuk
 */
@Service
@RequiredArgsConstructor
public class ServerAdminHandlerImpl implements ServerAdminHandler {

  private final ServerSettingsRepository serverSettingsRepository;

  private final MessageBus messageBus;

  @Override
  public Map<String, String> getServerSettings() {
    return ServerSettingsConverter.TO_RESOURCES.apply(
        serverSettingsRepository.selectServerSettings());
  }

  @Override
  public OperationCompletionRS saveAnalyticsSettings(AnalyticsResource analyticsResource) {
    String analyticsType = analyticsResource.getType();
    Map<String, ServerSettings> serverAnalyticsDetails = findServerSettings().entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(ANALYTICS_CONFIG_PREFIX))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    String formattedAnalyticsType =
        analyticsType.startsWith(ANALYTICS_CONFIG_PREFIX) ? analyticsType :
            ANALYTICS_CONFIG_PREFIX + analyticsType;

    ServerSettings analyticsDetails =
        ofNullable(serverAnalyticsDetails.get(formattedAnalyticsType)).orElseGet(
            ServerSettings::new);
    analyticsDetails.setKey(formattedAnalyticsType);
    analyticsDetails.setValue(
        String.valueOf((ofNullable(analyticsResource.getEnabled()).orElse(false))));

    serverSettingsRepository.save(analyticsDetails);
    return new OperationCompletionRS("Server Settings were successfully updated.");
  }

  @Override
  public OperationCompletionRS updateServerSettings(UpdateSettingsRq request,
      ReportPortalUser user) {
    ServerSettings serverSettings = serverSettingsRepository.findByKey(request.getKey())
        .orElseThrow(() -> new ReportPortalException(ErrorType.SERVER_SETTINGS_NOT_FOUND,
            request.getKey()));
    ServerSettingsResource before = TO_RESOURCE.apply(serverSettings);

    serverSettings.setValue(request.getValue());
    serverSettingsRepository.save(serverSettings);

    messageBus.publishActivity(new SettingsUpdatedEvent(before, TO_RESOURCE.apply(serverSettings),
        user.getUserId(), user.getUsername()));
    return new OperationCompletionRS("Server Settings were successfully updated.");
  }

  private Map<String, ServerSettings> findServerSettings() {
    return serverSettingsRepository.selectServerSettings().stream()
        .collect(toMap(ServerSettings::getKey, s -> s, (prev, curr) -> prev));
  }
}