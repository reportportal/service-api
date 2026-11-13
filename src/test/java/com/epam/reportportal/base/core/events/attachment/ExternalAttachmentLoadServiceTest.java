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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExternalAttachmentLoadServiceTest {

  private static final String PLUGIN_ID = "test-plugin";
  private static final String COMMAND_NAME = "downloadExternalAttachment";

  @Mock
  private LogRepository logRepository;

  @Mock
  private IntegrationRepository integrationRepository;

  @Mock
  private IntegrationTypeRepository integrationTypeRepository;

  @Mock
  private PluginBox pluginBox;

  @Mock
  private ReportPortalExtensionPoint pluginInstance;

  @Mock
  private ExtensionCommand<Object> pluginCommand;

  @InjectMocks
  private ExternalAttachmentLoadService service;

  @Test
  void prefersProjectIntegrationWhenAvailable() {
    IntegrationType type = integrationType();
    Integration projectIntegration = new Integration();
    when(integrationTypeRepository.findByName(PLUGIN_ID)).thenReturn(Optional.of(type));
    when(integrationRepository.findAllByProjectIdAndTypeOrderByCreationDateDesc(7L, type))
        .thenReturn(List.of(projectIntegration));
    when(pluginBox.getInstance(PLUGIN_ID, ReportPortalExtensionPoint.class))
        .thenReturn(Optional.of(pluginInstance));
    doReturn(pluginCommand).when(pluginInstance).getIntegrationExtensionCommand(COMMAND_NAME);

    service.loadAttachment(
        event(11L, 7L, 3L, 5L, "rec-1", "mobitru_selenium_recording_id"));

    ArgumentCaptor<PluginCommandRQ> paramsCaptor = ArgumentCaptor.forClass(PluginCommandRQ.class);
    verify(pluginCommand).executeCommand(eq(projectIntegration), paramsCaptor.capture());
    assertThat(paramsCaptor.getValue().getArguments()).containsEntry("logId", 11L)
        .containsEntry("projectId", 7L)
        .containsEntry("launchId", 3L)
        .containsEntry("testItemId", 5L)
        .containsEntry("attachmentExternalId", "rec-1")
        .containsEntry("attachmentAttributeKey", "mobitru_selenium_recording_id");
    assertThat(paramsCaptor.getValue().getContext().getProjectId()).isEqualTo(7L);
    verify(integrationRepository, never()).findAllGlobalByType(type);
  }

  @Test
  void fallsBackToGlobalIntegrationWhenProjectScopedMissing() {
    IntegrationType type = integrationType();
    Integration globalIntegration = new Integration();
    when(integrationTypeRepository.findByName(PLUGIN_ID)).thenReturn(Optional.of(type));
    when(integrationRepository.findAllByProjectIdAndTypeOrderByCreationDateDesc(7L, type))
        .thenReturn(List.of());
    when(integrationRepository.findAllGlobalByType(type)).thenReturn(List.of(globalIntegration));
    when(pluginBox.getInstance(PLUGIN_ID, ReportPortalExtensionPoint.class))
        .thenReturn(Optional.of(pluginInstance));
    doReturn(pluginCommand).when(pluginInstance).getIntegrationExtensionCommand(COMMAND_NAME);

    service.loadAttachment(event(11L, 7L, 3L, null, "rec-1"));

    verify(pluginCommand).executeCommand(eq(globalIntegration), any(PluginCommandRQ.class));
  }

  @Test
  void resolvesProjectIdFromLogWhenEventOmitsIt() {
    IntegrationType type = integrationType();
    Integration projectIntegration = new Integration();
    Log log = new Log();
    log.setProjectId(9L);
    when(logRepository.findById(11L)).thenReturn(Optional.of(log));
    when(integrationTypeRepository.findByName(PLUGIN_ID)).thenReturn(Optional.of(type));
    when(integrationRepository.findAllByProjectIdAndTypeOrderByCreationDateDesc(9L, type))
        .thenReturn(List.of(projectIntegration));
    when(pluginBox.getInstance(PLUGIN_ID, ReportPortalExtensionPoint.class))
        .thenReturn(Optional.of(pluginInstance));
    doReturn(pluginCommand).when(pluginInstance).getIntegrationExtensionCommand(COMMAND_NAME);

    service.loadAttachment(event(11L, null, 3L, null, "rec-1"));

    ArgumentCaptor<PluginCommandRQ> paramsCaptor = ArgumentCaptor.forClass(PluginCommandRQ.class);
    verify(pluginCommand).executeCommand(eq(projectIntegration), paramsCaptor.capture());
    assertThat(paramsCaptor.getValue().getArguments()).containsEntry("projectId", 9L);
  }

  @Test
  void skipsWhenIntegrationCannotBeResolved() {
    IntegrationType type = integrationType();
    when(integrationTypeRepository.findByName(PLUGIN_ID)).thenReturn(Optional.of(type));
    when(integrationRepository.findAllByProjectIdAndTypeOrderByCreationDateDesc(7L, type))
        .thenReturn(List.of());
    when(integrationRepository.findAllGlobalByType(type)).thenReturn(List.of());

    service.loadAttachment(event(11L, 7L, 3L, 5L, "rec-1"));

    verifyNoInteractions(pluginBox);
  }

  @Test
  void skipsWhenPluginMetadataIsMissing() {
    ExternalAttachmentLoadEvent event = event(11L, 7L, 3L, 5L, "rec-1");
    event.setPluginCommandName(null);

    service.loadAttachment(event);

    verifyNoInteractions(logRepository, integrationRepository, integrationTypeRepository,
        pluginBox);
  }

  @Test
  void swallowsPluginCommandExceptions() {
    IntegrationType type = integrationType();
    Integration projectIntegration = new Integration();
    when(integrationTypeRepository.findByName(PLUGIN_ID)).thenReturn(Optional.of(type));
    when(integrationRepository.findAllByProjectIdAndTypeOrderByCreationDateDesc(7L, type))
        .thenReturn(List.of(projectIntegration));
    when(pluginBox.getInstance(PLUGIN_ID, ReportPortalExtensionPoint.class))
        .thenReturn(Optional.of(pluginInstance));
    doReturn(pluginCommand).when(pluginInstance).getIntegrationExtensionCommand(COMMAND_NAME);
    doThrow(new RuntimeException("boom"))
        .when(pluginCommand).executeCommand(eq(projectIntegration), any(PluginCommandRQ.class));

    assertThatCode(() -> service.loadAttachment(
        event(11L, 7L, 3L, 5L, "rec-1")))
        .doesNotThrowAnyException();
  }

  private ExternalAttachmentLoadEvent event(Long logId, Long projectId, Long launchId,
      Long testItemId, String attachmentExternalId) {
    return event(logId, projectId, launchId, testItemId, attachmentExternalId, null);
  }

  private ExternalAttachmentLoadEvent event(Long logId, Long projectId, Long launchId,
      Long testItemId, String attachmentExternalId, String attachmentAttributeKey) {
    return new ExternalAttachmentLoadEvent(PLUGIN_ID, COMMAND_NAME, logId, projectId, launchId,
        testItemId, attachmentExternalId, attachmentAttributeKey);
  }

  private IntegrationType integrationType() {
    IntegrationType type = new IntegrationType();
    type.setName(PLUGIN_ID);
    return type;
  }
}
