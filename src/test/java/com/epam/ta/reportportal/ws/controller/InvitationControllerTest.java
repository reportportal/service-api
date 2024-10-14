/*
 * Copyright 2024 EPAM Systems
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.api.model.Invitation;
import com.epam.reportportal.api.model.InvitationRequest;
import com.epam.reportportal.api.model.InvitationRequestOrganizationsInner;
import com.epam.reportportal.api.model.InvitationStatus;
import com.epam.reportportal.api.model.OrgRole;
import com.epam.reportportal.api.model.ProjectRole;
import com.epam.reportportal.api.model.UserProjectInfo;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

class InvitationControllerTest extends BaseMvcTest {

  private static final String INVITATIONS_ENDPOINT = "/invitations";

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void createInvitationByAdmin() throws Exception {
    List<UserProjectInfo> projects = new ArrayList<>();
    InvitationRequestOrganizationsInner orgInfo = new InvitationRequestOrganizationsInner();
    UserProjectInfo projectInfo = new UserProjectInfo()
        .id(1L)
        .projectRole(ProjectRole.VIEWER);

    projects.add(projectInfo);

    orgInfo.setId(1L);
    orgInfo.setOrgRole(OrgRole.MANAGER);
    orgInfo.setProjects(projects);

    List<InvitationRequestOrganizationsInner> organizations = new ArrayList<>();
    organizations.add(orgInfo);

    var rq = new InvitationRequest();

    rq.setEmail("invitation@example.com");
    rq.setOrganizations(organizations);

    var result = mockMvc.perform(MockMvcRequestBuilders.post(INVITATIONS_ENDPOINT)
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse().getContentAsString();

    var invitation = objectMapper.readValue(result, Invitation.class);

    assertNotNull(invitation);

    assertEquals(InvitationStatus.PENDING, invitation.getStatus());

  }


  @Test
  void createInvitationNotEnoughPermissions() throws Exception {
    List<UserProjectInfo> projects = new ArrayList<>();
    var orgInfo = new InvitationRequestOrganizationsInner();
    UserProjectInfo projectInfo = new UserProjectInfo()
        .id(1L)
        .projectRole(ProjectRole.VIEWER);

    projects.add(projectInfo);

    orgInfo.setId(1L);
    orgInfo.setOrgRole(OrgRole.MANAGER);
    orgInfo.setProjects(projects);

    List<InvitationRequestOrganizationsInner> organizations = new ArrayList<>();
    organizations.add(orgInfo);

    var rq = new InvitationRequest();

    rq.setEmail("invitation@example.com");
    rq.setOrganizations(organizations);

    mockMvc.perform(MockMvcRequestBuilders.post(INVITATIONS_ENDPOINT)
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isForbidden());

  }
}
