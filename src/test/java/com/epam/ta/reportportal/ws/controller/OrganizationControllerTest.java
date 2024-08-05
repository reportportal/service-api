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

import com.epam.reportportal.api.model.OffsetRequest.OrderEnum;
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
          "name|EQ|def",
          "slug|EQ|qwe",
          "created_at|GT|qwe",
          "type|EQ|internal",
          "updated_at|GT|123",
          "projects|EQ|123",
          "launches|EQ|123",
          "last_launch_occurred|LT|123"
      },
      delimiter = '|',
      nullValues = "null"
  )
  void searchOrganizationsByParameter(String field, String op, String value) throws Exception {
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

  }

}
