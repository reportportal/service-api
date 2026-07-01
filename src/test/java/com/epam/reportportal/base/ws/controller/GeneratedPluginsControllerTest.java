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

package com.epam.reportportal.base.ws.controller;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.INTEGRATION_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.api.model.PluginCommandContext;
import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql({"/db/organization/full_organization_samples.sql",
    "/db/organization/organization_integrations.sql"})
class GeneratedPluginsControllerTest extends BaseMvcTest {

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("Plugin command execution returns 200 with the handler result in the body")
  void executePluginCommandReturnsOk() throws Exception {
    var rq = new PluginCommandRQ(new PluginCommandContext(), Map.of("key", "value"));
    when(executeIntegrationHandler.executeExtensionCommand(eq("email"), eq("testConnection"), any()))
        .thenReturn(Map.of("result", true));

    mockMvc.perform(post("/plugins/email/commands/testConnection")
            .with(token(oAuthHelper.getDefaultToken()))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(rq)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value(true));
  }

  @Test
  @DisplayName("Plugin command execution without an auth token returns 401")
  void executePluginCommandReturnsUnauthorizedWhenTokenMissing() throws Exception {
    var rq = new PluginCommandRQ(new PluginCommandContext(), Map.of("key", "value"));

    mockMvc.perform(post("/plugins/email/commands/testConnection")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(rq)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Plugin command execution returns 404 when no integration is resolved")
  void executePluginCommandReturnsNotFoundWhenIntegrationMissing() throws Exception {
    var rq = new PluginCommandRQ(new PluginCommandContext(), Map.of("key", "value"));
    when(executeIntegrationHandler.executeExtensionCommand(eq("email"), eq("testConnection"), any()))
        .thenThrow(new ReportPortalException(INTEGRATION_NOT_FOUND, "email"));

    mockMvc.perform(post("/plugins/email/commands/testConnection")
            .with(token(oAuthHelper.getDefaultToken()))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(rq)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Plugin command execution returns 400 for an unknown command")
  void executePluginCommandReturnsBadRequestWhenCommandUnknown() throws Exception {
    var rq = new PluginCommandRQ(new PluginCommandContext(), Map.of("key", "value"));
    when(executeIntegrationHandler.executeExtensionCommand(eq("email"), eq("unknown"), any()))
        .thenThrow(new ReportPortalException(BAD_REQUEST_ERROR, "Command 'unknown' is not found"));

    mockMvc.perform(post("/plugins/email/commands/unknown")
            .with(token(oAuthHelper.getDefaultToken()))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(rq)))
        .andExpect(status().isBadRequest());
  }
}
