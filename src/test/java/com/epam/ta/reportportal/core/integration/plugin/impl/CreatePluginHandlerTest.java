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

import com.epam.ta.reportportal.core.integration.impl.util.IntegrationTestUtil;
import com.epam.ta.reportportal.core.integration.plugin.CreatePluginHandler;
import com.epam.ta.reportportal.core.plugin.PluginInfo;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class CreatePluginHandlerTest {

	public static final String PLUGIN_ID = "jira";
	public static final String WRONG_PLUGIN_ID = "JERA";
	public static final String PLUGIN_VERSION = "1.0.0";
	public static final String FILE_NAME = "file.jar";

	private final PluginInfo pluginInfo = mock(PluginInfo.class);

	private final MultipartFile multipartFile = mock(MultipartFile.class);

	private final Pf4jPluginBox pluginBox = mock(Pf4jPluginBox.class);

	private final InputStream inputStream = mock(InputStream.class);

	private final ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);

	private final CreatePluginHandler createPluginHandler = new CreatePluginHandlerImpl(pluginBox);

	@Test
	void shouldUploadPluginWhenValid() throws IOException {

		when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);

		when(multipartFile.getInputStream()).thenReturn(inputStream);

		when(pluginInfo.getId()).thenReturn(PLUGIN_ID);
		when(pluginInfo.getVersion()).thenReturn(PLUGIN_VERSION);

		doNothing().when(applicationEventPublisher).publishEvent(any());
		when(pluginBox.uploadPlugin(FILE_NAME, inputStream)).thenReturn(IntegrationTestUtil.getJiraIntegrationType());


		EntryCreatedRS entryCreatedRS = createPluginHandler.uploadPlugin(multipartFile);

		assertNotNull(entryCreatedRS);
		assertEquals(IntegrationTestUtil.getJiraIntegrationType().getId(), entryCreatedRS.getId());
	}
}