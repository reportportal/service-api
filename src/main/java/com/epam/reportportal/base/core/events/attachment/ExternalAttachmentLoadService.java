/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.core.events.attachment;

import com.epam.reportportal.api.model.PluginCommandContext;
import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.core.plugin.PluginBox;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.Log;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.command.ExtensionCommand;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Resolves the target integration and delegates the attachment download/save flow to the plugin command requested in
 * the event.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalAttachmentLoadService {

  private final LogRepository logRepository;
  private final IntegrationRepository integrationRepository;
  private final IntegrationTypeRepository integrationTypeRepository;
  private final PluginBox pluginBox;

  public void loadAttachment(ExternalAttachmentLoadEvent event) {
    try {
      processEvent(event);
    } catch (Exception e) {
      log.warn("Dropping external attachment load event for logId {} and attachmentExternalId {}",
          event == null ? null : event.getLogId(),
          event == null ? null : event.getAttachmentExternalId(), e);
    }
  }

  private void processEvent(ExternalAttachmentLoadEvent event) {
    if (event == null || event.getLogId() == null || !StringUtils.hasText(
        event.getAttachmentExternalId()) || !StringUtils.hasText(event.getPluginId())
        || !StringUtils.hasText(event.getPluginCommandName())) {
      log.warn("Skipping external attachment load: event payload is incomplete");
      return;
    }

    Long projectId = resolveProjectId(event);
    if (projectId == null) {
      log.warn("Skipping external attachment load for log {}: projectId could not be resolved", event.getLogId());
      return;
    }

    Integration integration = resolveIntegration(projectId, event.getPluginId()).orElse(null);
    if (integration == null) {
      log.warn("Skipping external attachment load for log {}: integration for plugin '{}' not found",
          event.getLogId(), event.getPluginId());
      return;
    }

    ReportPortalExtensionPoint pluginInstance = pluginBox.getInstance(event.getPluginId(),
            ReportPortalExtensionPoint.class)
        .orElse(null);
    if (pluginInstance == null) {
      log.warn("Skipping external attachment load for log {}: plugin '{}' is unavailable",
          event.getLogId(), event.getPluginId());
      return;
    }
    ExtensionCommand<?> command = pluginInstance.getIntegrationExtensionCommand(event.getPluginCommandName());
    if (command == null) {
      log.warn(
          "Skipping external attachment load for log {}: command '{}' is unavailable for plugin '{}'",
          event.getLogId(), event.getPluginCommandName(), event.getPluginId());
      return;
    }

    command.executeCommand(integration, buildCommandParams(event, projectId));
  }

  private Long resolveProjectId(ExternalAttachmentLoadEvent event) {
    if (event.getProjectId() != null) {
      return event.getProjectId();
    }
    Log logEntity = logRepository.findById(event.getLogId()).orElse(null);
    return logEntity == null ? null : logEntity.getProjectId();
  }

  private Optional<Integration> resolveIntegration(Long projectId, String pluginId) {
    return integrationTypeRepository.findByName(pluginId)
        .flatMap(type -> resolveIntegration(projectId, type));
  }

  private Optional<Integration> resolveIntegration(Long projectId,
      IntegrationType integrationType) {
    List<Integration> projectIntegrations =
        integrationRepository.findAllByProjectIdAndTypeOrderByCreationDateDesc(projectId,
            integrationType);
    if (!projectIntegrations.isEmpty()) {
      return Optional.of(projectIntegrations.getFirst());
    }
    return integrationRepository.findAllGlobalByType(integrationType).stream().findFirst();
  }

  private PluginCommandRQ buildCommandParams(ExternalAttachmentLoadEvent event, Long projectId) {
    HashMap<String, Object> params = new HashMap<>();
    params.put("logId", event.getLogId());
    params.put("projectId", projectId);
    params.put("launchId", event.getLaunchId());
    params.put("testItemId", event.getTestItemId());
    params.put("attachmentExternalId", event.getAttachmentExternalId());
    if (StringUtils.hasText(event.getAttachmentAttributeKey())) {
      params.put("attachmentAttributeKey", event.getAttachmentAttributeKey());
    }
    return new PluginCommandRQ()
        .context(new PluginCommandContext(null, projectId, null))
        .arguments(params);
  }
}
