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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.ta.reportportal.ws.BaseMvcTest;
import org.junit.jupiter.api.Test;

/**
 * @author Andrei Piankouski
 */
public class OrganizationControllerTest extends BaseMvcTest {

  @Test
  void getOrganization() throws Exception {
    mockMvc.perform(get("/organization/1").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void getAllOrganizations() throws Exception {
    mockMvc.perform(get("/organization/list").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }
}