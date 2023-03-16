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

package com.epam.ta.reportportal.ws.controller;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.ta.reportportal.entity.attachment.BinaryData;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import java.io.ByteArrayInputStream;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class PluginControllerTest extends BaseMvcTest {

  @Test
  void getLaunchPositive() throws Exception {
    mockMvc.perform(get("/v1/plugin").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void shouldGetFileWhenAuthenticated() throws Exception {

    final ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{});
    final String contentType = MimetypesFileTypeMap.getDefaultFileTypeMap()
        .getContentType("image.png");

    final BinaryData binaryData = new BinaryData(contentType, (long) inputStream.available(),
        inputStream);
    when(pluginFilesProvider.load("pluginName", "image.png")).thenReturn(binaryData);

    mockMvc.perform(
            get("/v1/plugin/pluginName/file/image.png").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    verify(binaryDataResponseWriter, times(1)).write(eq(binaryData),
        any(HttpServletResponse.class));
  }

  @Test
  void shouldNotGetFileWhenNotAuthenticated() throws Exception {
    mockMvc.perform(get("/v1/plugin/pluginName/file/image.png"))
        .andExpect(status().isUnauthorized());
  }

}