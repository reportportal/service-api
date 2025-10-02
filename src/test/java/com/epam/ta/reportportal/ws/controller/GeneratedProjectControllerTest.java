/*
 * Copyright 2025 EPAM Systems
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.ta.reportportal.ws.BaseMvcTest;
import org.junit.jupiter.api.Test;

class GeneratedProjectControllerTest extends BaseMvcTest {

  @Test
  void getLogTypesPositive() throws Exception {
    mockMvc.perform(get("/projects/default_personal/log-types")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(7)));
  }

  @Test
  void getLogTypesNormalizesProjectName() throws Exception {
    mockMvc.perform(get("/projects/Default_Personal/log-types")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(7)));
  }

  @Test
  void getLogTypesReturns404ForUnknownProject() throws Exception {
    mockMvc.perform(get("/projects/unknown_project/log-types")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void getLogTypesReturns403ForCustomerRole() throws Exception {
    mockMvc.perform(get("/projects/superadmin_personal/log-types")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isForbidden());
  }
}
