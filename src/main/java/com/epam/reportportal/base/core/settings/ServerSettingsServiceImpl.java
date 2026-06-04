/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.settings;

import static com.epam.reportportal.base.infrastructure.persistence.entity.ServerSettingsConstants.ANALYTICS_CONFIG_PREFIX;
import static com.epam.reportportal.base.ws.converter.converters.ServerSettingsConverter.TO_RESOURCE;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import com.epam.reportportal.base.core.events.domain.SettingsUpdatedEvent;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.ServerSettingsRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.ServerSettings;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.settings.AnalyticsResource;
import com.epam.reportportal.base.model.settings.ServerSettingsResource;
import com.epam.reportportal.base.model.settings.UpdateSettingsRq;
import com.epam.reportportal.base.reporting.OperationCompletionRS;
import com.epam.reportportal.base.ws.converter.converters.ServerSettingsConverter;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Basic implementation of server administration interface {@link ServerSettingsService}
 *
 * @author Andrei_Ramanchuk
 */
@Service
@RequiredArgsConstructor
public class ServerSettingsServiceImpl implements ServerSettingsService {

  private final ServerSettingsRegistry settingsRegistry;

  private final ServerSettingsRepository serverSettingsRepository;

  private final ApplicationEventPublisher eventPublisher;

  @Override
  public Map<String, String> getServerSettings() {
    return ServerSettingsConverter.TO_RESOURCES.apply(
        serverSettingsRepository.selectServerSettings());
  }

  @Override
  public boolean checkServerSettingsState(String key, String value) {
    return serverSettingsRepository.findByKey(key).map(it -> Objects.equals(value, it.getValue()))
        .orElse(Boolean.FALSE);
  }


  @Override
  public OperationCompletionRS saveAnalyticsSettings(AnalyticsResource analyticsResource,
      ReportPortalUser user) {
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
    ServerSettingsResource before = TO_RESOURCE.apply(analyticsDetails);

    analyticsDetails.setKey(formattedAnalyticsType);
    analyticsDetails.setValue(
        String.valueOf((ofNullable(analyticsResource.getEnabled()).orElse(false))));

    serverSettingsRepository.save(analyticsDetails);

    eventPublisher.publishEvent(new SettingsUpdatedEvent(
        before,
        TO_RESOURCE.apply(analyticsDetails),
        user.getUserId(),
        user.getUsername()
    ));
    return new OperationCompletionRS("Server Settings were successfully updated.");
  }

  @Override
  public OperationCompletionRS updateServerSettings(UpdateSettingsRq request,
      ReportPortalUser user) {
    ServerSettings serverSettings = serverSettingsRepository.findByKey(request.getKey().getName())
        .orElseThrow(() -> new ReportPortalException(ErrorType.SERVER_SETTINGS_NOT_FOUND,
            request.getKey().getName()));
    ServerSettingsResource before = TO_RESOURCE.apply(serverSettings);
    var settingHandler = settingsRegistry.getHandler(serverSettings.getKey());
    settingHandler.ifPresent(handler -> handler.validate(request.getValue()));

    serverSettings.setValue(request.getValue());
    serverSettingsRepository.save(serverSettings);

    settingHandler.ifPresent(handler -> handler.handle(serverSettings.getValue()));
    eventPublisher.publishEvent(new SettingsUpdatedEvent(
        before,
        TO_RESOURCE.apply(serverSettings),
        user.getUserId(),
        user.getUsername()
    ));
    return new OperationCompletionRS("Server Settings were successfully updated.");
  }

  private Map<String, ServerSettings> findServerSettings() {
    return serverSettingsRepository.selectServerSettings().stream()
        .collect(toMap(ServerSettings::getKey, s -> s, (prev, curr) -> prev));
  }
}
