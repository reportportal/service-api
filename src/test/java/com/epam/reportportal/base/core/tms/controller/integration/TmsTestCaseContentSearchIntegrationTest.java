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

package com.epam.reportportal.base.core.tms.controller.integration;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.ws.BaseMvcTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Search of test cases by precondition, instructions and expected result (EPMRPP-123497).
 *
 * <p>Fixture: TC4 (folder 4) has a text scenario "Execute test case 4 functionality" / "Test
 * should pass with high priority" and the precondition "System must be ready". TC18 (folder 6)
 * has a step-based default version (steps added here) and a non-default text version "Execute
 * draft version functionality" with the precondition "Draft version setup".
 */
@Sql("/db/tms/tms-test-case/tms-test-case-fill.sql")
class TmsTestCaseContentSearchIntegrationTest extends BaseMvcTest {

  private static final String TEST_CASES_URL = "/v1/project/superadmin_personal/tms/test-case";
  private static final String FOLDERS_URL = "/v1/project/superadmin_personal/tms/folder";

  @PersistenceContext
  private EntityManager entityManager;

  @BeforeEach
  void addStepsToDefaultVersionOfTestCase18() {
    entityManager.createNativeQuery(
            "INSERT INTO tms_steps_manual_scenario (manual_scenario_id) VALUES (15)"
                + " ON CONFLICT DO NOTHING")
        .executeUpdate();
    insertStep(9001, "Open the Login page", "Login form is shown", 0);
    insertStep(9002, "Enter the password", "Dashboard opens", 1);
  }

  private void insertStep(long id, String instructions, String expectedResult, int number) {
    entityManager.createNativeQuery(
            "INSERT INTO tms_step (id, instructions, expected_result, number,"
                + " steps_manual_scenario_id) VALUES (:id, :instructions, :expected, :number, 15)")
        .setParameter("id", id)
        .setParameter("instructions", instructions)
        .setParameter("expected", expectedResult)
        .setParameter("number", number)
        .executeUpdate();
  }

  private MockHttpServletRequestBuilder testCases() {
    return get(TEST_CASES_URL).with(token(oAuthHelper.getSuperadminToken()));
  }

  private MockHttpServletRequestBuilder folders() {
    return get(FOLDERS_URL).with(token(oAuthHelper.getSuperadminToken()));
  }

  @Test
  void instructions_MatchAnyStepOfStepBasedScenario_IgnoringCase() throws Exception {
    mockMvc.perform(testCases().param("filter.cnt.instructions", "enter the PASSWORD"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].id", contains(18)));
  }

  @Test
  void instructions_MatchTextScenario() throws Exception {
    mockMvc.perform(testCases().param("filter.cnt.instructions", "test case 4 functionality"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].id", contains(4)));
  }

  @Test
  void expectedResult_MatchStepsAndText_AndIsNotSearchedInInstructions() throws Exception {
    mockMvc.perform(testCases().param("filter.cnt.expectedResult", "DASHBOARD opens"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].id", contains(18)));
    mockMvc.perform(testCases().param("filter.cnt.expectedResult", "pass with high priority"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].id", contains(4)));
    mockMvc.perform(testCases().param("filter.cnt.instructions", "Dashboard opens"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", empty()));
  }

  @Test
  void precondition_MatchDefaultVersion() throws Exception {
    mockMvc.perform(testCases().param("filter.cnt.precondition", "SYSTEM MUST be ready"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].id", contains(4)));
    mockMvc.perform(testCases().param("filter.cnt.precondition", "complex scenario SETUP"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].id", contains(18)));
  }

  @Test
  void nonDefaultVersion_IsNotSearched() throws Exception {
    mockMvc.perform(testCases().param("filter.cnt.instructions", "draft version functionality"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", empty()));
    mockMvc.perform(testCases().param("filter.cnt.precondition", "Draft version setup"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", empty()));
  }

  @Test
  void severalCriteria_CombinedWithAnd() throws Exception {
    mockMvc.perform(testCases()
            .param("filter.cnt.instructions", "open the login page")
            .param("filter.cnt.expectedResult", "login form")
            .param("filter.eq.testFolderId", "6"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].id", contains(18)));
    mockMvc.perform(testCases()
            .param("filter.cnt.instructions", "open the login page")
            .param("filter.cnt.expectedResult", "high priority"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", empty()));
  }

  @Test
  void repeatedName_AllValuesMustMatch() throws Exception {
    mockMvc.perform(testCases().param("filter.cnt.name", "Test Case", "complex", " "))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].id", contains(18)));
    mockMvc.perform(testCases().param("filter.cnt.name", "Complex", "no such name"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", empty()));
  }

  @Test
  void wildcardIsLiteral() throws Exception {
    mockMvc.perform(testCases().param("filter.cnt.instructions", "%"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", empty()));
  }

  @Test
  void onlyContainsAllowed() throws Exception {
    mockMvc.perform(testCases().param("filter.eq.instructions", "Enter the password"))
        .andExpect(status().isBadRequest());
    mockMvc.perform(folders().param("filter.eq.testCaseExpectedResult", "Dashboard opens"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void folders_CountOnlyMatchingTestCases() throws Exception {
    mockMvc.perform(folders().param("filter.cnt.testCaseInstructions", "enter the password"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[?(@.id == 6)].countOfTestCases", contains(1)))
        .andExpect(jsonPath("$.content[?(@.id == 4)]", empty()));

    mockMvc.perform(folders()
            .param("filter.cnt.testCaseExpectedResult", "high priority")
            .param("filter.cnt.testCasePrecondition", "system must be ready")
            .param("filter.cnt.testCaseDescription", "test case 4"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[?(@.id == 4)].countOfTestCases", contains(1)))
        .andExpect(jsonPath("$.content[?(@.id == 6)]", empty()));

    mockMvc.perform(folders().param("filter.cnt.testCaseName", "Test Case", "complex"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[?(@.id == 6)].countOfTestCases", contains(1)))
        .andExpect(jsonPath("$.content[?(@.id == 4)]", empty()));
  }
}
