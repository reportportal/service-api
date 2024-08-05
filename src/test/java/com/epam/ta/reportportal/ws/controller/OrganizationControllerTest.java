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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.api.model.OffsetRequest.OrderEnum;
import com.epam.reportportal.api.model.OrganizationProfilesPage;
import com.epam.reportportal.api.model.SearchCriteriaRQ;
import com.epam.reportportal.api.model.SearchCriteriaSearchCriteria;
import com.epam.reportportal.api.model.SearchCriteriaSearchCriteria.OperationEnum;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * @author Andrei Piankouski
 */
class OrganizationControllerTest extends BaseMvcTest {

  @Autowired
  private ObjectMapper objectMapper;

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

    var searchCriteriaSearchCriteria = new SearchCriteriaSearchCriteria()
        .filterKey(field)
        .operation(OperationEnum.fromValue(op))
        .value(value);
    rq.limit(1)
        .offset(0)
        .sort(field)
        .order(OrderEnum.ASC);
    rq.addSearchCriteriaItem(searchCriteriaSearchCriteria);

    var result = mockMvc.perform(MockMvcRequestBuilders.post("/organizations/searches")
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    var response = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(),
        OrganizationProfilesPage.class);

    assertEquals(rows, response.getItems().size());

  }

}
