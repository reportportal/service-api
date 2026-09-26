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

package com.epam.reportportal.base.core.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.model.integration.IntegrationRQ;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

/**
 * "github-actions" and "gitlab-ci" are built-in integration types without a plugin; creating them through the
 * API used to fail with "Plugin for ... isn't installed", which left "Push to agent" without a way to be set up.
 */
class CiTriggerIntegrationCreationTest extends BaseMvcTest {

  private static final String PROJECT = "default_personal";

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void githubActions_ShouldBeCreatedWithItsParametersAndListedAsEnabled() throws Exception {
    create("github-actions", "github_actions", Map.of("token", "ghp_test", "repo", "org/agent", "ref", "main",
        "fixWorkflowRef", "fix-test-case.yml")).andExpect(status().isCreated());

    mockMvc.perform(get("/v1/integration/project/{project}/all/github-actions", PROJECT)
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].enabled").value(true))
        .andExpect(jsonPath("$[0].integrationParameters.repo").value("org/agent"))
        .andExpect(jsonPath("$[0].integrationParameters.fixWorkflowRef").value("fix-test-case.yml"));
  }

  @Test
  void gitlabCi_ShouldBeCreated() throws Exception {
    create("gitlab-ci", "gitlab_ci", Map.of("url", "https://git.epam.com", "projectId", "EPM-RPP/rp-tests",
        "triggerToken", "glptt_test")).andExpect(status().isCreated());
  }

  @Test
  void githubActionsWithoutRepo_ShouldBeRejectedAtCreation() throws Exception {
    create("github-actions", "github_actions", Map.of("token", "ghp_test"))
        .andExpect(status().isBadRequest());
  }

  private org.springframework.test.web.servlet.ResultActions create(String type, String name,
      Map<String, Object> params) throws Exception {
    var request = new IntegrationRQ();
    request.setName(name);
    request.setEnabled(true);
    request.setIntegrationParams(new HashMap<>(params));
    return mockMvc.perform(post("/v1/integration/{project}/{type}", PROJECT, type)
        .with(token(oAuthHelper.getDefaultToken()))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(request)));
  }
}
