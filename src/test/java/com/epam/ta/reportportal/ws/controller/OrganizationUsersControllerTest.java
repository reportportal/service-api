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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.api.model.OrgRole;
import com.epam.reportportal.api.model.OrgUserAssignment;
import com.epam.reportportal.api.model.ProjectRole;
import com.epam.reportportal.api.model.UserAssignmentResponse;
import com.epam.reportportal.api.model.UserProjectInfo;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.dao.organization.OrganizationUserRepository;
import com.epam.ta.reportportal.entity.user.ProjectUserId;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@Log4j2
@Sql("/db/organization/full_organization_samples.sql")
class OrganizationUsersControllerTest extends BaseMvcTest {

  private static final String BASE_URL = "/organizations/%s/users";
  public static final Long ORG_ID_1 = 201L;
  public static final Long ORG_ID_2 = 202L;
  public static final Long ORG_ID_3 = 203L;
  public static final Long PRJ_ID_1 = 301L;
  public static final Long PRJ_ID_2 = 302L;

  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private ProjectUserRepository projectUserRepository;
  @Autowired
  private OrganizationUserRepository organizationUserRepository;

  private String adminToken;
  private String managerToken;
  private String editorToken;
  private String viewerToken;
  private String noOrgUser;
  private String noProjectsUser;

  @BeforeEach
  void beforeEach() {
    adminToken =
        oAuthHelper.createAccessToken("admin", "erebus", UserRole.ADMINISTRATOR)
            .getValue();
    managerToken =
        oAuthHelper.createAccessToken("user-manager", "erebus", UserRole.USER)
            .getValue();
    editorToken =
        oAuthHelper.createAccessToken("user-member-editor", "erebus", UserRole.USER)
            .getValue();
    viewerToken =
        oAuthHelper.createAccessToken("user-member-viewer", "erebus", UserRole.USER)
            .getValue();
    noProjectsUser =
        oAuthHelper.createAccessToken("no-projects-user", "erebus", UserRole.USER)
            .getValue();
    noOrgUser =
        oAuthHelper.createAccessToken("no-orgs-user", "erebus", UserRole.USER)
            .getValue();
  }


  @Test
  void getOrganizationUsersForAdmin() throws Exception {
    mockMvc.perform(get(BASE_URL.formatted(1))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void getOrganizationUsersForUser() throws Exception {
    mockMvc.perform(get(BASE_URL.formatted(1))
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Admin/Manager sends request to assign Internal user to organization")
  void assignInternalUser() throws Exception {
    Long userId = 108L;
    List<UserProjectInfo> projects = new ArrayList<>();
    OrgUserAssignment rq = new OrgUserAssignment()
        .projects(projects)
        .id(userId);

    performAssignUserSuccess(ORG_ID_1, rq, adminToken);
    validateAssignedRoles(ORG_ID_1, userId, projects);

    performAssignUserSuccess(ORG_ID_2, rq, managerToken);
    validateAssignedRoles(ORG_ID_2, userId, projects);
  }

  @Test
  @DisplayName("Admin/Manager sends request to assign Internal user to organization as manager")
  void assignInternalUserAsMember() throws Exception {
    Long userId = 108L;
    List<UserProjectInfo> projects = new ArrayList<>();
    OrgUserAssignment rq = new OrgUserAssignment()
        .orgRole(OrgRole.MANAGER)
        .projects(projects)
        .id(userId);

    performAssignUserSuccess(ORG_ID_1, rq, adminToken);
    validateAssignedRoles(ORG_ID_1, userId, projects);
    var orgUser = organizationUserRepository.findByUserIdAndOrganization_Id(userId, ORG_ID_1);

    assertEquals(orgUser.get().getOrganizationRole().toString(), OrgRole.MANAGER.toString());

  }

  @Test
  @DisplayName("Admin, Manager sends request to assign UPSA user to External organization")
  void assignUpsaToExternalOrg() throws Exception {
    Long orgId = 204L;
    Long userId = 109L;
    List<UserProjectInfo> projects = new ArrayList<>();
    OrgUserAssignment rq = new OrgUserAssignment()
        .projects(projects)
        .id(userId);

    performAssignUserFailed(orgId, rq, adminToken, status().isForbidden());
    performAssignUserFailed(orgId, rq, managerToken, status().isForbidden());
  }

  @Test
  @DisplayName("Admin, Manager sends request to assign UPSA user to NOT External organization")
  void assignInternalUserToExternalOrg() throws Exception {
    Long userId = 109L;
    List<UserProjectInfo> projects = new ArrayList<>();
    OrgUserAssignment rq = new OrgUserAssignment()
        .projects(projects)
        .id(userId);

    performAssignUserSuccess(ORG_ID_1, rq, adminToken);
    validateAssignedRoles(ORG_ID_1, userId, projects);

    performAssignUserSuccess(ORG_ID_2, rq, managerToken);
    validateAssignedRoles(ORG_ID_2, userId, projects);
  }

  @Test
  @DisplayName("Admin, Manager sends request to assign NOT UPSA user to organization and project in this organization")
  void assignInternalUserWithProjects() throws Exception {
    Long userId = 108L;
    UserProjectInfo project1 = new UserProjectInfo()
        .projectRole(ProjectRole.EDITOR)
        .id(302L);
    UserProjectInfo project2 = new UserProjectInfo()
        .projectRole(ProjectRole.EDITOR)
        .id(303L);
    List<UserProjectInfo> projects = new ArrayList<>(List.of(project1, project2));

    OrgUserAssignment rq = new OrgUserAssignment()
        .projects(projects)
        .id(userId);

    performAssignUserSuccess(ORG_ID_2, rq, managerToken);
    validateAssignedRoles(ORG_ID_2, userId, projects);

  }

  @Test
  @DisplayName("Non admin user sends request to assign user to an organization (no assignment to this organization)")
  void assignUserByNobody() throws Exception {
    Long orgId = 204L;
    Long userId = 108L;
    OrgUserAssignment rq = new OrgUserAssignment()
        .projects(new ArrayList<>())
        .id(userId);

    performAssignUserFailed(orgId, rq, noOrgUser, status().isForbidden());
  }

  @Test
  @DisplayName("Member of organization sends request to assign user to an organization")
  void assignUserByMember() throws Exception {
    Long userId = 108L;
    OrgUserAssignment rq = new OrgUserAssignment()
        .projects(new ArrayList<>())
        .id(userId);

    performAssignUserFailed(201L, rq, editorToken, status().isForbidden());
    performAssignUserFailed(202L, rq, viewerToken, status().isForbidden());
  }

  @Test
  @DisplayName("Member of organization sends request to assign user to an organization and project")
  void assignUserByMemberWithProjects() throws Exception {
    Long orgId = 204L;
    Long userId = 108L;
    OrgUserAssignment rq = new OrgUserAssignment()
        .projects(new ArrayList<>())
        .id(userId);

    performAssignUserFailed(orgId, rq, noOrgUser, status().isForbidden());
  }

  @Test
  @DisplayName("Admin, Manager sends request to assign user to non-existent organization")
  void organizationNotFound() throws Exception {
    Long nonExistentOrgId = 999L;
    Long userId = 108L;
    OrgUserAssignment rq = new OrgUserAssignment()
        .projects(new ArrayList<>())
        .id(userId);

    performAssignUserFailed(nonExistentOrgId, rq, managerToken, status().isNotFound());
    performAssignUserFailed(nonExistentOrgId, rq, adminToken, status().isNotFound());
  }


  @Test
  @DisplayName("Admin, Manager can assign user with Manager role in organization, in case any projects assignment - 'Editor' role is set by default to the Manager")
  void assignWithManagerAndSetEditorRole() throws Exception {
    Long userId = 108L;
    UserProjectInfo project1 = new UserProjectInfo()
        .projectRole(ProjectRole.VIEWER)
        .id(302L);

    List<UserProjectInfo> projects = new ArrayList<>(List.of(project1));

    OrgUserAssignment rq = new OrgUserAssignment()
        .projects(projects)
        .id(userId);
    rq.setOrgRole(OrgRole.MANAGER);

    performAssignUserSuccess(ORG_ID_2, rq, managerToken);
    validateAssignedRoles(ORG_ID_2, userId, projects);

    var prUser = projectUserRepository
        .findProjectUserByUserIdAndProjectId(rq.getId(), 302L);
    assertTrue(prUser.isPresent());
    prUser.map(pru -> {
      Assertions.assertEquals(ProjectRole.EDITOR.toString(), pru.getProjectRole().toString());
      return pru;
    });
  }


  @Test
  @DisplayName("Admin, Manager sends request to assign user to organization with duplicate projects and project roles")
  void duplicateProjectRoles() throws Exception {
    Long userId = 108L;
    UserProjectInfo project1 = new UserProjectInfo()
        .projectRole(ProjectRole.VIEWER)
        .id(302L);

    List<UserProjectInfo> projects = new ArrayList<>(List.of(project1, project1, project1));

    OrgUserAssignment rq = new OrgUserAssignment()
        .projects(projects)
        .id(userId);
    rq.setOrgRole(OrgRole.MEMBER);

    performAssignUserSuccess(ORG_ID_2, rq, managerToken);
    validateAssignedRoles(ORG_ID_2, userId, projects);

    var prUser = projectUserRepository
        .findProjectUserByUserIdAndProjectId(rq.getId(), 302L);
    assertTrue(prUser.isPresent());
    prUser.map(pru -> {
      Assertions.assertEquals(ProjectRole.VIEWER.toString(), pru.getProjectRole().toString());
      return pru;
    });
  }

  @Test
  @DisplayName("Admin, Manager sends request to assign user to organization with duplicate projects and different project roles")
  void duplicatePorjectsWithDifferentRoles() throws Exception {
    Long userId = 108L;
    UserProjectInfo project1 = new UserProjectInfo()
        .projectRole(ProjectRole.VIEWER)
        .id(302L);
    UserProjectInfo project2 = new UserProjectInfo()
        .projectRole(ProjectRole.EDITOR)
        .id(302L);

    List<UserProjectInfo> projects = new ArrayList<>(List.of(project1, project2, project1));

    OrgUserAssignment rq = new OrgUserAssignment()
        .projects(projects)
        .id(userId);
    rq.setOrgRole(OrgRole.MEMBER);

    performAssignUserSuccess(ORG_ID_2, rq, managerToken);
    validateAssignedRoles(ORG_ID_2, userId, projects);

    var prUser = projectUserRepository
        .findProjectUserByUserIdAndProjectId(rq.getId(), 302L);
    assertTrue(prUser.isPresent());
    prUser.map(pru -> {
      Assertions.assertEquals(ProjectRole.EDITOR.toString(), pru.getProjectRole().toString());
      return pru;
    });
  }

  @Test
  @DisplayName("Admin, Manager sends request to assign user who is already assigned to this organization")
  void alreadyAssigned() throws Exception {
    Long orgId = 201L;
    Long userId = 107L;
    OrgUserAssignment rq = new OrgUserAssignment()
        .projects(new ArrayList<>())
        .id(userId);

    performAssignUserFailed(orgId, rq, noOrgUser, status().is4xxClientError());
  }


  private UserAssignmentResponse performAssignUserSuccess(Long orgId, OrgUserAssignment rq,
      String token)
      throws Exception {
    var result = performAssignUserRequest(BASE_URL.formatted(orgId), rq, token, status().isOk());

    return objectMapper.readValue(result, UserAssignmentResponse.class);
  }

  private String performAssignUserRequest(String url, OrgUserAssignment rq, String token,
      ResultMatcher status) throws Exception {
    return mockMvc.perform(MockMvcRequestBuilders
            .post(url)
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(token)))
        .andExpect(status)
        .andReturn()
        .getResponse()
        .getContentAsString();
  }


  private UserAssignmentResponse performAssignUserFailed(Long orgId, OrgUserAssignment rq,
      String token, ResultMatcher status)
      throws Exception {
    var result = performAssignUserRequest(BASE_URL.formatted(orgId), rq, token, status);

    return objectMapper.readValue(result, UserAssignmentResponse.class);
  }

  private void validateAssignedRoles(long orgId, Long userId, List<UserProjectInfo> projects) {
    var orgUser = organizationUserRepository.findByUserIdAndOrganization_Id(userId, orgId);
    assertTrue(orgUser.isPresent());

    projects.forEach(project -> {
      var projectUserId = new ProjectUserId();
      projectUserId.setProjectId(project.getId());
      projectUserId.setUserId(userId);

      var projectUser = projectUserRepository.getById(projectUserId);
      Assertions.assertNotNull(projectUser);
    });
  }

}
