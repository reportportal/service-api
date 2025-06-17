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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.api.model.Invitation;
import com.epam.reportportal.api.model.InvitationActivation;
import com.epam.reportportal.api.model.InvitationActivation.StatusEnum;
import com.epam.reportportal.api.model.InvitationRequest;
import com.epam.reportportal.api.model.InvitationRequestOrganizationsInner;
import com.epam.reportportal.api.model.InvitationStatus;
import com.epam.reportportal.api.model.OrgRole;
import com.epam.reportportal.api.model.ProjectRole;
import com.epam.reportportal.api.model.UserProjectInfo;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.dao.organization.OrganizationUserRepository;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql("/db/organization/full_organization_samples.sql")
class InvitationControllerTest extends BaseMvcTest {

  private static final String INVITATIONS_ENDPOINT = "/invitations";

  private static final InvitationActivation activationRq = new InvitationActivation()
      .fullName("Stanley Jobson")
      .password("sw0rd_Fi$h")
      .status(StatusEnum.ACTIVATED);

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  OrganizationUserRepository organizationUserRepository;
  @Autowired
  ProjectUserRepository projectUserRepository;

  @ParameterizedTest
  @CsvSource(value = {
      "MANAGER|EDITOR|1",
      "MANAGER|VIEWER|1",
      "MEMBER|EDITOR|1",
      "MEMBER|VIEWER|1",
      "MANAGER|EDITOR|2",
      "MANAGER|VIEWER|2",
      "MEMBER|EDITOR|2",
      "MEMBER|VIEWER|2"
  }, delimiter = '|')
  void createInvitationByAdmin(OrgRole orgRole, ProjectRole projectRole) throws Exception {
    var rq = getInvitationRequest(orgRole, projectRole);

    var result = mockMvc.perform(post(INVITATIONS_ENDPOINT)
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse().getContentAsString();

    var invitation = objectMapper.readValue(result, Invitation.class);

    assertNotNull(invitation);

    assertEquals(InvitationStatus.PENDING, invitation.getStatus());

    var storedInvitationString = mockMvc.perform(
            get(INVITATIONS_ENDPOINT + "/" + invitation.getId())
                .content(objectMapper.writeValueAsBytes(rq))
                .contentType(APPLICATION_JSON)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse().getContentAsString();
    var storedInvitation = objectMapper.readValue(storedInvitationString, Invitation.class);

    assertEquals(storedInvitation, invitation);

  }

  private static InvitationRequest getInvitationRequest(OrgRole orgRole, ProjectRole projectRole) {
    List<UserProjectInfo> projects = new ArrayList<>();
    InvitationRequestOrganizationsInner orgInfo = new InvitationRequestOrganizationsInner();
    UserProjectInfo projectInfo = new UserProjectInfo()
        .id(1L)
        .projectRole(projectRole);

    projects.add(projectInfo);

    orgInfo.setId(1L);
    orgInfo.setOrgRole(orgRole);
    orgInfo.setProjects(projects);

    List<InvitationRequestOrganizationsInner> organizations = new ArrayList<>();
    organizations.add(orgInfo);

    var rq = new InvitationRequest();

    rq.setEmail("invitation@example.com");
    rq.setOrganizations(organizations);
    return rq;
  }


  @ParameterizedTest
  @CsvSource(value = {
      "MANAGER|EDITOR|1",
      "MANAGER|VIEWER|1",
      "MEMBER|EDITOR|1",
      "MEMBER|VIEWER|1",
      "MANAGER|EDITOR|2",
      "MANAGER|VIEWER|2"
  }, delimiter = '|')
  void memberEditorNotEnoughPermissionsFor(OrgRole orgRole, ProjectRole projectRole, long prjId)
      throws Exception {
    List<UserProjectInfo> projects = new ArrayList<>();
    var orgInfo = new InvitationRequestOrganizationsInner();
    UserProjectInfo projectInfo = new UserProjectInfo()
        .id(prjId)
        .projectRole(projectRole);

    projects.add(projectInfo);

    orgInfo.setId(1L);
    orgInfo.setOrgRole(orgRole);
    orgInfo.setProjects(projects);

    List<InvitationRequestOrganizationsInner> organizations = new ArrayList<>();
    organizations.add(orgInfo);

    var rq = new InvitationRequest();

    rq.setEmail("invitation@example.com");
    rq.setOrganizations(organizations);

    mockMvc.perform(post(INVITATIONS_ENDPOINT)
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isForbidden());

  }


  @ParameterizedTest
  @CsvSource(value = {
      "MEMBER|VIEWER|2"
  }, delimiter = '|')
  void memberEditorCanInvite(OrgRole orgRole, ProjectRole projectRole, long prjId)
      throws Exception {
    List<UserProjectInfo> projects = new ArrayList<>();
    var orgInfo = new InvitationRequestOrganizationsInner();
    UserProjectInfo projectInfo = new UserProjectInfo()
        .id(prjId)
        .projectRole(projectRole);

    projects.add(projectInfo);

    orgInfo.setId(1L);
    orgInfo.setOrgRole(orgRole);
    orgInfo.setProjects(projects);

    List<InvitationRequestOrganizationsInner> organizations = new ArrayList<>();
    organizations.add(orgInfo);

    var rq = new InvitationRequest();

    rq.setEmail("invitation@example.com");
    rq.setOrganizations(organizations);

    mockMvc.perform(post(INVITATIONS_ENDPOINT)
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated());

  }

  @Test
  void activateInvitation() throws Exception {
    var rq = getInvitationRequest(OrgRole.MANAGER, ProjectRole.EDITOR);

    var invitationAsString = mockMvc.perform(post(INVITATIONS_ENDPOINT)
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse().getContentAsString();
    var invitation = objectMapper.readValue(invitationAsString, Invitation.class);

    mockMvc.perform(put(INVITATIONS_ENDPOINT + "/" + invitation.getId())
            // no token required
            .content(objectMapper.writeValueAsBytes(activationRq))
            .contentType(APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  void activateManagerViewerInvitation() throws Exception {
    var rq = getInvitationRequest(OrgRole.MANAGER, ProjectRole.VIEWER);

    var invitationAsString = mockMvc.perform(post(INVITATIONS_ENDPOINT)
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse().getContentAsString();
    var invitation = objectMapper.readValue(invitationAsString, Invitation.class);

    var response = objectMapper.readValue(mockMvc.perform(put(INVITATIONS_ENDPOINT + "/" + invitation.getId())
            // no token required
            .content(objectMapper.writeValueAsBytes(activationRq))
            .contentType(APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString(), Invitation.class);
    var pu = projectUserRepository.findProjectUserByUserIdAndProjectId(response.getUserId(), 1L).get();
    assertEquals(com.epam.ta.reportportal.entity.project.ProjectRole.EDITOR, pu.getProjectRole() );

  }

  @Test
  void activateInvitationNotFound() throws Exception {
    mockMvc.perform(put(INVITATIONS_ENDPOINT + "/" + UUID.randomUUID())
            // no token required
            .content(objectMapper.writeValueAsBytes(activationRq))
            .contentType(APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().is4xxClientError());
  }

  @Test
  void activateInvitationUserAlreadyExists() throws Exception {
    var rq = getInvitationRequest(OrgRole.MANAGER, ProjectRole.EDITOR);

    var invitationAsString = mockMvc.perform(post(INVITATIONS_ENDPOINT)
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse().getContentAsString();
    var invitation = objectMapper.readValue(invitationAsString, Invitation.class);

    var invitationAsString2 = mockMvc.perform(post(INVITATIONS_ENDPOINT)
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse().getContentAsString();
    var invitation2 = objectMapper.readValue(invitationAsString2, Invitation.class);

    mockMvc.perform(put(INVITATIONS_ENDPOINT + "/" + invitation.getId())
            // no token required
            .content(objectMapper.writeValueAsBytes(activationRq))
            .contentType(APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());

    mockMvc.perform(put(INVITATIONS_ENDPOINT + "/" + invitation2.getId())
            // no token required
            .content(objectMapper.writeValueAsBytes(activationRq))
            .contentType(APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  void createInvitationForExistingUserByAdmin() throws Exception {
    var rq = getInvitationRequest(OrgRole.MANAGER, ProjectRole.EDITOR);
    rq.setEmail("no-orgs-user@example.com");

    var result = mockMvc.perform(post(INVITATIONS_ENDPOINT)
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse().getContentAsString();

    var invitation = objectMapper.readValue(result, Invitation.class);

    assertNotNull(invitation);
    assertEquals(InvitationStatus.ACTIVATED, invitation.getStatus());
    organizationUserRepository.findByUserIdAndOrganization_Id(108L, 1L).orElseThrow();

    var prjIds = projectUserRepository.findProjectIdsByUserId(108L);
    var expectedPrjIds = rq.getOrganizations().stream()
        .flatMap(orgs -> orgs.getProjects().stream())
        .map(UserProjectInfo::getId)
        .toList();
    assertTrue(prjIds.containsAll(expectedPrjIds));

    mockMvc.perform(
            get(INVITATIONS_ENDPOINT + "/" + invitation.getId())
                .content(objectMapper.writeValueAsBytes(rq))
                .contentType(APPLICATION_JSON)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }
}
