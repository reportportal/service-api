/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.epam.ta.reportportal.ws.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.ta.reportportal.ws.BaseMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql("/db/activity/activity-fill.sql")
class ActivityEventControllerTest extends BaseMvcTest {

  @Test
  void testGetActivities_actionByAdminWithPredefinedFilter() throws Exception {
    int limit = 2;
    int offset = 0;
    String order = "ASC";
    String sort = "id";

    String searchCriteriaJsonString = "{\"search_criterias\":["
        + "{\"filter_key\":\"predefinedFilter\",\"operation\":\"IN\",\"value\":\"create\"}]}";

    mockMvc.perform(post("/v1/activities/searches")
            .param("limit", String.valueOf(limit))
            .param("offset", String.valueOf(offset))
            .param("order", order)
            .param("sort", sort)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken()))
            .content(searchCriteriaJsonString))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.offset").value(0))
        .andExpect(jsonPath("$.limit").value(2))
        .andExpect(jsonPath("$.sort").value("id"))
        .andExpect(jsonPath("$.order").value("ASC"))
        .andExpect(jsonPath("$.total_count").value(3));
  }

  @Test
  void testGetActivities_actionByUserAccessDenied() throws Exception {
    int limit = 2;
    int offset = 0;
    String order = "ASC";
    String sort = "id";

    String searchCriteriaJsonString = "{\"search_criterias\":["
        + "{\"filter_key\":\"eventName\",\"operation\":\"IN\",\"value\":\"updateDashboard\"},"
        + "{\"filter_key\":\"predefinedFilter\",\"operation\":\"IN\",\"value\":\"user\"}]}";

    mockMvc.perform(post("/v1/activities/searches")
            .param("limit", String.valueOf(limit))
            .param("offset", String.valueOf(offset))
            .param("order", order)
            .param("sort", sort)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getDefaultToken()))
            .content(searchCriteriaJsonString))
        .andExpect(status().is(403));
  }

  @Test
  void testGetActivities_actionByAdminWithoutSearchCriteria() throws Exception {
    int limit = 2;
    int offset = 0;
    String order = "ASC";
    String sort = "id";

    String searchCriteriaJsonString = "{\"search_criterias\":[]}";

    mockMvc.perform(post("/v1/activities/searches")
            .param("limit", String.valueOf(limit))
            .param("offset", String.valueOf(offset))
            .param("order", order)
            .param("sort", sort)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken()))
            .content(searchCriteriaJsonString))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.offset").value(0))
        .andExpect(jsonPath("$.limit").value(2))
        .andExpect(jsonPath("$.sort").value("id"))
        .andExpect(jsonPath("$.order").value("ASC"))
        .andExpect(jsonPath("$.total_count").value(7));
  }

  @Test
  void testGetActivities_actionByAdminWithPredefinedFilterAndEventName() throws Exception {
    int limit = 2;
    int offset = 0;
    String order = "ASC";
    String sort = "id";

    String searchCriteriaJsonString = "{\"search_criterias\":["
        + "{\"filter_key\":\"eventName\",\"operation\":\"IN\",\"value\":\"updateDashboard\"},"
        + "{\"filter_key\":\"predefinedFilter\",\"operation\":\"IN\",\"value\":\"user\"}]}";

    mockMvc.perform(post("/v1/activities/searches")
            .param("limit", String.valueOf(limit))
            .param("offset", String.valueOf(offset))
            .param("order", order)
            .param("sort", sort)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken()))
            .content(searchCriteriaJsonString))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.offset").value(0))
        .andExpect(jsonPath("$.limit").value(2))
        .andExpect(jsonPath("$.sort").value("id"))
        .andExpect(jsonPath("$.order").value("ASC"))
        .andExpect(jsonPath("$.total_count").value(1));
  }


}