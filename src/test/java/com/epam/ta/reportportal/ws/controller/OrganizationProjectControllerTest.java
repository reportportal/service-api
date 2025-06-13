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
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.api.model.FilterOperation;
import com.epam.reportportal.api.model.OperationType;
import com.epam.reportportal.api.model.OrganizationProjectsPage;
import com.epam.reportportal.api.model.PatchOperation;
import com.epam.reportportal.api.model.SearchCriteriaRQ;
import com.epam.reportportal.api.model.SearchCriteriaSearchCriteriaInner;
import com.epam.ta.reportportal.core.project.ProjectService;
import com.epam.ta.reportportal.util.SlugUtils;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import java.util.Collections;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * @author Siarhei Hrabko
 */
@Sql("/db/organization/full_organization_samples.sql")
class OrganizationProjectControllerTest extends BaseMvcTest {

  private static final String ORG_SLUG = "my-organization";

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  ProjectService projectService;

  @Test
  void getOrganizationProjectAdmin() throws Exception {
    mockMvc.perform(get("/organizations/1/projects")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }


  @ParameterizedTest
  @CsvSource(
      value = {
          "name|EQ|superadmin_personal|1",
          "slug|EQ|superadmin-personal|1",
          "key|EQ|superadmin_personal|1",
          "created_at|NE|2024-08-14T06:01:25.329026Z|2",
          "updated_at|NE|2024-08-14T06:01:25.329026Z|2",
          "users|EQ|1|2",
          "users|GT|0|2",
          "users|GTE|0|2",
          "users|LT|3|2",
          "users|LTE|2|2",
          "launches|EQ|0|2",
          "last_launch_occurred|EQ|2024-08-14T06:01:25.329026Z|0"
      },
      delimiter = '|',
      nullValues = "null"
  )
  void getOrganizationProjectsByFilter(String field, String op, String value, int rows)
      throws Exception {

    SearchCriteriaRQ rq = new SearchCriteriaRQ();

    var searchCriteriaSearchCriteria = new SearchCriteriaSearchCriteriaInner()
        .filterKey(field)
        .operation(FilterOperation.fromValue(op))
        .value(value);
    rq.limit(100)
        .offset(0)
        .sort(field)
        .order(Direction.ASC);
    rq.addSearchCriteriaItem(searchCriteriaSearchCriteria);

    var result = mockMvc.perform(MockMvcRequestBuilders.post("/organizations/1/projects/searches")
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse().getContentAsString();

    var orgProjectsPage = objectMapper.readValue(result, OrganizationProjectsPage.class);

    assertEquals(rows, orgProjectsPage.getItems().size());
  }

  @Test
  void getOrganizationProjectUser() throws Exception {
    mockMvc.perform(get("/organizations/1/projects")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk());
  }


  @Test
  void getOrganizationProjectUserWithParams() throws Exception {
    mockMvc.perform(get("/organizations/1/projects?order=ASC&offset=0&limit=1")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.offset").value(0))
        .andExpect(jsonPath("$.limit").value(1));
  }

  @ParameterizedTest
  @CsvSource(
      value = {
          "Proj Name|proj-name|proj-name",
          "Proj Name|null|proj-name"
      },
      delimiter = '|',
      nullValues = "null"
  )
  void createProject(String name, String slug, String expectSlug) throws Exception {
    JsonObject jsonBody = new JsonObject();
    jsonBody.addProperty("name", name);
    if (StringUtils.isNotEmpty(slug)) {
      jsonBody.addProperty("slug", slug);
    }

    mockMvc.perform(post("/organizations/1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken()))
            .content(jsonBody.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value(name))
        .andExpect(jsonPath("$.key").value(ORG_SLUG + "." + expectSlug))
        .andExpect(jsonPath("$.slug").value(expectSlug));
  }


  @Test
  void createDuplicateProjectFails() throws Exception {
    JsonObject jsonBody = new JsonObject();
    String name = "uniq_name_123";
    jsonBody.addProperty("name", name);

    mockMvc.perform(post("/organizations/1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken()))
            .content(jsonBody.toString()))
        .andExpect(status().isOk());

    mockMvc.perform(post("/organizations/1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken()))
            .content(jsonBody.toString()))
        .andExpect(status().is4xxClientError());

    jsonBody.addProperty("name", name.toUpperCase());
    mockMvc.perform(post("/organizations/1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken()))
            .content(jsonBody.toString()))
        .andExpect(status().is4xxClientError());
  }



  @Test
  void patchWithAllowedRoles() throws Exception {
    var projectId = 301L;
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.REPLACE)
        .path("name")
        .value("correct-value");

    mockMvc.perform(patch("/organizations/201/projects/" + projectId)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(managerToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().isOk());

    mockMvc.perform(patch("/organizations/201/projects/" + projectId)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(editorToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().isOk());

    var project = projectService.findProjectById(projectId);
    assertEquals("correct-value", project.getName());
  }

  @Test
  void patchWithAccessDenied() throws Exception {
    var projectId = 301L;
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.REPLACE)
        .path("name")
        .value("correct-value");

    mockMvc.perform(patch("/organizations/201/projects/" + projectId)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(viewerToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().isForbidden());
  }

  @Test
  void patchProjectName() throws Exception {
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.REPLACE)
        .path("name")
        .value("correct-value");

    mockMvc.perform(patch("/organizations/1/projects/1")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(adminToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().isOk());

    var project = projectService.findProjectById(1L);
    assertEquals("correct-value", project.getName());
  }

  @ParameterizedTest
  @CsvSource(
      value = {
          "raw-slug-value",
          "RawSlugValue",
          "raw---slug---value",
          "raw_slug__val.ue",
          "r!aw_slug__va#l$.ue"
      },
      delimiter = '|'
  )
  void patchProjectSlug(String rawSlug) throws Exception {
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.REPLACE)
        .path("slug")
        .value(rawSlug);

    mockMvc.perform(patch("/organizations/1/projects/1")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(adminToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().isOk());

    var project = projectService.findProjectById(1L);
    assertEquals(SlugUtils.slug(rawSlug), project.getSlug());
  }

  @Test
  void patchWrongPath() throws Exception {
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.REPLACE)
        .path("wrong_path")
        .value("correct-value");

    mockMvc.perform(patch("/organizations/1/projects/1")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(adminToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().is4xxClientError())
        .andExpect(jsonPath("$.message")
            .value("Incorrect Request. Unexpected path: wrong_path"));
  }


  @ParameterizedTest
  @CsvSource(
      value = {
          "name",
          "slug"
      },
      delimiter = '|'
  )
  void patchValueTooLong(String path) throws Exception {
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.REPLACE)
        .path(path)
        .value(RandomStringUtils.insecure().nextAlphabetic(1000));

    mockMvc.perform(patch("/organizations/1/projects/1")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(adminToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().is4xxClientError());
  }

  @ParameterizedTest
  @CsvSource(
      value = {
          "1nv@lid-name",
          "!nvalid-#ame"
      },
      delimiter = '|'
  )
  void patchInvalidName(String name) throws Exception {
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.REPLACE)
        .path("name")
        .value(name);

    mockMvc.perform(patch("/organizations/1/projects/1")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(adminToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().is4xxClientError());
  }


  @ParameterizedTest
  @CsvSource(
      value = {
          "add|name",
          "add|slug",
          "remove|name",
          "remove|slug"
      },
      delimiter = '|'
  )
  void patchUnsupportedOperation(String operation, String name) throws Exception {
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.fromValue(operation))
        .path(name)
        .value("validValue");

    mockMvc.perform(patch("/organizations/1/projects/1")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(adminToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void patchWrongUrl() throws Exception {
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.REPLACE)
        .path("name")
        .value("validValue");

    mockMvc.perform(patch("/organizations/1/projects/string")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(adminToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void patchExistingNameSameOrg() throws Exception {
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.REPLACE)
        .path("name")
        .value("superadmin_personal");

    mockMvc.perform(patch("/organizations/1/projects/2")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(adminToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().isConflict());
  }

  @Test
  void patchTheSameName() throws Exception {
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.REPLACE)
        .path("name")
        .value("superadmin_personal");

    mockMvc.perform(patch("/organizations/1/projects/1")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(adminToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().isOk());
  }

  @Test
  void patchExistingSlugSameOrg() throws Exception {
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.REPLACE)
        .path("slug")
        .value("superadmin_personal");

    mockMvc.perform(patch("/organizations/1/projects/2")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(adminToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().isConflict());
  }

  @Test
  void patchRegenerateSlug() throws Exception {
    PatchOperation patchOperation = new PatchOperation()
        .op(OperationType.REPLACE)
        .path("slug")
        .value("newslug");

    mockMvc.perform(patch("/organizations/1/projects/2")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(adminToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().isOk());
    patchOperation.setValue(null);
    assertEquals("newslug", projectService.findProjectById(2L).getSlug());

    mockMvc.perform(patch("/organizations/1/projects/2")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(adminToken))
            .content(objectMapper.writeValueAsString(Collections.singletonList(patchOperation))))
        .andExpect(status().isOk());
    assertEquals("default-personal", projectService.findProjectById(2L).getSlug());

  }

}
