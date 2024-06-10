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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.ta.reportportal.ws.BaseMvcTest;
import org.junit.jupiter.api.Test;

/**
 * @author Siarhei Hrabko
 */
class OrganizationProjectControllerTest extends BaseMvcTest {

  @Test
  void getOrganizationProjectAdmin() throws Exception {
    mockMvc.perform(get("/organizations/1/projects")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
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

}
