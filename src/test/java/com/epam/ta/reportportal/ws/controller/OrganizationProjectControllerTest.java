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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.api.model.FilterOperation;
import com.epam.reportportal.api.model.OrganizationProjectsPage;
import com.epam.reportportal.api.model.SearchCriteriaRQ;
import com.epam.reportportal.api.model.SearchCriteriaSearchCriteriaInner;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * @author Siarhei Hrabko
 */
class OrganizationProjectControllerTest extends BaseMvcTest {

  private static final String ORG_SLUG = "my-organization";

  @Autowired
  private ObjectMapper objectMapper;

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

}
