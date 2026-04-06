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

package com.epam.reportportal.base.ws.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.api.model.FilterOperation;
import com.epam.reportportal.api.model.OperationType;
import com.epam.reportportal.api.model.OrgRole;
import com.epam.reportportal.api.model.OrganizationPage;
import com.epam.reportportal.api.model.PatchOperation;
import com.epam.reportportal.api.model.SearchCriteriaRQ;
import com.epam.reportportal.api.model.SearchCriteriaSearchCriteriaInner;
import com.epam.reportportal.api.model.UserOrgInfo;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.model.IdContainer;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * @author Andrei Piankouski
 */
@Sql("/db/organization/full_organization_samples.sql")
class OrganizationControllerTest extends BaseMvcTest {

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private OrganizationUserRepository organizationUserRepository;

  @Test
  void getOrganizationAdmin() throws Exception {
    mockMvc.perform(get("/organizations/1")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void getOrganizationUser() throws Exception {
    mockMvc.perform(get("/organizations/1")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk());
  }

  @Test
  void getAllOrganizationsAdmin() throws Exception {
    mockMvc.perform(get("/organizations")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void getAllOrganizationsUser() throws Exception {
    mockMvc.perform(get("/organizations")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void getAllOrganizationsByName() throws Exception {
    mockMvc.perform(get("/organizations?name=superadmin")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @ParameterizedTest
  @CsvSource(
      value = {
          "name|EQ|My organization|1",
          "slug|EQ|my-organization|1",
          "slug|EQ|notexists|0",
          "created_at|NE|2024-08-01T12:42:30.758055Z|1",
          "type|EQ|INTERNAL|1",
          "updated_at|BTW|2024-08-01T12:42:30.758055Z,2025-08-01T12:42:30.758055Z|1",
          "projects|EQ|2|1",
          "launches|EQ|0|1",
          "last_launch_occurred|BTW|2024-08-01T12:42:30.758055Z,2025-08-01T12:42:30.758055Z|0"
      },
      delimiter = '|',
      nullValues = "null"
  )
  void searchOrganizationsByParameter(String field, String op, String value, int rows)
      throws Exception {
    SearchCriteriaRQ rq = new SearchCriteriaRQ();

    var searchCriteriaSearchCriteria = new SearchCriteriaSearchCriteriaInner()
        .filterKey(field)
        .operation(FilterOperation.fromValue(op))
        .value(value);
    rq.limit(1)
        .offset(0)
        .sort(field)
        .order(Direction.ASC);
    rq.addSearchCriteriaItem(searchCriteriaSearchCriteria);

    var result = mockMvc.perform(MockMvcRequestBuilders.post("/organizations/searches")
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    var response = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(),
        OrganizationPage.class);

    assertEquals(rows, response.getItems().size());

  }


  @ParameterizedTest
  @CsvSource(
      value = {
          "name|EQ|My organization"
      },
      delimiter = '|',
      nullValues = "null"
  )
  void exportOrganizations(String field, String op, String value)
      throws Exception {
    SearchCriteriaRQ rq = new SearchCriteriaRQ();

    var searchCriteriaSearchCriteria = new SearchCriteriaSearchCriteriaInner()
        .filterKey(field)
        .operation(FilterOperation.fromValue(op))
        .value(value);
    rq.limit(1)
        .offset(0)
        .sort(field)
        .order(Direction.ASC);
    rq.addSearchCriteriaItem(searchCriteriaSearchCriteria);

    var result = mockMvc.perform(MockMvcRequestBuilders.post("/organizations/searches")
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .header(ACCEPT, "text/csv")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    assertTrue(result.contains("My organization"));
    assertTrue(result.contains("INTERNAL"));
  }

  @Test
  void exportAccessDenied() throws Exception {
    SearchCriteriaRQ rq = new SearchCriteriaRQ();

    var searchCriteriaSearchCriteria = new SearchCriteriaSearchCriteriaInner()
        .filterKey("key")
        .operation(FilterOperation.EQ)
        .value("value");
    rq.limit(1).offset(0).sort("name").order(Direction.ASC);
    rq.addSearchCriteriaItem(searchCriteriaSearchCriteria);

    mockMvc.perform(MockMvcRequestBuilders.post("/organizations/searches")
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .header(ACCEPT, "text/csv")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isForbidden());
  }

  @Test
  void getOrganizationByAdminWrongId() throws Exception {
    mockMvc.perform(get("/organizations/notnumber")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void getOrganizationByUserWrongId() throws Exception {
    mockMvc.perform(get("/organizations/notnumber")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void patchWithAllowedRoles() throws Exception {
    var userOrgInfo = new UserOrgInfo(108L, OrgRole.MEMBER);
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.ADD)
        .path("/users/-")
        .value(objectMapper.writeValueAsString(userOrgInfo));

    mockMvc.perform(patch("/organizations/201")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(managerToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().isOk());
  }

  @Test
  void patchWithAccessDenied() throws Exception {
    var userOrgInfo = new UserOrgInfo(108L, OrgRole.MEMBER);
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.ADD)
        .path("/users/-")
        .value(objectMapper.writeValueAsString(userOrgInfo));

    mockMvc.perform(patch("/organizations/201")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(viewerToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().isForbidden());
  }

  @Test
  void patchAddUserToOrganization() throws Exception {
    var userOrgInfo = new UserOrgInfo(108L, OrgRole.MEMBER);
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.ADD)
        .path("/users/-")
        .value(objectMapper.writeValueAsString(userOrgInfo));

    var orgUserBefore = organizationUserRepository.findByUserIdAndOrganization_Id(108L, 201L);
    assertTrue(orgUserBefore.isEmpty());

    mockMvc.perform(patch("/organizations/201")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(adminToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().isOk());

    var orgUserAfter = organizationUserRepository.findByUserIdAndOrganization_Id(108L, 201L);
    assertTrue(orgUserAfter.isPresent());
  }

  @Test
  void patchAddUserAlreadyAssigned() throws Exception {
    var userOrgInfo = new UserOrgInfo(104L, OrgRole.MANAGER);
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.ADD)
        .path("/users/-")
        .value(objectMapper.writeValueAsString(userOrgInfo));

    mockMvc.perform(patch("/organizations/201")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(adminToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().isConflict());
  }

  @Test
  void patchReplaceUsers() throws Exception {
    var users = List.of(
        new UserOrgInfo(108L, OrgRole.MANAGER)
    );
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.REPLACE)
        .path("/users")
        .value(objectMapper.writeValueAsString(users));

    mockMvc.perform(patch("/organizations/201")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(adminToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().isOk());

    var newUser = organizationUserRepository.findByUserIdAndOrganization_Id(108L, 201L);
    assertTrue(newUser.isPresent());
  }

  @Test
  void patchRemoveUserFromOrganization() throws Exception {
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.REMOVE)
        .path("/users")
        .value(objectMapper.writeValueAsString(List.of(new IdContainer(105L))));

    var orgUserBefore = organizationUserRepository.findByUserIdAndOrganization_Id(105L, 201L);
    assertTrue(orgUserBefore.isPresent());

    mockMvc.perform(patch("/organizations/201")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(adminToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().isOk());

    var orgUserAfter = organizationUserRepository.findByUserIdAndOrganization_Id(105L, 201L);
    assertTrue(orgUserAfter.isEmpty());
  }

  @Test
  void patchRemoveAllUsersFromOrganization() throws Exception {
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.REMOVE)
        .path("/users");

    mockMvc.perform(patch("/organizations/201")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(managerToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().isOk());
  }

  @Test
  void patchRemoveNonExistingUser() throws Exception {
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.REMOVE)
        .path("/users")
        .value(objectMapper.writeValueAsString(List.of(new IdContainer(999L))));

    mockMvc.perform(patch("/organizations/201")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(adminToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().isNotFound());
  }

  @Test
  void patchRemoveUsersEmptyArray() throws Exception {
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.REMOVE)
        .path("/users")
        .value("[]");

    mockMvc.perform(patch("/organizations/201")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(managerToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().isOk());
  }

  @Test
  void patchWrongPath() throws Exception {
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.REPLACE)
        .path("wrong_path")
        .value("some-value");

    mockMvc.perform(patch("/organizations/201")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(adminToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().is4xxClientError())
        .andExpect(jsonPath("$.message")
            .value("Incorrect Request. Unexpected path: 'wrong_path'"));
  }

  @Test
  void patchWrongUrl() throws Exception {
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.ADD)
        .path("/users/-")
        .value("{}");

    mockMvc.perform(patch("/organizations/string")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(adminToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void patchOrganizationNotFound() throws Exception {
    var userOrgInfo = new UserOrgInfo(108L, OrgRole.MEMBER);
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.ADD)
        .path("/users/-")
        .value(objectMapper.writeValueAsString(userOrgInfo));

    mockMvc.perform(patch("/organizations/999")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(adminToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().isNotFound());
  }

  @Test
  void patchNoPathUnsupported() throws Exception {
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.ADD)
        .value("some-value");

    mockMvc.perform(patch("/organizations/201")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(adminToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().is4xxClientError());
  }

}
