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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.api.model.LogType;
import com.epam.reportportal.api.model.LogTypeStyle;
import com.epam.reportportal.api.model.LogTypeStyle.TextStyleEnum;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

class GeneratedProjectControllerTest extends BaseMvcTest {

  @Autowired
  private ObjectMapper objectMapper;

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
  void getLogTypesReturns403WhenUserNotAssignedToProject() throws Exception {
    mockMvc.perform(get("/projects/superadmin_personal/log-types")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isForbidden());
  }

  @Test
  void createLogTypePositive() throws Exception {
    LogType request = createLogType("custom log", 9000, false, "#123456", "#ffffff", "#000000",
        TextStyleEnum.BOLD);

    mockMvc.perform(post("/projects/default_personal/log-types")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("custom log"))
        .andExpect(jsonPath("$.level").value(9000))
        .andExpect(jsonPath("$.is_filterable").value(false))
        .andExpect(jsonPath("$.style.label_color").value("#123456"))
        .andExpect(jsonPath("$.style.background_color").value("#ffffff"))
        .andExpect(jsonPath("$.style.text_color").value("#000000"))
        .andExpect(jsonPath("$.style.text_style").value("bold"));
  }

  @Test
  void createLogTypeReturnsConflictForDuplicateName() throws Exception {
    LogType request = createLogType("trace", 7000, false, "#445A47", "#FFFFFF", "#445A47",
        TextStyleEnum.NORMAL);

    mockMvc.perform(post("/projects/default_personal/log-types")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message")
            .value("Resource 'Log type: trace - 7000' already exists. "
                + "You couldn't create the duplicate."));
  }

  @Test
  void createLogTypeReturnsBadRequestForExceedingFilterableLimit() throws Exception {
    LogType request = createLogType("extraLog", 9500, true, "#445A47", "#FFFFFF", "#445A47",
        TextStyleEnum.NORMAL);

    mockMvc.perform(post("/projects/default_personal/log-types")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(
            "Error in handled Request. Please, check specified parameters: 'Cannot create more than 6 filterable log types per project.'"));
  }

  @Test
  void createLogTypeReturnsBadRequestForInvalidInput() throws Exception {
    LogType request = createLogType("", -1, false, null, null, null,
        null);

    mockMvc.perform(post("/projects/default_personal/log-types")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createLogTypeReturns403WhenUserNotAssignedToProject() throws Exception {
    LogType request = createLogType("custom error", 8000, true, "#123456", "#ffffff", "#000000",
        TextStyleEnum.BOLD);

    mockMvc.perform(post("/projects/superadmin_personal/log-types")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isForbidden());
  }

  @Test
  @Sql("/db/log-type/log-type-fill.sql")
  void deleteLogTypePositive() throws Exception {
    mockMvc.perform(delete("/projects/default_personal/log-types/1000")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isNoContent());

    mockMvc.perform(delete("/projects/default_personal/log-types/1000")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(
            "'Log type' not found. Did you use correct ID?"));
  }

  @Test
  @Sql("/db/log-type/log-type-fill.sql")
  void deleteLogTypeAdminCannotDeleteFromAnyProject() throws Exception {
    mockMvc.perform(delete("/projects/default_personal/log-types/1001")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isForbidden());
  }

  @Test
  void deleteLogTypeReturns404ForNonExistentLogType() throws Exception {
    mockMvc.perform(delete("/projects/default_personal/log-types/99999")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(
            "'Log type' not found. Did you use correct ID?"));
  }


  @Test
  void deleteLogTypeReturns400WithNonNumericId() throws Exception {
    mockMvc.perform(delete("/projects/default_personal/log-types/99999nonNumeric")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value(4001));
  }

  @Test
  void deleteLogTypeReturns403ForSystemLogType() throws Exception {
    String result = mockMvc.perform(get("/projects/default_personal/log-types")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    long systemLogTypeId = objectMapper.readTree(result)
        .get("items").elements().next().get("id").asLong();

    mockMvc.perform(delete("/projects/default_personal/log-types/" + systemLogTypeId)
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isForbidden());
  }

  @Test
  void deleteLogTypeReturns403WhenUserNotAssignedToProject() throws Exception {
    mockMvc.perform(delete("/projects/superadmin_personal/log-types/1001")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value(
            "You do not have enough permissions."));
  }

  private LogType createLogType(String name, int level, boolean isFilterable,
      String labelColor, String backgroundColor, String textColor, TextStyleEnum textStyle) {
    LogTypeStyle style = new LogTypeStyle();
    style.setLabelColor(labelColor);
    style.setBackgroundColor(backgroundColor);
    style.setTextColor(textColor);
    style.setTextStyle(textStyle);

    LogType logType = new LogType();
    logType.setName(name);
    logType.setLevel(level);
    logType.setIsFilterable(isFilterable);
    logType.setStyle(style);

    return logType;
  }
}
