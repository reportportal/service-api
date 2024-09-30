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

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.api.model.OrganizationUserBase.OrgRoleEnum;
import com.epam.reportportal.api.model.UserAssignmentRequest;
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
@Sql("/db/organization/all_tests_samples.sql")
class OrganizationUsersControllerTest extends BaseMvcTest {

  private static final String BASE_URL = "/organizations/%s/users";

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
    viewerToken =
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
  void Test_1() throws Exception {
    Long userId = 108L;
    List<UserProjectInfo> projects = new ArrayList<>();
    UserAssignmentRequest rq = new UserAssignmentRequest()
        .projects(projects)
        .id(userId);

    performAssignUserSuccess(201L, rq, adminToken);

    performAssignUserSuccess(202L, rq, managerToken);

    validateAssignedRoles(201L, userId, projects);
  }

  @Test
  @DisplayName("Admin, Manager sends request to assign UPSA user to External organization")
  void Test_2() throws Exception {
    Long orgId = 204L;
    Long userId = 109L;
    List<UserProjectInfo> projects = new ArrayList<>();
    UserAssignmentRequest rq = new UserAssignmentRequest()
        .projects(projects)
        .id(userId);

    performAssignUserFailed(orgId, rq, adminToken, status().isForbidden());
    performAssignUserFailed(orgId, rq, managerToken, status().isForbidden());
  }

  @Test
  @DisplayName("Admin, Manager sends request to assign UPSA user to NOT External organization")
  void Test_3() throws Exception {
    Long userId = 109L;
    List<UserProjectInfo> projects = new ArrayList<>();
    UserAssignmentRequest rq = new UserAssignmentRequest()
        .projects(projects)
        .id(userId);

    performAssignUserSuccess(201L, rq, adminToken);
    performAssignUserSuccess(202L, rq, managerToken);
    validateAssignedRoles(201L, userId, projects);
  }

  @Test
  @DisplayName("Admin, Manager sends request to assign NOT UPSA user to organization and project in this organization")
  void Test_4() throws Exception {
    Long orgId = 201L;
    Long userId = 108L;
    List<UserProjectInfo> projects = new ArrayList<>();

    UserAssignmentRequest rq = new UserAssignmentRequest()
        .projects(projects) //TODO: add project
        .id(userId);

    performAssignUserSuccess(201L, rq, adminToken);
    validateAssignedRoles(201L, userId, projects);

    performAssignUserSuccess(202L, rq, managerToken);
    validateAssignedRoles(202L, userId, projects);

  }

  @Test
  @DisplayName("Non admin user sends request to assign user to an organization (no assignment to this organization)")
  void Test_5() throws Exception {
    Long orgId = 204L;
    Long userId = 108L;
    UserAssignmentRequest rq = new UserAssignmentRequest()
        .projects(new ArrayList<>())
        .id(userId);

    performAssignUserFailed(orgId, rq, noOrgUser, status().isForbidden());
  }

  @Test
  @DisplayName("Member of organization sends request to assign user to an organization")
  void Test_6() throws Exception {
    Long userId = 108L;
    UserAssignmentRequest rq = new UserAssignmentRequest()
        .projects(new ArrayList<>())
        .id(userId);

    performAssignUserFailed(201L, rq, editorToken, status().isForbidden());
    performAssignUserFailed(202L, rq, viewerToken, status().isForbidden());
  }

  @Test
  @DisplayName("Member of organization sends request to assign user to an organization and project")
  void Test_7() throws Exception {
    Long orgId = 204L;
    Long userId = 108L;
    UserAssignmentRequest rq = new UserAssignmentRequest()
        .projects(new ArrayList<>())
        .id(userId);

    performAssignUserFailed(orgId, rq, noOrgUser, status().isForbidden());
  }

  @Test
  @DisplayName("Admin, Manager sends request to assign user to non-existent organization")
  void Test_8() throws Exception {
    Long nonExistentOrgId = 999L;
    Long userId = 108L;
    UserAssignmentRequest rq = new UserAssignmentRequest()
        .projects(new ArrayList<>())
        .id(userId);

    performAssignUserFailed(nonExistentOrgId, rq, managerToken, status().isNotFound());
    performAssignUserFailed(nonExistentOrgId, rq, adminToken, status().isNotFound());
  }

  @Test
  @DisplayName("Admin, Manager sends request to assign user to organization with invalid data (user_id, organization_id, organization role, project_id, project role)\tcode 400")
  void Test_9() throws Exception {
    // TODO ???
  }

  @Test
  @DisplayName("Admin, Manager can assign user with Manager role in organization, in case any projects assignment - 'Editor' role is set by default to the Manager")
  void Test_10() throws Exception {
    Long userId = 108L;
    List<UserProjectInfo> projects = new ArrayList<>();//TODO: add project
    UserAssignmentRequest rq = new UserAssignmentRequest()
        .id(userId)
        .projects(projects);
    rq.setOrgRole(OrgRoleEnum.MANAGER);

    performAssignUserSuccess(201L, rq, adminToken);
    performAssignUserSuccess(202L, rq, managerToken);
    // TODO check project role
    validateAssignedRoles(201L, userId, projects);
  }


  @Test
  @DisplayName("Admin, Manager sends request to assign user to organization with duplicate projects and project roles (ex. id=1, project_role=Viewer - twice mentioned in response)")
  void Test_11() throws Exception {
    Long userId = 108L;
    List<UserProjectInfo> projects = new ArrayList<>();//TODO: add project
    UserAssignmentRequest rq = new UserAssignmentRequest()
        .id(userId)
        .projects(projects);
    rq.setOrgRole(OrgRoleEnum.MANAGER);

    performAssignUserSuccess(201L, rq, adminToken);
    validateAssignedRoles(201L, userId, projects);

    performAssignUserSuccess(202L, rq, managerToken);
    validateAssignedRoles(202L, userId, projects);
  }

  @Test
  @DisplayName("Admin, Manager sends request to assign user to organization with duplicate projects and different project roles")
  void Test_12() throws Exception {
    Long userId = 108L;
    List<UserProjectInfo> projects = new ArrayList<>();//TODO: add project
    UserAssignmentRequest rq = new UserAssignmentRequest()
        .id(userId)
        .projects(projects);
    rq.setOrgRole(OrgRoleEnum.MANAGER);

    performAssignUserSuccess(201L, rq, adminToken);
    performAssignUserSuccess(202L, rq, managerToken);
    // TODO check project role DITOR
    validateAssignedRoles(201L, userId, projects);
  }

  @Test
  @DisplayName("Admin, Manager sends request to assign user who is already assigned to this organization")
  void Test_13() throws Exception {
    Long orgId = 201L;
    Long userId = 107L;
    UserAssignmentRequest rq = new UserAssignmentRequest()
        .projects(new ArrayList<>())
        .id(userId);

    performAssignUserFailed(orgId, rq, noOrgUser, status().is4xxClientError());
  }


  private UserAssignmentResponse performAssignUserSuccess(Long orgId, UserAssignmentRequest rq,
      String token)
      throws Exception {
    var result = performAssignUserRequest(BASE_URL.formatted(orgId), rq, token, status().isOk());

    return objectMapper.readValue(result, UserAssignmentResponse.class);
  }

  private String performAssignUserRequest(String url, UserAssignmentRequest rq, String token,
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


  private UserAssignmentResponse performAssignUserFailed(Long orgId, UserAssignmentRequest rq,
      String token, ResultMatcher status)
      throws Exception {
    var result = performAssignUserRequest(BASE_URL.formatted(orgId), rq, token, status);

    return objectMapper.readValue(result, UserAssignmentResponse.class);
  }

  private void validateAssignedRoles(long orgId, Long userId, List<UserProjectInfo> projects) {
    var orgUser = organizationUserRepository.findByUserIdAndOrganization_Id(userId, orgId);
    Assertions.assertTrue(orgUser.isPresent());

    projects.forEach(project -> {
      var projectUserId = new ProjectUserId();
      projectUserId.setProjectId(project.getId());
      projectUserId.setUserId(userId);

      var projectUser = projectUserRepository.getById(projectUserId);
      Assertions.assertNotNull(projectUser);
    });
  }

}
