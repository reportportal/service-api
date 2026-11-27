/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.core.tms.controller.unit;

import static com.epam.reportportal.base.util.StandaloneMockMvcSupport.standaloneJsonSetup;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.core.tms.controller.TestCaseController;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseService;
import com.epam.reportportal.base.exception.handler.GlobalExceptionHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.util.ProjectExtractor;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * The create/update test case endpoints carry a custom constraint annotation on the body parameter, which makes Spring
 * use built-in method validation: every body violation is then raised as a HandlerMethodValidationException. It must be
 * answered with 400 and the field messages, not 500.
 */
class TmsTestCaseControllerValidationTest {

  private final TmsTestCaseService tmsTestCaseService = mock(TmsTestCaseService.class);
  private final ProjectExtractor projectExtractor = mock(ProjectExtractor.class);

  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    var user = ReportPortalUser.userBuilder()
        .withUserName("testUser")
        .withPassword("password")
        .withUserId(1L)
        .withActive(true)
        .withAuthorities(Collections.emptyList())
        .build();
    mockMvc = standaloneJsonSetup(new TestCaseController(tmsTestCaseService, projectExtractor))
        .setControllerAdvice(new GlobalExceptionHandler())
        .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
          @Override
          public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterAnnotation(AuthenticationPrincipal.class) != null;
          }

          @Override
          public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
              NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return user;
          }
        })
        .build();
  }

  @Test
  void createTestCaseWithInvalidNestedFieldReturns400WithFieldMessage() throws Exception {
    var body = """
        {"name": "tc", "testFolderId": 1,
         "manualScenario": {"manualScenarioType": "TEXT", "executionEstimationTime": 0}}
        """;

    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-case", "project")
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value(4001))
        .andExpect(jsonPath("$.message").value(
            containsString("[Field 'manualScenario.executionEstimationTime' should be greater or equal than '1'.]")));
    verifyNoInteractions(tmsTestCaseService);
  }

  @Test
  void createTestCaseWithoutFolderReturns400WithConstraintMessage() throws Exception {
    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-case", "project")
            .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"tc\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value(4001))
        .andExpect(jsonPath("$.message").value(containsString(
            "[Either testFolderId or testFolderName must be provided and not empty]")));
    verifyNoInteractions(tmsTestCaseService);
  }

  @Test
  void updateTestCaseWithInvalidNestedFieldReturns400() throws Exception {
    var body = """
        {"name": "tc", "testFolderId": 1,
         "manualScenario": {"manualScenarioType": "TEXT", "executionEstimationTime": 0}}
        """;

    mockMvc.perform(put("/v1/project/{projectKey}/tms/test-case/{id}", "project", 1)
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(
            containsString("[Field 'manualScenario.executionEstimationTime' should be greater or equal than '1'.]")));
  }
}
