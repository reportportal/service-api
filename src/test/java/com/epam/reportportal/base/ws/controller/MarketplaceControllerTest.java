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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.core.marketplace.exception.RegistryUnreachableException;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.marketplace.MarketplaceInstallResource;
import com.epam.reportportal.base.model.marketplace.MarketplacePlugin;
import com.epam.reportportal.base.ws.BaseMvcTest;
import java.net.SocketTimeoutException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/**
 * Routing, guard and offline shape of {@code /v1/plugins}.
 */
class MarketplaceControllerTest extends BaseMvcTest {

  // marketplaceClient and installMarketplacePluginHandler are mocked on BaseMvcTest so this class
  // shares its context; both are reset between tests, so each test speaks to a registry of its own.

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
  void installIsAdminOnly() throws Exception {
    mockMvc.perform(post("/v1/plugins/slack/install")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"version\":\"2.0.0\"}")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isForbidden());
    verifyNoInteractions(installMarketplacePluginHandler);
  }

  @Test
  void installIsNotReachableWithoutAuthentication() throws Exception {
    mockMvc.perform(post("/v1/plugins/slack/install")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"version\":\"2.0.0\"}"))
        .andExpect(status().isUnauthorized());
    verifyNoInteractions(installMarketplacePluginHandler);
  }

  @Test
  void installReachesTheHandlerForAnAdmin() throws Exception {
    when(installMarketplacePluginHandler.install(eq("slack"), any(), any()))
        .thenReturn(new MarketplaceInstallResource(9L, "slack", "slack", "2.0.0"));

    mockMvc.perform(post("/v1/plugins/slack/install")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"version\":\"2.0.0\"}")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.integrationTypeId").value(9))
        .andExpect(jsonPath("$.version").value("2.0.0"));
  }

  // The three the reviewer has to be able to tell apart on the wire: a version that does not
  // exist, a registry that answered unusably, and a registry that did not answer at all.

  @Test
  void aVersionTheRegistryDoesNotHaveIsNotFound() throws Exception {
    when(installMarketplacePluginHandler.install(eq("slack"), any(), any())).thenThrow(
        new ReportPortalException(ErrorType.MARKETPLACE_PLUGIN_NOT_FOUND,
            "version '9.9.9' of plugin 'slack' is not in the registry at 'registry.test'"));

    mockMvc.perform(post("/v1/plugins/slack/install")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"version\":\"9.9.9\"}")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("9.9.9")));
  }

  @Test
  void aRegistryThatAnsweredUnusablyIsABadGateway() throws Exception {
    when(installMarketplacePluginHandler.install(eq("slack"), any(), any())).thenThrow(
        new ReportPortalException(ErrorType.MARKETPLACE_REGISTRY_ERROR,
            "Unreadable artifact response for 'slack:2.0.0'"));

    mockMvc.perform(post("/v1/plugins/slack/install")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"version\":\"2.0.0\"}")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadGateway());
  }

  @Test
  void aRegistryThatCannotBeReachedIsServiceUnavailable() throws Exception {
    when(installMarketplacePluginHandler.install(eq("slack"), any(), any())).thenThrow(
        new ReportPortalException(ErrorType.MARKETPLACE_REGISTRY_UNREACHABLE,
            "Marketplace registry at 'registry.test' is unreachable: connect timed out"));

    mockMvc.perform(post("/v1/plugins/slack/install")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"version\":\"2.0.0\"}")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isServiceUnavailable());
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
