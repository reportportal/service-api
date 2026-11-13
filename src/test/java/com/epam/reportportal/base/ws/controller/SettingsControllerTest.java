/*
 * Copyright 2019 EPAM Systems
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

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.api.model.AnalyticsSettingsRequest;
import com.epam.reportportal.api.model.ServerSettingKey;
import com.epam.reportportal.api.model.UpdateServerSettingsRequest;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class SettingsControllerTest extends BaseMvcTest {

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void getServerSettings() throws Exception {
    mockMvc.perform(get("/v1/settings").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void updateSettingsNegative() throws Exception {
    String json = "{\"key\":\"server.nonexistent.setting\",\"value\":\"true\"}";
    mockMvc.perform(put("/v1/settings").with(token(oAuthHelper.getSuperadminToken()))
            .contentType(APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest());
  }

  @Test
  void updateSettings() throws Exception {
    UpdateServerSettingsRequest mockRequest = new UpdateServerSettingsRequest();
    mockRequest.setKey(ServerSettingKey.fromValue("server.users.sso"));
    mockRequest.setValue("true");

    mockMvc.perform(put("/v1/settings").with(token(oAuthHelper.getSuperadminToken()))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(mockRequest)))
        .andExpect(status().isOk());
  }

  @Test
  void updateAnalyticsSettings() throws Exception {
    AnalyticsSettingsRequest resource = new AnalyticsSettingsRequest();
    resource.setType("server.analytics.all");
    resource.setEnabled(true);
    mockMvc.perform(put("/v1/settings/analytics").with(token(oAuthHelper.getSuperadminToken()))
            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(resource)))
        .andExpect(status().isOk());
  }

  @Test
  void updateAnalyticsSettingsViaPostOnNewApi() throws Exception {
    AnalyticsSettingsRequest resource = new AnalyticsSettingsRequest();
    resource.setType("all");
    resource.setEnabled(true);
    mockMvc.perform(post("/settings/analytics").with(token(oAuthHelper.getSuperadminToken()))
            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(resource)))
        .andExpect(status().isOk());
  }

  @Test
  void saveAnalyticsSettingsNegative() throws Exception {
    AnalyticsSettingsRequest resource = new AnalyticsSettingsRequest();
    resource.setEnabled(true);
    resource.setType("");
    mockMvc.perform(put("/v1/settings/analytics").with(token(oAuthHelper.getSuperadminToken()))
            .contentType(APPLICATION_JSON).content(objectMapper.writeValueAsBytes(resource)))
        .andExpect(status().isBadRequest());
  }
}
