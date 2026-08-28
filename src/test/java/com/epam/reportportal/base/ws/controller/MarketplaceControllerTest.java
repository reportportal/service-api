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

package com.epam.reportportal.base.ws.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.core.marketplace.MarketplaceClient;
import com.epam.reportportal.base.core.marketplace.exception.RegistryUnreachableException;
import com.epam.reportportal.base.model.marketplace.MarketplacePlugin;
import com.epam.reportportal.base.ws.BaseMvcTest;
import java.net.SocketTimeoutException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Routing, guard and offline shape of {@code /v1/plugins}.
 */
class MarketplaceControllerTest extends BaseMvcTest {

  @MockBean
  private MarketplaceClient marketplaceClient;

  // The handler is a singleton in the shared context and remembers an unreachable host for a
  // while, so each test speaks to a registry of its own rather than to a shared verdict.

  @Test
  void catalogueIsReadableByAnyAuthenticatedUser() throws Exception {
    when(marketplaceClient.registryHost()).thenReturn("marketplace.reportportal.io");
    when(marketplaceClient.getCatalogue(any(), any())).thenReturn(List.of(
        new MarketplacePlugin("slack", "Slack", "2.0.0", "Notifier", "notifications", "public",
            "official", "slack")));

    mockMvc.perform(get("/v1/plugins").with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.registry.status").value("ONLINE"))
        .andExpect(jsonPath("$.registry.host").value("marketplace.reportportal.io"))
        .andExpect(jsonPath("$.available[0].id").value("slack"));
  }

  @Test
  void catalogueIsNotReadableWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/v1/plugins")).andExpect(status().isUnauthorized());
  }

  @Test
  void unreachableRegistryStillRendersThePage() throws Exception {
    when(marketplaceClient.registryHost()).thenReturn("offline.reportportal.test");
    when(marketplaceClient.getCatalogue(any(), any())).thenThrow(new RegistryUnreachableException(
        "offline.reportportal.test", new SocketTimeoutException("Read timed out")));

    mockMvc.perform(get("/v1/plugins?q=jira").with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.registry.status").value("OFFLINE"))
        .andExpect(jsonPath("$.registry.host").value("offline.reportportal.test"))
        .andExpect(jsonPath("$.available").isEmpty());
  }
}
