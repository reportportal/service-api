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

package com.epam.ta.reportportal.core.integration.plugin.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.activity.PluginUploadedEvent;
import com.epam.ta.reportportal.core.integration.impl.util.IntegrationTestUtil;
import com.epam.ta.reportportal.core.integration.plugin.IntegrationTypeHandler;
import com.epam.ta.reportportal.core.integration.plugin.file.PluginFileManager;
import com.epam.ta.reportportal.core.integration.plugin.info.PluginInfoResolver;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.core.plugin.PluginInfo;
import com.epam.ta.reportportal.core.plugin.PluginMetadata;
import com.epam.ta.reportportal.core.plugin.PluginPathInfo;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class CreatePluginHandlerTest {

  public static final String PLUGIN_ID = "jira";
  public static final String WRONG_PLUGIN_ID = "JERA";
  public static final String PLUGIN_VERSION = "1.0.0";
  public static final String FILE_NAME = "file.jar";

  private final PluginInfo pluginInfo = mock(PluginInfo.class);
  private final PluginPathInfo pluginPathInfo = mock(PluginPathInfo.class);

  private final MultipartFile multipartFile = mock(MultipartFile.class);

  private final InputStream inputStream = mock(InputStream.class);

  private final PluginFileManager pluginFileManager = mock(PluginFileManager.class);
  private final PluginInfoResolver pluginInfoResolver = mock(PluginInfoResolver.class);
  private final Pf4jPluginBox pluginBox = mock(Pf4jPluginBox.class);
  private final IntegrationTypeHandler integrationTypeHandler = mock(IntegrationTypeHandler.class);


  private final ApplicationEventPublisher applicationEventPublisher =
      mock(ApplicationEventPublisher.class);

  private final CreatePluginHandlerImpl createPluginHandler = new CreatePluginHandlerImpl(
      pluginFileManager,
      pluginInfoResolver,
      pluginBox,
      integrationTypeHandler,
      applicationEventPublisher
  );

  @Test
  void shouldUploadPluginWhenValid() throws IOException {

    when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);
    when(multipartFile.getInputStream()).thenReturn(inputStream);

    final Path path = Path.of("");
    when(pluginFileManager.uploadTemp(multipartFile)).thenReturn(path);
    when(pluginInfoResolver.resolveInfo(path)).thenReturn(pluginInfo);

    when(pluginInfo.getId()).thenReturn(PLUGIN_ID);
    when(pluginInfo.getVersion()).thenReturn(PLUGIN_VERSION);

    when(pluginFileManager.download(pluginInfo)).thenReturn(pluginPathInfo);

    when(pluginPathInfo.getPluginPath()).thenReturn(path);

    final IntegrationType jiraIntegrationType = IntegrationTestUtil.getJiraIntegrationType();

    when(integrationTypeHandler.getByName(anyString())).thenReturn(Optional.empty());
    when(integrationTypeHandler.create(any(PluginMetadata.class))).thenReturn(jiraIntegrationType);

    doNothing().when(applicationEventPublisher).publishEvent(any());

    ReportPortalUser reportPortalUser = mock(ReportPortalUser.class);

    EntryCreatedRS entryCreatedRS =
        createPluginHandler.uploadPlugin(multipartFile, reportPortalUser);

    assertNotNull(entryCreatedRS);
    assertEquals(jiraIntegrationType.getId(), entryCreatedRS.getId());

    verify(pluginBox, times(1)).startUpPlugin(path);
    verify(applicationEventPublisher, times(1)).publishEvent(isA(PluginUploadedEvent.class));
  }
}