/*
 * Copyright 2022 EPAM Systems
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

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.entity.attachment.BinaryData;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class PluginPublicControllerTest extends BaseMvcTest {

	@Test
	void shouldGetFileWhenAuthenticated() throws Exception {

		final ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[] {});
		final String contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType("image.png");

		final BinaryData binaryData = new BinaryData(contentType, (long) inputStream.available(), inputStream);
		when(pluginPublicFilesProvider.load("pluginName", "image.png")).thenReturn(binaryData);

		mockMvc.perform(get("/v1/plugin/public/pluginName/file/image.png").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());

		verify(binaryDataResponseWriter, times(1)).write(eq(binaryData), any(HttpServletResponse.class));
	}

	@Test
	void shouldGetFileWhenNotAuthenticated() throws Exception {
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[] {});
		final String contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType("image.png");

		final BinaryData binaryData = new BinaryData(contentType, (long) inputStream.available(), inputStream);
		when(pluginPublicFilesProvider.load("pluginName", "image.png")).thenReturn(binaryData);

		mockMvc.perform(get("/v1/plugin/public/pluginName/file/image.png")).andExpect(status().isOk());

		verify(binaryDataResponseWriter, times(1)).write(eq(binaryData), any(HttpServletResponse.class));
	}

	@Test
	void shouldExecutePublicCommandWhenAuthenticated() throws Exception {
		final String plugin = "signup";
		final String command = "testCommand";
		final Map<String, Object> params = Collections.emptyMap();
		final String ok = "{'result': 'ok'}";
		when(executeIntegrationHandler.executePublicCommand(plugin, command, params)).thenReturn(ok);

		mockMvc.perform(put("/v1/plugin/public/{plugin}/{command}", plugin, command)
						.with(token(oAuthHelper.getSuperadminToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString(ok)));

		verify(executeIntegrationHandler).executePublicCommand(eq(plugin), eq(command), eq(params));
	}

	@Test
	void shouldExecutePublicCommandWhenNotAuthenticated() throws Exception {
		final String plugin = "signup";
		final String command = "testCommand";
		final Map<String, Object> params = Collections.emptyMap();
		final String ok = "{'result': 'ok'}";
		when(executeIntegrationHandler.executePublicCommand(plugin, command, params)).thenReturn(ok);

		mockMvc.perform(put("/v1/plugin/public/{plugin}/{command}", plugin, command)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString(ok)));

		verify(executeIntegrationHandler).executePublicCommand(eq(plugin), eq(command), eq(params));
	}

}
