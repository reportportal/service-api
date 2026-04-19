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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.api.model.CreateOrgIntegrationRequest;
import com.epam.reportportal.api.model.OrganizationIntegration;
import com.epam.reportportal.api.model.OrganizationIntegrationPage;
import com.epam.reportportal.api.model.UpdateOrgIntegrationRequest;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 */
@Sql({"/db/organization/full_organization_samples.sql",
    "/db/organization/organization_integrations.sql"})
class OrganizationIntegrationsControllerTest extends BaseMvcTest {

  @Autowired
  private ObjectMapper objectMapper;

  // ── GET /organizations/{org_id}/integrations ──────────────────────────────

  @Test
  void getIntegrations_admin_ok() throws Exception {
    mockMvc.perform(get("/organizations/201/integrations")
            .with(token(adminToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray());
  }

  @Test
  void getIntegrations_manager_ok() throws Exception {
    mockMvc.perform(get("/organizations/201/integrations")
            .with(token(managerToken)))
        .andExpect(status().isOk());
  }

  @Test
  void getIntegrations_member_ok() throws Exception {
    mockMvc.perform(get("/organizations/201/integrations")
            .with(token(viewerToken)))
        .andExpect(status().isOk());
  }

  @Test
  void getIntegrations_noMembership_forbidden() throws Exception {
    mockMvc.perform(get("/organizations/201/integrations")
            .with(token(noOrgUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getIntegrations_unknownOrg_notFound() throws Exception {
    mockMvc.perform(get("/organizations/999/integrations")
            .with(token(adminToken)))
        .andExpect(status().isNotFound());
  }

  @Test
  void getIntegrations_returnsOnlyOrgIntegrations() throws Exception {
    var result = mockMvc.perform(get("/organizations/201/integrations")
            .with(token(adminToken)))
        .andExpect(status().isOk())
        .andReturn();

    var page = objectMapper.readValue(
        result.getResponse().getContentAsString(), OrganizationIntegrationPage.class);

    assertEquals(2, page.getTotalCount());
    page.getItems().forEach(i -> assertNotNull(i.getId()));
  }

  @Test
  void getIntegrations_pagination_limitsResults() throws Exception {
    var result = mockMvc.perform(get("/organizations/201/integrations?offset=0&limit=1")
            .with(token(adminToken)))
        .andExpect(status().isOk())
        .andReturn();

    var page = objectMapper.readValue(
        result.getResponse().getContentAsString(), OrganizationIntegrationPage.class);

    assertEquals(1, page.getItems().size());
    assertEquals(2, page.getTotalCount());
  }

  // ── GET /organizations/{org_id}/integrations/{integration_id} ─────────────

  @Test
  void getIntegrationById_admin_ok() throws Exception {
    mockMvc.perform(get("/organizations/201/integrations/901")
            .with(token(adminToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(901))
        .andExpect(jsonPath("$.name").value("jira-org-integration"));
  }

  @Test
  void getIntegrationById_member_ok() throws Exception {
    mockMvc.perform(get("/organizations/201/integrations/901")
            .with(token(viewerToken)))
        .andExpect(status().isOk());
  }

  @Test
  void getIntegrationById_noMembership_forbidden() throws Exception {
    mockMvc.perform(get("/organizations/201/integrations/901")
            .with(token(noOrgUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getIntegrationById_notFound() throws Exception {
    mockMvc.perform(get("/organizations/201/integrations/999")
            .with(token(adminToken)))
        .andExpect(status().isNotFound());
  }

  @Test
  void getIntegrationById_wrongOrg_notFound() throws Exception {
    // integration 903 belongs to org 202, not org 201
    mockMvc.perform(get("/organizations/201/integrations/903")
            .with(token(adminToken)))
        .andExpect(status().isNotFound());
  }

  // ── POST /organizations/{org_id}/integrations ─────────────────────────────

  @Test
  void createIntegration_manager_created() throws Exception {
    when(pluginBox.getInstance(eq("jira"), eq(BtsExtension.class))).thenReturn(
        Optional.of(extension));
    when(extension.testConnection(any())).thenReturn(true);

    var request = new CreateOrgIntegrationRequest()
        .name("new-jira-integration")
        .pluginId("jira")
        .enabled(true)
        .parameters(Map.of("url", "http://jira.test", "project", "TEST", "authType", "BASIC",
            "username", "testuser", "password", "testpass"));

    var result = mockMvc.perform(post("/organizations/201/integrations")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(request))
            .with(token(managerToken)))
        .andExpect(status().isCreated())
        .andReturn();

    var created = objectMapper.readValue(
        result.getResponse().getContentAsString(), OrganizationIntegration.class);
    assertNotNull(created.getId());
    assertEquals("new-jira-integration", created.getName());
  }

  @Test
  void createIntegration_duplicateName_conflict() throws Exception {
    var request = new CreateOrgIntegrationRequest()
        .name("jira-org-integration")
        .pluginId("jira")
        .enabled(true)
        .parameters(Map.of("url", "http://jira.test", "project", "TEST"));

    mockMvc.perform(post("/organizations/201/integrations")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(request))
            .with(token(managerToken)))
        .andExpect(status().isConflict());
  }

  @Test
  void createIntegration_unknownPlugin_notFound() throws Exception {
    var request = new CreateOrgIntegrationRequest()
        .name("test-integration")
        .pluginId("unknown-plugin")
        .enabled(true);

    mockMvc.perform(post("/organizations/201/integrations")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(request))
            .with(token(managerToken)))
        .andExpect(status().isNotFound());
  }

  @Test
  void createIntegration_member_forbidden() throws Exception {
    var request = new CreateOrgIntegrationRequest()
        .name("test-integration")
        .pluginId("jira")
        .enabled(true);

    mockMvc.perform(post("/organizations/201/integrations")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(request))
            .with(token(viewerToken)))
        .andExpect(status().isForbidden());
  }

  @Test
  void createIntegration_unknownOrg_notFound() throws Exception {
    var request = new CreateOrgIntegrationRequest()
        .name("test-integration")
        .pluginId("jira")
        .enabled(true);

    mockMvc.perform(post("/organizations/999/integrations")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(request))
            .with(token(adminToken)))
        .andExpect(status().isNotFound());
  }

  // ── PUT /organizations/{org_id}/integrations/{integration_id} ────────────

  @Test
  void updateIntegration_manager_ok() throws Exception {
    when(pluginBox.getInstance(eq("jira"), eq(BtsExtension.class))).thenReturn(
        Optional.of(extension));
    when(extension.testConnection(any())).thenReturn(true);

    var request = new UpdateOrgIntegrationRequest()
        .name("updated-jira-integration")
        .enabled(false)
        .parameters(Map.of("url", "http://jira-updated.test", "project", "NEW", "authType", "BASIC",
            "username", "testuser", "password", "testpass"));

    mockMvc.perform(put("/organizations/201/integrations/901")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(request))
            .with(token(managerToken)))
        .andExpect(status().isOk());
  }

  @Test
  void updateIntegration_member_forbidden() throws Exception {
    var request = new UpdateOrgIntegrationRequest().name("updated");

    mockMvc.perform(put("/organizations/201/integrations/901")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(request))
            .with(token(viewerToken)))
        .andExpect(status().isForbidden());
  }

  @Test
  void updateIntegration_notFound() throws Exception {
    var request = new UpdateOrgIntegrationRequest().name("updated");

    mockMvc.perform(put("/organizations/201/integrations/999")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(request))
            .with(token(managerToken)))
        .andExpect(status().isNotFound());
  }

  // ── DELETE /organizations/{org_id}/integrations/{integration_id} ──────────

  @Test
  void deleteIntegration_manager_noContent() throws Exception {
    mockMvc.perform(delete("/organizations/201/integrations/901")
            .with(token(managerToken)))
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteIntegration_admin_noContent() throws Exception {
    mockMvc.perform(delete("/organizations/201/integrations/901")
            .with(token(adminToken)))
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteIntegration_member_forbidden() throws Exception {
    mockMvc.perform(delete("/organizations/201/integrations/901")
            .with(token(viewerToken)))
        .andExpect(status().isForbidden());
  }

  @Test
  void deleteIntegration_notFound() throws Exception {
    mockMvc.perform(delete("/organizations/201/integrations/999")
            .with(token(managerToken)))
        .andExpect(status().isNotFound());
  }
}
