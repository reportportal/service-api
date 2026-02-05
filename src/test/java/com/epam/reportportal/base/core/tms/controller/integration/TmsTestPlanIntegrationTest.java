package com.epam.reportportal.base.core.tms.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.core.tms.dto.DuplicateTmsTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestPlanAttributeRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestPlanRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchAddTestCasesToPlanRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchRemoveTestCasesFromPlanRQ;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseAttributeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestPlanRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestPlanTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Integration tests for TMS Test Plan Controller. Tests cover CRUD operations, batch operations, and test plan
 * duplication functionality.
 *
 * @author <a href="mailto:konstantin_shaplyko@epam.com">Konstantin Shaplyko</a>
 */
@Sql("/db/tms/tms-test-plan/tms-test-plan-fill.sql")
@ExtendWith(MockitoExtension.class)
@Disabled
public class TmsTestPlanIntegrationTest extends BaseMvcTest {

  private static final String SUPERADMIN_PROJECT_KEY = "superadmin_personal";
  private static final Long TEST_PLAN_WITH_TEST_CASES_ID = 200L;
  private static final Long TEST_PLAN_EMPTY_FOR_RETRIEVAL_ID = 201L;
  private static final Long TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID = 200L;
  private static final Long TEST_CASE_WITH_TWO_EXECUTIONS_ID = 201L;
  private static final Long TEST_CASE_WITH_ONE_EXECUTION_ID = 202L;
  private static final Long TEST_CASE_WITHOUT_EXECUTIONS_ID = 203L;
  private static final Long TEST_CASE_NOT_IN_PLAN_ID = 204L;
  private static final Long NON_EXISTENT_TEST_PLAN_ID = 99999L;
  private static final Long NON_EXISTENT_TEST_CASE_ID = 99999L;
  private final ObjectMapper objectMapper = new ObjectMapper();
  @PersistenceContext
  private EntityManager entityManager;
  @Autowired
  private TmsTestCaseAttributeRepository tmsTestCaseAttributeRepository;
  @Autowired
  private TmsTestCaseRepository testCaseRepository;
  @Autowired
  private TmsTestPlanTestCaseRepository tmsTestPlanTestCaseRepository;
  @Autowired
  private TmsTestPlanRepository testPlanRepository;

  @Test
  void createTestPlanIntegrationTest() throws Exception {
    TmsTestPlanAttributeRQ attribute = new TmsTestPlanAttributeRQ();
    attribute.setValue("value3");
    attribute.setId(3L);

    TmsTestPlanRQ tmsTestPlan = new TmsTestPlanRQ();
    tmsTestPlan.setName("name3");
    tmsTestPlan.setDescription("description3");
    tmsTestPlan.setAttributes(List.of(attribute));

    String jsonContent = objectMapper.writeValueAsString(tmsTestPlan);

    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    Optional<TmsTestPlan> testPlan = testPlanRepository
        .findAll()
        .stream()
        .filter(plan -> plan.getName().equals(tmsTestPlan.getName()))
        .findFirst();

    assertTrue(testPlan.isPresent());
    assertEquals(tmsTestPlan.getName(), testPlan.get().getName());
    assertEquals(tmsTestPlan.getDescription(), testPlan.get().getDescription());
  }

  @Test
  void getTestPlansByCriteriaIntegrationTest() throws Exception {
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan")
            .param("offset", "0")
            .param("limit", "10")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(10));
  }

  @Test
  void getTestPlansByCriteriaWithSearchIntegrationTest() throws Exception {
    // Test search by name
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan")
            .param("search", "name1")
            .param("page", "0")
            .param("size", "10")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").isNumber());
  }

  @Test
  void getTestPlansByCriteriaWithSearchByDescriptionIntegrationTest() throws Exception {
    // Test search by description
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan")
            .param("search", "description")
            .param("page", "0")
            .param("size", "10")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").isNumber());
  }

  @Test
  void getTestPlansByCriteriaWithEmptySearchIntegrationTest() throws Exception {
    // Test with empty search parameter
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan")
            .param("filter.fts.search", "")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getTestPlansByCriteriaWithNonExistentSearchIntegrationTest() throws Exception {
    // Test search with non-existent term
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan")
            .param("filter.fts.search", "nonexistent_search_term")
            .param("offset", "0")
            .param("limit", "10")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0));
  }

  @Test
  void getTestPlansByCriteriaWithoutSearchParameterIntegrationTest() throws Exception {
    // Test without search parameter (should return all test plans)
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan")
            .param("offset", "0")
            .param("limit", "10")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(10));
  }

  @Test
  void updateTestPlanIntegrationTest() throws Exception {
    TmsTestPlanAttributeRQ attribute = new TmsTestPlanAttributeRQ();
    attribute.setValue("value5");
    attribute.setId(5L);

    TmsTestPlanRQ tmsTestPlan = new TmsTestPlanRQ();
    tmsTestPlan.setName("updated_name5");
    tmsTestPlan.setDescription("updated_name5");
    tmsTestPlan.setAttributes(List.of(attribute));

    String jsonContent = objectMapper.writeValueAsString(tmsTestPlan);

    mockMvc.perform(put("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/5")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    Optional<TmsTestPlan> testPlan = testPlanRepository.findById(5L);

    assertTrue(testPlan.isPresent());
    assertEquals(tmsTestPlan.getName(), testPlan.get().getName());
    assertEquals(tmsTestPlan.getDescription(), testPlan.get().getDescription());
  }

  @Test
  void getTestPlanByIdIntegrationTest() throws Exception {
    Optional<TmsTestPlan> testPlan = testPlanRepository.findById(5L);

    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/5")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(testPlan.get().getId()))
        .andExpect(jsonPath("$.name").value(testPlan.get().getName()))
        .andExpect(jsonPath("$.description").value(testPlan.get().getDescription()));
  }

  @Test
  void deleteTestPlanIntegrationTest() throws Exception {

    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/6")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    assertFalse(testPlanRepository.findById(6L).isPresent());
  }

  @Test
  void patchTestPlanTest() throws Exception {
    TmsTestPlanAttributeRQ attributQ = new TmsTestPlanAttributeRQ();
    attributQ.setValue("value6");
    attributQ.setId(6L);

    TmsTestPlanRQ tmsTestPlan = new TmsTestPlanRQ();
    tmsTestPlan.setName("updated_name5");
    tmsTestPlan.setDescription("updated_name5");
    tmsTestPlan.setAttributes(List.of(attributQ));

    String jsonContent = objectMapper.writeValueAsString(tmsTestPlan);

    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/5")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    Optional<TmsTestPlan> testPlan = testPlanRepository.findById(5L);

    assertTrue(testPlan.isPresent());
    assertEquals(tmsTestPlan.getName(), testPlan.get().getName());
    assertEquals(tmsTestPlan.getDescription(), testPlan.get().getDescription());
  }

  @Test
  void addTestCasesToPlanIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = Arrays.asList(7L, 8L, 9L);
    BatchAddTestCasesToPlanRQ addRequest = BatchAddTestCasesToPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();
    String jsonContent = objectMapper.writeValueAsString(addRequest);

    // When & Then
    mockMvc
        .perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/4/test-case/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").exists())
        .andExpect(jsonPath("$.successCount").exists())
        .andExpect(jsonPath("$.failureCount").exists())
        .andExpect(jsonPath("$.errors").isArray());
  }

  @Test
  void addSingleTestCaseToPlanIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = List.of(10L);
    BatchAddTestCasesToPlanRQ addRequest = BatchAddTestCasesToPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();
    String jsonContent = objectMapper.writeValueAsString(addRequest);

    // When & Then
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/5/test-case/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").value(1))
        .andExpect(jsonPath("$.successCount").exists())
        .andExpect(jsonPath("$.failureCount").exists())
        .andExpect(jsonPath("$.errors").isArray());
  }

  @Test
  void addEmptyTestCaseListToPlanIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = Collections.emptyList();
    BatchAddTestCasesToPlanRQ addRequest = BatchAddTestCasesToPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();
    String jsonContent = objectMapper.writeValueAsString(addRequest);

    // When & Then
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/4/test-case/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void addTestCasesToPlanWithExpectedResultsIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = Arrays.asList(14L, 15L);
    BatchAddTestCasesToPlanRQ addRequest = BatchAddTestCasesToPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();
    String jsonContent = objectMapper.writeValueAsString(addRequest);

    // When & Then
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/5/test-case/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").value(2))
        .andExpect(jsonPath("$.successCount").exists())
        .andExpect(jsonPath("$.failureCount").exists())
        .andExpect(jsonPath("$.errors").isArray());
  }

  @Test
  void removeTestCasesFromPlanIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = Arrays.asList(11L, 12L);
    BatchRemoveTestCasesFromPlanRQ removeRequest = BatchRemoveTestCasesFromPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();
    String jsonContent = objectMapper.writeValueAsString(removeRequest);

    // When & Then
    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/5/test-case/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").exists())
        .andExpect(jsonPath("$.successCount").exists())
        .andExpect(jsonPath("$.failureCount").exists())
        .andExpect(jsonPath("$.errors").isArray());
  }

  @Test
  void removeSingleTestCaseFromPlanIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = List.of(13L);
    BatchRemoveTestCasesFromPlanRQ removeRequest = BatchRemoveTestCasesFromPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();
    String jsonContent = objectMapper.writeValueAsString(removeRequest);

    // When & Then
    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/4/test-case/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").value(1))
        .andExpect(jsonPath("$.successCount").exists())
        .andExpect(jsonPath("$.failureCount").exists())
        .andExpect(jsonPath("$.errors").isArray());
  }

  @Test
  void removeEmptyTestCaseListFromPlanIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = Collections.emptyList();
    BatchRemoveTestCasesFromPlanRQ removeRequest = BatchRemoveTestCasesFromPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();
    String jsonContent = objectMapper.writeValueAsString(removeRequest);

    // When & Then
    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/5/test-case/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void addMultipleTestCasesToPlanIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = Arrays.asList(16L, 17L, 18L, 19L, 20L);
    BatchAddTestCasesToPlanRQ addRequest = BatchAddTestCasesToPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();
    String jsonContent = objectMapper.writeValueAsString(addRequest);

    // When & Then
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/6/test-case/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").value(5))
        .andExpect(jsonPath("$.successCount").exists())
        .andExpect(jsonPath("$.failureCount").exists())
        .andExpect(jsonPath("$.errors").isArray());
  }

  @Test
  void removeMultipleTestCasesFromPlanIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = Arrays.asList(19L, 20L, 21L);
    BatchRemoveTestCasesFromPlanRQ removeRequest = BatchRemoveTestCasesFromPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();
    String jsonContent = objectMapper.writeValueAsString(removeRequest);

    // When & Then
    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/6/test-case/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").value(3))
        .andExpect(jsonPath("$.successCount").exists())
        .andExpect(jsonPath("$.failureCount").exists())
        .andExpect(jsonPath("$.errors").isArray());
  }

  @Test
  void addTestCasesToPlanWithValidationSuccessIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = Arrays.asList(7L, 8L);
    BatchAddTestCasesToPlanRQ addRequest = BatchAddTestCasesToPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();
    String jsonContent = objectMapper.writeValueAsString(addRequest);

    // When & Then
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/4/test-case/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").value(2))
        .andExpect(jsonPath("$.successCount").isNumber())
        .andExpect(jsonPath("$.failureCount").isNumber())
        .andExpect(jsonPath("$.errors").isArray());
  }

  @Test
  void removeTestCasesFromPlanWithValidationSuccessIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = Arrays.asList(10L, 11L);
    BatchRemoveTestCasesFromPlanRQ removeRequest = BatchRemoveTestCasesFromPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();
    String jsonContent = objectMapper.writeValueAsString(removeRequest);

    // When & Then
    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/5/test-case/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").value(2))
        .andExpect(jsonPath("$.successCount").isNumber())
        .andExpect(jsonPath("$.failureCount").isNumber())
        .andExpect(jsonPath("$.errors").isArray());
  }

  @Test
  void getTestPlanByIdWithExecutionStatisticIntegrationTest() throws Exception {
    // Given - test plan 100 with execution statistics (2 total, 1 covered)

    // When/Then
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/100")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(100L))
        .andExpect(jsonPath("$.name").value("Test Plan with Executions"))
        .andExpect(jsonPath("$.executionStatistic").exists())
        .andExpect(jsonPath("$.executionStatistic.total").value(2))
        .andExpect(jsonPath("$.executionStatistic.covered").value(1));
  }

  @Test
  void getTestPlanByIdWithoutExecutionStatisticIntegrationTest() throws Exception {
    // Given - test plan 101 without execution statistics (2 total, 0 covered)

    // When/Then
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/101")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(101L))
        .andExpect(jsonPath("$.name").value("Test Plan without Executions"))
        .andExpect(jsonPath("$.executionStatistic").exists())
        .andExpect(jsonPath("$.executionStatistic.total").value(2))
        .andExpect(jsonPath("$.executionStatistic.covered").value(0));
  }

  @Test
  void getTestPlansByCriteriaWithExecutionStatisticsIntegrationTest() throws Exception {
    // When/Then - Get test plans that have execution statistics
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan")
            .param("filter.in.id", "100,101,102")
            .param("offset", "0")
            .param("limit", "10")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(3))
        // Test plan 100 - 2 total, 1 covered
        .andExpect(jsonPath("$.content[?(@.id == 100)].executionStatistic.total").value(2))
        .andExpect(jsonPath("$.content[?(@.id == 100)].executionStatistic.covered").value(1))
        // Test plan 101 - 2 total, 0 covered
        .andExpect(jsonPath("$.content[?(@.id == 101)].executionStatistic.total").value(2))
        .andExpect(jsonPath("$.content[?(@.id == 101)].executionStatistic.covered").value(0))
        // Test plan 102 - 3 total, 2 covered (mixed statuses)
        .andExpect(jsonPath("$.content[?(@.id == 102)].executionStatistic.total").value(3))
        .andExpect(jsonPath("$.content[?(@.id == 102)].executionStatistic.covered").value(2));
  }

  @Test
  void patchTestPlanWithExecutionStatisticIntegrationTest() throws Exception {
    // Given
    TmsTestPlanRQ tmsTestPlan = new TmsTestPlanRQ();
    tmsTestPlan.setDescription("Patched description for test plan with executions");

    String jsonContent = objectMapper.writeValueAsString(tmsTestPlan);

    // When
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/102")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(102L))
        .andExpect(
            jsonPath("$.description").value("Patched description for test plan with executions"))
        .andExpect(jsonPath("$.executionStatistic").exists())
        .andExpect(jsonPath("$.executionStatistic.total").value(3))
        .andExpect(jsonPath("$.executionStatistic.covered").value(2));
  }

  @Test
  void getTestPlanWithMixedExecutionStatusesIntegrationTest() throws Exception {
    // Given - test plan 102 has mixed execution statuses (PASSED, FAILED, SKIPPED)
    // Only PASSED and FAILED should count as "covered"

    // When/Then
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/102")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(102L))
        .andExpect(jsonPath("$.executionStatistic.total").value(3))
        .andExpect(
            jsonPath("$.executionStatistic.covered").value(2)); // Only PASSED and FAILED count
  }

  @Test
  void duplicateTestPlanIntegrationTest() throws Exception {
    // Given - test plan 1 exists with 3 test cases (4, 5, 6) and attributes
    long originalTestPlanId = 1L;

    TmsTestPlanRQ duplicateTestPlanRQ = new TmsTestPlanRQ();
    duplicateTestPlanRQ.setName("Test Plan 1");
    duplicateTestPlanRQ.setDescription("Description for test plan 1");

    String jsonContent = objectMapper.writeValueAsString(duplicateTestPlanRQ);

    // When - duplicate test plan
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/" + originalTestPlanId
                + "/duplicate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.name").value("Test Plan 1"))
        .andExpect(jsonPath("$.description").value("Description for test plan 1"))
        .andExpect(jsonPath("$.executionStatistic").exists())
        .andExpect(jsonPath("$.executionStatistic.total").value(3))
        .andExpect(jsonPath("$.executionStatistic.covered").value(0))
        .andExpect(jsonPath("$.duplicationStatistic").exists())
        .andExpect(jsonPath("$.duplicationStatistic.totalCount").value(3))
        .andExpect(jsonPath("$.duplicationStatistic.successCount").value(3))
        .andExpect(jsonPath("$.duplicationStatistic.failureCount").value(0))
        .andExpect(jsonPath("$.duplicationStatistic.errors").isArray())
        .andExpect(jsonPath("$.duplicationStatistic.errors.length()").value(0));
  }

  @Test
  void duplicateTestPlanWithAttributesIntegrationTest() throws Exception {
    // Given - test plan with attributes exists
    long originalTestPlanId = 4L; // has environment_id and product_version_id

    TmsTestPlanRQ duplicateTestPlanRQ = new TmsTestPlanRQ();
    duplicateTestPlanRQ.setName("name4");
    duplicateTestPlanRQ.setDescription("description4");

    String jsonContent = objectMapper.writeValueAsString(duplicateTestPlanRQ);

    // When - duplicate test plan
    MvcResult result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/" + originalTestPlanId
                + "/duplicate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("name4"))
        .andExpect(jsonPath("$.description").value("description4"))
        .andExpect(jsonPath("$.attributes").doesNotExist())
        .andReturn();

    entityManager.clear();

    // Then - verify duplicated test plan has correct attributes
    String content = result.getResponse().getContentAsString();
    var duplicatedTestPlanResponse = objectMapper.readValue(content, DuplicateTmsTestPlanRS.class);
    var duplicatedTestPlanId = duplicatedTestPlanResponse.getId();

    Optional<TmsTestPlan> duplicatedTestPlan = testPlanRepository.findById(duplicatedTestPlanId);
    assertTrue(duplicatedTestPlan.isPresent());
    assertEquals("name4", duplicatedTestPlan.get().getName());
    assertEquals("description4", duplicatedTestPlan.get().getDescription());
  }

  @Test
  void duplicateTestPlanWithTestCasesIntegrationTest() throws Exception {
    // Given - test plan 2 with 3 test cases (7, 8, 9)
    Long originalTestPlanId = 2L;
    long initialTestCaseCount = testCaseRepository.count();

    TmsTestPlanRQ duplicateTestPlanRQ = new TmsTestPlanRQ();
    duplicateTestPlanRQ.setName("Duplicated Test Plan 2");
    duplicateTestPlanRQ.setDescription("Duplicated description");

    String jsonContent = objectMapper.writeValueAsString(duplicateTestPlanRQ);

    // When - duplicate test plan
    MvcResult result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/" + originalTestPlanId
                + "/duplicate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.duplicationStatistic.successCount").value(3))
        .andReturn();

    // Then - verify test cases were duplicated and added to new plan
    String content = result.getResponse().getContentAsString();
    var duplicatedTestPlan = objectMapper.readValue(content, DuplicateTmsTestPlanRS.class);
    var duplicatedTestPlanId = duplicatedTestPlan.getId();

    // Verify new test cases were created
    long finalTestCaseCount = testCaseRepository.count();
    assertEquals(initialTestCaseCount + 3, finalTestCaseCount);

    // Verify duplicated test cases are in the new plan
    List<Long> testCaseIdsInDuplicatedPlan = tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(
        duplicatedTestPlanId);
    assertEquals(3, testCaseIdsInDuplicatedPlan.size());

    // Verify original test cases still exist and are in original plan
    List<Long> testCaseIdsInOriginalPlan = tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(
        originalTestPlanId);
    assertEquals(3, testCaseIdsInOriginalPlan.size());
    assertEquals(Set.of(7L, 8L, 9L), new HashSet<>(testCaseIdsInOriginalPlan));
  }

  @Test
  void duplicateTestPlanWithTestCaseAttributesIntegrationTest() throws Exception {
    // Given - test plan 1 has test cases with attributes
    long originalTestPlanId = 1L;

    TmsTestPlanRQ duplicateTestPlanRQ = new TmsTestPlanRQ();
    duplicateTestPlanRQ.setName("Test Plan with Attributes");
    duplicateTestPlanRQ.setDescription("Test plan duplication with test case attributes");

    String jsonContent = objectMapper.writeValueAsString(duplicateTestPlanRQ);

    // When - duplicate test plan
    MvcResult result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/" + originalTestPlanId
                + "/duplicate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.duplicationStatistic.successCount").value(3))
        .andReturn();

    // Then - verify duplicated test cases have attributes
    String content = result.getResponse().getContentAsString();
    var duplicatedTestPlan = objectMapper.readValue(content, DuplicateTmsTestPlanRS.class);
    var duplicatedTestPlanId = duplicatedTestPlan.getId();

    List<Long> duplicatedTestCaseIds = tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(
        duplicatedTestPlanId);
    assertEquals(3, duplicatedTestCaseIds.size());

    // Verify each duplicated test case has the expected attributes
    for (Long testCaseId : duplicatedTestCaseIds) {
      TmsTestCase duplicatedTestCase = testCaseRepository.findById(testCaseId).orElseThrow();
      assertNotNull(duplicatedTestCase);

      // Check that test case has attributes (the exact attributes depend on SQL data)
      if (duplicatedTestCase.getName().contains("Test Case 4")) {
        // Original test case 4 had attribute with id=4, value="test value 4"
        List<TmsTestCaseAttribute> attributes = tmsTestCaseAttributeRepository.findAllById_TestCaseId(
            testCaseId);
        assertFalse(attributes.isEmpty());
      }
    }
  }

  @Test
  void duplicateEmptyTestPlanIntegrationTest() throws Exception {
    // Given - create an empty test plan
    TmsTestPlanRQ emptyTestPlan = new TmsTestPlanRQ();
    emptyTestPlan.setName("Empty Test Plan");
    emptyTestPlan.setDescription("Test plan with no test cases");

    String jsonContent = objectMapper.writeValueAsString(emptyTestPlan);

    // Create empty test plan
    MvcResult createResult = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();

    String createContent = createResult.getResponse().getContentAsString();
    var createdTestPlan = objectMapper.readValue(createContent, DuplicateTmsTestPlanRS.class);
    var emptyTestPlanId = createdTestPlan.getId();

    TmsTestPlanRQ duplicateTestPlanRQ = new TmsTestPlanRQ();
    duplicateTestPlanRQ.setName("Empty Test Plan");
    duplicateTestPlanRQ.setDescription("Test plan with no test cases");

    String duplicateJsonContent = objectMapper.writeValueAsString(duplicateTestPlanRQ);

    // When - duplicate empty test plan
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/" + emptyTestPlanId
                + "/duplicate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(duplicateJsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Empty Test Plan"))
        .andExpect(jsonPath("$.description").value("Test plan with no test cases"))
        .andExpect(jsonPath("$.duplicationStatistic.totalCount").value(0))
        .andExpect(jsonPath("$.duplicationStatistic.successCount").value(0))
        .andExpect(jsonPath("$.duplicationStatistic.failureCount").value(0));
  }

  @Test
  void duplicateNonExistentTestPlanIntegrationTest() throws Exception {
    // Given - non-existent test plan ID
    long nonExistentTestPlanId = 99999L;

    TmsTestPlanRQ duplicateTestPlanRQ = new TmsTestPlanRQ();
    duplicateTestPlanRQ.setName("Non-existent Plan");

    String jsonContent = objectMapper.writeValueAsString(duplicateTestPlanRQ);

    // When/Then - should return 404 Not Found
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/" + nonExistentTestPlanId
                + "/duplicate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void duplicateTestPlanVerifyIndependenceIntegrationTest() throws Exception {
    // Given - test plan 3 with test cases
    Long originalTestPlanId = 3L;

    TmsTestPlanRQ duplicateTestPlanRQ = new TmsTestPlanRQ();
    duplicateTestPlanRQ.setName("Independent Duplicated Plan");
    duplicateTestPlanRQ.setDescription("Verifying independence of duplicated plan");

    String jsonContent = objectMapper.writeValueAsString(duplicateTestPlanRQ);

    // When - duplicate test plan
    MvcResult result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/" + originalTestPlanId
                + "/duplicate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();

    String content = result.getResponse().getContentAsString();
    var duplicatedTestPlan = objectMapper.readValue(content, DuplicateTmsTestPlanRS.class);
    var duplicatedTestPlanId = duplicatedTestPlan.getId();

    // Then - verify that modifying original doesn't affect duplicate
    // Get original and duplicated test cases
    List<Long> originalTestCaseIds = tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(
        originalTestPlanId);
    List<Long> duplicatedTestCaseIds = tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(
        duplicatedTestPlanId);

    assertEquals(originalTestCaseIds.size(), duplicatedTestCaseIds.size());

    // Verify they are different test cases (different IDs)
    assertFalse(originalTestCaseIds.containsAll(duplicatedTestCaseIds));
    assertFalse(duplicatedTestCaseIds.containsAll(originalTestCaseIds));

    // Verify original and duplicate test cases have same names but different IDs
    for (int i = 0; i < originalTestCaseIds.size(); i++) {
      TmsTestCase original = testCaseRepository.findById(originalTestCaseIds.get(i)).orElseThrow();
      TmsTestCase duplicate = testCaseRepository.findById(duplicatedTestCaseIds.get(i))
          .orElseThrow();

      assertNotEquals(original.getId(), duplicate.getId());
      assertEquals(original.getName() + "-copy", duplicate.getName());
      assertEquals(original.getDescription(), duplicate.getDescription());
      assertEquals(original.getPriority(), duplicate.getPriority());
    }
  }

  @Test
  void duplicateTestPlanWithCustomNameIntegrationTest() throws Exception {
    // Given - test plan 1 exists
    long originalTestPlanId = 1L;

    TmsTestPlanRQ duplicateTestPlanRQ = new TmsTestPlanRQ();
    duplicateTestPlanRQ.setName("Custom Named Duplicate");
    duplicateTestPlanRQ.setDescription("Original description");

    String jsonContent = objectMapper.writeValueAsString(duplicateTestPlanRQ);

    // When - duplicate test plan with custom name
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/" + originalTestPlanId
                + "/duplicate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Custom Named Duplicate"))
        .andExpect(jsonPath("$.description").value("Original description"));
  }

  @Test
  void duplicateTestPlanWithCustomDescriptionIntegrationTest() throws Exception {
    // Given - test plan 2 exists
    long originalTestPlanId = 2L;

    TmsTestPlanRQ duplicateTestPlanRQ = new TmsTestPlanRQ();
    duplicateTestPlanRQ.setName("Original Name");
    duplicateTestPlanRQ.setDescription("Custom description for duplicate");

    String jsonContent = objectMapper.writeValueAsString(duplicateTestPlanRQ);

    // When - duplicate test plan with custom description
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/" + originalTestPlanId
                + "/duplicate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Original Name"))
        .andExpect(jsonPath("$.description").value("Custom description for duplicate"));
  }

  @Test
  void duplicateTestPlanWithBothCustomFieldsIntegrationTest() throws Exception {
    // Given - test plan 3 exists
    long originalTestPlanId = 3L;

    TmsTestPlanRQ duplicateTestPlanRQ = new TmsTestPlanRQ();
    duplicateTestPlanRQ.setName("Completely New Name");
    duplicateTestPlanRQ.setDescription("Completely new description");

    String jsonContent = objectMapper.writeValueAsString(duplicateTestPlanRQ);

    // When - duplicate test plan with both custom name and description
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/" + originalTestPlanId
                + "/duplicate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Completely New Name"))
        .andExpect(jsonPath("$.description").value("Completely new description"))
        .andExpect(jsonPath("$.duplicationStatistic").exists());
  }

  @Test
  void duplicateTestPlanWithEmptyRequestBodyIntegrationTest() throws Exception {
    // Given - test plan 1 exists
    long originalTestPlanId = 1L;

    TmsTestPlanRQ duplicateTestPlanRQ = new TmsTestPlanRQ();
    // Empty request body - should use original values

    String jsonContent = objectMapper.writeValueAsString(duplicateTestPlanRQ);

    // When - duplicate test plan with empty request
    MvcResult result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/" + originalTestPlanId
                + "/duplicate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andReturn();

    // Then - verify that null values were used (as per mapper logic)
    String content = result.getResponse().getContentAsString();
    var duplicatedTestPlan = objectMapper.readValue(content, DuplicateTmsTestPlanRS.class);

    // Name and description will be null according to mapper
    // (since duplicateTestPlanRQ has null values)
    Optional<TmsTestPlan> testPlan = testPlanRepository.findById(duplicatedTestPlan.getId());
    assertTrue(testPlan.isPresent());
  }

  @Test
  void getTestCasesAddedToPlan_shouldReturnPagedTestCasesWithLastExecution() throws Exception {
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_TEST_CASES_ID)
            .param("offset", "0")
            .param("limit", "10")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(4))
        .andExpect(jsonPath("$.content[0].id").isNumber())
        .andExpect(jsonPath("$.content[0].name").isString())
        .andExpect(jsonPath("$.content[0].description").isString())
        .andExpect(jsonPath("$.content[0].priority").isString())
        .andExpect(jsonPath("$.content[0].externalId").isString())
        .andExpect(jsonPath("$.content[0].testFolder").exists())
        .andExpect(jsonPath("$.content[0].testFolder.id").isNumber())
        .andExpect(jsonPath("$.content[0].manualScenario").exists())
        .andExpect(jsonPath("$.content[0].createdAt").isNumber())
        .andExpect(jsonPath("$.content[0].updatedAt").isNumber())
        .andExpect(jsonPath("$.page.totalElements").value(4))
        .andExpect(jsonPath("$.page.size").value(10))
        .andExpect(jsonPath("$.page.number").value(1));
  }

  @Test
  void getTestCasesAddedToPlan_shouldReturnLastExecutionOnly() throws Exception {
    // Test case 200 has 3 executions, but should return only the last one
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_TEST_CASES_ID)
            .param("offset", "0")
            .param("limit", "10")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[?(@.id == " + TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID
            + ")].lastExecution").exists())
        .andExpect(jsonPath("$.content[?(@.id == " + TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID
            + ")].lastExecution.id").value(202))
        .andExpect(jsonPath("$.content[?(@.id == " + TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID
            + ")].lastExecution.status").value("PASSED"))
        .andExpect(jsonPath("$.content[?(@.id == " + TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID
            + ")].executions").doesNotExist());
  }

  @Test
  void getTestCasesAddedToPlan_shouldReturnNullLastExecutionForTestCaseWithoutExecutions()
      throws Exception {
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_TEST_CASES_ID)
            .param("offset", "0")
            .param("limit", "10")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[?(@.id == " + TEST_CASE_WITHOUT_EXECUTIONS_ID
            + ")].lastExecution").isEmpty());
  }

  @Test
  void getTestCasesAddedToPlan_withPagination_shouldReturnCorrectPage() throws Exception {
    // First page
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_TEST_CASES_ID)
            .param("offset", "0")
            .param("limit", "2")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.page.size").value(2))
        .andExpect(jsonPath("$.page.number").value(1))
        .andExpect(jsonPath("$.page.totalElements").value(4));

    // Second page
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_TEST_CASES_ID)
            .param("offset", "2")
            .param("limit", "2")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.page.size").value(2))
        .andExpect(jsonPath("$.page.number").value(2))
        .andExpect(jsonPath("$.page.totalElements").value(4));
  }

  @Test
  void getTestCasesAddedToPlan_whenNoTestCases_shouldReturnEmptyPage() throws Exception {
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_EMPTY_FOR_RETRIEVAL_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.page.totalElements").value(0));
  }

  @Test
  void getTestCasesAddedToPlan_whenTestPlanNotFound_shouldReturn404() throws Exception {
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case",
            SUPERADMIN_PROJECT_KEY, NON_EXISTENT_TEST_PLAN_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void getTestCasesAddedToPlan_shouldIncludeExecutionDetailsInLastExecution() throws Exception {
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_TEST_CASES_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[?(@.id == " + TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID
            + ")].lastExecution.id").exists())
        .andExpect(jsonPath("$.content[?(@.id == " + TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID
            + ")].lastExecution.launch").exists())
        .andExpect(jsonPath("$.content[?(@.id == " + TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID
            + ")].lastExecution.launch.id").exists())
        .andExpect(jsonPath("$.content[?(@.id == " + TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID
            + ")].lastExecution.status").exists())
        .andExpect(jsonPath("$.content[?(@.id == " + TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID
            + ")].lastExecution.startedAt").exists())
        .andExpect(jsonPath("$.content[?(@.id == " + TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID
            + ")].lastExecution.finishedAt").exists())
        .andExpect(jsonPath("$.content[?(@.id == " + TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID
            + ")].lastExecution.duration").exists());
  }

  // ========== Tests for GET /test-plan/{testPlanId}/test-case/{id} - getTestCaseInTestPlan ==========

  @Test
  void getTestCaseInTestPlan_shouldReturnTestCaseWithAllExecutions() throws Exception {
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/{id}",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_TEST_CASES_ID, TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID))
        .andExpect(jsonPath("$.name").value("Test Case with Multiple Executions"))
        .andExpect(jsonPath("$.testFolder").exists())
        .andExpect(jsonPath("$.lastExecution").exists())
        .andExpect(jsonPath("$.lastExecution.id").value(202))
        .andExpect(jsonPath("$.executions").isArray())
        .andExpect(jsonPath("$.executions.length()").value(3));
  }

  @Test
  void getTestCaseInTestPlan_shouldReturnLastExecutionAsNewest() throws Exception {
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/{id}",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_TEST_CASES_ID, TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.lastExecution").exists())
        .andExpect(jsonPath("$.lastExecution.id").value(202)) // Latest execution
        .andExpect(jsonPath("$.lastExecution.status").value("PASSED"))
        .andExpect(jsonPath("$.lastExecution.startedAt").isNumber())
        .andExpect(jsonPath("$.executions").isArray())
        .andExpect(jsonPath("$.executions.length()").value(3));
  }

  @Test
  void getTestCaseInTestPlan_executionsShouldBeOrderedByStartTimeDesc() throws Exception {
    MvcResult result = mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/{id}",
                SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_TEST_CASES_ID,
                TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.executions").isArray())
        .andExpect(jsonPath("$.executions.length()").value(3))
        .andReturn();

    String content = result.getResponse().getContentAsString();
    // Verify executions are ordered: 202 (newest), 201, 200 (oldest)
    assertTrue(content.contains("\"id\":202"));
    assertTrue(content.contains("\"id\":201"));
    assertTrue(content.contains("\"id\":200"));
  }

  @Test
  void getTestCaseInTestPlan_whenTestCaseNotInPlan_shouldReturn404() throws Exception {
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/{id}",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_TEST_CASES_ID, TEST_CASE_NOT_IN_PLAN_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void getTestCaseInTestPlan_whenTestCaseNotFound_shouldReturn404() throws Exception {
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/{id}",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_TEST_CASES_ID, NON_EXISTENT_TEST_CASE_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void getTestCaseInTestPlan_whenTestPlanNotFound_shouldReturn404() throws Exception {
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/{id}",
            SUPERADMIN_PROJECT_KEY, NON_EXISTENT_TEST_PLAN_ID, TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void getTestCaseInTestPlan_withNoExecutions_shouldReturnTestCaseWithEmptyExecutions()
      throws Exception {
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/{id}",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_TEST_CASES_ID, TEST_CASE_WITHOUT_EXECUTIONS_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(TEST_CASE_WITHOUT_EXECUTIONS_ID))
        .andExpect(jsonPath("$.name").value("Test Case without Executions"))
        .andExpect(jsonPath("$.lastExecution").doesNotExist())
        .andExpect(jsonPath("$.executions").doesNotExist());
  }

  @Test
  void getTestCaseInTestPlan_shouldIncludeAllTestCaseDetails() throws Exception {
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/{id}",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_TEST_CASES_ID, TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID))
        .andExpect(jsonPath("$.name").isString())
        .andExpect(jsonPath("$.description").isString())
        .andExpect(jsonPath("$.priority").isString())
        .andExpect(jsonPath("$.externalId").isString())
        .andExpect(jsonPath("$.testFolder").exists())
        .andExpect(jsonPath("$.testFolder.id").isNumber())
        .andExpect(jsonPath("$.createdAt").isNumber())
        .andExpect(jsonPath("$.updatedAt").isNumber())
        .andExpect(jsonPath("$.manualScenario").exists());
  }

  @Test
  void getTestCaseInTestPlan_executionsShouldHaveRequiredFields() throws Exception {
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/{id}",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_TEST_CASES_ID, TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.executions[0].id").isNumber())
        .andExpect(jsonPath("$.executions[0].launch").exists())
        .andExpect(jsonPath("$.executions[0].launch.id").isNumber())
        .andExpect(jsonPath("$.executions[0].status").isString())
        .andExpect(jsonPath("$.executions[0].startedAt").isNumber())
        .andExpect(jsonPath("$.executions[0].finishedAt").isNumber())
        .andExpect(jsonPath("$.executions[0].duration").isNumber());
  }

  @Test
  void getTestCaseInTestPlan_testCaseWithTwoExecutions_shouldReturnCorrectData() throws Exception {
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/{id}",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_TEST_CASES_ID, TEST_CASE_WITH_TWO_EXECUTIONS_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(TEST_CASE_WITH_TWO_EXECUTIONS_ID))
        .andExpect(jsonPath("$.name").value("Test Case with Two Executions"))
        .andExpect(jsonPath("$.lastExecution").exists())
        .andExpect(jsonPath("$.lastExecution.id").value(211))
        .andExpect(jsonPath("$.lastExecution.status").value("FAILED"))
        .andExpect(jsonPath("$.executions").isArray())
        .andExpect(jsonPath("$.executions.length()").value(2));
  }

  @Test
  void getTestCaseInTestPlan_testCaseWithOneExecution_shouldReturnCorrectData() throws Exception {
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/{id}",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_TEST_CASES_ID, TEST_CASE_WITH_ONE_EXECUTION_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(TEST_CASE_WITH_ONE_EXECUTION_ID))
        .andExpect(jsonPath("$.name").value("Test Case with One Execution"))
        .andExpect(jsonPath("$.lastExecution").exists())
        .andExpect(jsonPath("$.lastExecution.id").value(220))
        .andExpect(jsonPath("$.lastExecution.status").value("PASSED"))
        .andExpect(jsonPath("$.executions").isArray())
        .andExpect(jsonPath("$.executions.length()").value(1));
  }

  @Test
  void getTestCaseInTestPlan_lastExecutionShouldMatchFirstInExecutionsList() throws Exception {
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/{id}",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_TEST_CASES_ID, TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.lastExecution.id").value(202))
        .andExpect(jsonPath("$.lastExecution.launch.id").value(302))
        .andExpect(jsonPath("$.lastExecution.status").value("PASSED"));
  }

  @Test
  void getTestCaseInTestPlan_verifyExecutionStatuses() throws Exception {
    MvcResult result = mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/{id}",
                SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_TEST_CASES_ID,
                TEST_CASE_WITH_MULTIPLE_EXECUTIONS_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.executions.length()").value(3))
        .andReturn();

    String content = result.getResponse().getContentAsString();
    // Verify that executions contain expected statuses: PASSED (3002), FAILED (3001), PASSED (3000)
    assertTrue(content.contains("PASSED"));
    assertTrue(content.contains("FAILED"));
  }

  // ========== Constants for getTestFoldersFromPlan tests ==========

  private static final Long TEST_PLAN_WITH_MULTIPLE_FOLDERS_ID = 300L;
  private static final Long TEST_PLAN_EMPTY_FOR_FOLDERS_ID = 301L;
  private static final Long TEST_PLAN_WITH_SINGLE_FOLDER_ID = 302L;
  private static final Long TEST_PLAN_WITH_MANY_FOLDERS_ID = 303L;
  private static final Long FOLDER_300_ID = 300L;
  private static final Long FOLDER_301_ID = 301L;
  private static final Long FOLDER_302_ID = 302L;
  private static final Long FOLDER_303_ID = 303L;

  // ========== Tests for GET /test-plan/{id}/folder - getTestFoldersFromPlan ==========

  @Test
  void getTestFoldersFromPlan_shouldReturnFoldersWithTestCaseCount() throws Exception {
    // Test plan 300 has test cases from 3 folders (300, 301, 302)
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{id}/folder",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_MULTIPLE_FOLDERS_ID)
            .param("offset", "0")
            .param("limit", "10")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(3))
        .andExpect(jsonPath("$.page.totalElements").value(3))
        // Verify folders contain test case counts
        .andExpect(jsonPath("$.content[*].id").exists())
        .andExpect(jsonPath("$.content[*].name").exists())
        .andExpect(jsonPath("$.content[*].countOfTestCases").exists());
  }

  @Test
  void getTestFoldersFromPlan_shouldReturnCorrectTestCaseCountsPerFolder() throws Exception {
    // Test plan 300 has:
    // - Folder 300: 2 test cases (300, 301)
    // - Folder 301: 3 test cases (302, 303, 304)
    // - Folder 302: 1 test case (305)

    MvcResult result = mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{id}/folder",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_MULTIPLE_FOLDERS_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();

    String content = result.getResponse().getContentAsString();

    // Verify specific counts for each folder
    assertTrue(content.contains("\"countOfTestCases\":2"));
    assertTrue(content.contains("\"countOfTestCases\":3"));
    assertTrue(content.contains("\"countOfTestCases\":1"));

    // Verify folder IDs are present
    assertTrue(content.contains("\"id\":" + FOLDER_300_ID));
    assertTrue(content.contains("\"id\":" + FOLDER_301_ID));
    assertTrue(content.contains("\"id\":" + FOLDER_302_ID));
  }

  @Test
  void getTestFoldersFromPlan_whenEmptyPlan_shouldReturnEmptyPage() throws Exception {
    // Test plan 301 has no test cases
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{id}/folder",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_EMPTY_FOR_FOLDERS_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.page.totalElements").value(0));
  }

  @Test
  void getTestFoldersFromPlan_whenNonExistentPlan_shouldReturn404() throws Exception {
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{id}/folder",
            SUPERADMIN_PROJECT_KEY, NON_EXISTENT_TEST_PLAN_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void getTestFoldersFromPlan_withSingleFolder_shouldReturnOneFolderWithCorrectCount()
      throws Exception {
    // Test plan 302 has all test cases from folder 303 (5 test cases)
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{id}/folder",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_SINGLE_FOLDER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].id").value(FOLDER_303_ID))
        .andExpect(jsonPath("$.content[0].name").value("Folder 303 for Plan 302"))
        .andExpect(jsonPath("$.content[0].countOfTestCases").value(5))
        .andExpect(jsonPath("$.page.totalElements").value(1));
  }

  @Test
  void getTestFoldersFromPlan_withPagination_shouldReturnCorrectFirstPage() throws Exception {
    // Test plan 303 has test cases from 10 folders (304-313)

    // First page with 5 items
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{id}/folder",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_MANY_FOLDERS_ID)
            .param("offset", "0")
            .param("limit", "5")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(5))
        .andExpect(jsonPath("$.page.size").value(5))
        .andExpect(jsonPath("$.page.number").value(1))
        .andExpect(jsonPath("$.page.totalElements").value(10));
  }

  @Test
  void getTestFoldersFromPlan_withPagination_shouldReturnCorrectSecondPage() throws Exception {
    // Test plan 303 has test cases from 10 folders (304-313)

    // Second page with 5 items
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{id}/folder",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_MANY_FOLDERS_ID)
            .param("offset", "5")
            .param("limit", "5")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(5))
        .andExpect(jsonPath("$.page.size").value(5))
        .andExpect(jsonPath("$.page.number").value(2))
        .andExpect(jsonPath("$.page.totalElements").value(10));
  }

  @Test
  void getTestFoldersFromPlan_shouldReturnUniqueFolders() throws Exception {
    // Verify that if multiple test cases from same folder are in plan,
    // folder appears only once with correct count
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{id}/folder",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_SINGLE_FOLDER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].id").value(FOLDER_303_ID))
        .andExpect(jsonPath("$.content[0].countOfTestCases").value(5))
        .andExpect(jsonPath("$.page.totalElements").value(1));
  }

  @Test
  void getTestFoldersFromPlan_shouldIncludeFolderDetails() throws Exception {
    // Verify all folder details are present in response
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{id}/folder",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_MULTIPLE_FOLDERS_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").exists())
        .andExpect(jsonPath("$.content[0].name").exists())
        .andExpect(jsonPath("$.content[0].description").exists())
        .andExpect(jsonPath("$.content[0].countOfTestCases").exists());
  }

  @Test
  void getTestFoldersFromPlan_withLimitGreaterThanTotal_shouldReturnAllFolders() throws Exception {
    // Test plan 300 has 3 folders, request with limit 10
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{id}/folder",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_MULTIPLE_FOLDERS_ID)
            .param("offset", "0")
            .param("limit", "10")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(3))
        .andExpect(jsonPath("$.page.totalElements").value(3));
  }

  @Test
  void getTestFoldersFromPlan_withOffsetBeyondTotal_shouldReturnEmptyPage() throws Exception {
    // Test plan 300 has 3 folders, request offset 10
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{id}/folder",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_MULTIPLE_FOLDERS_ID)
            .param("offset", "10")
            .param("limit", "5")
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.page.totalElements").value(3));
  }

  @Test
  void getTestFoldersFromPlan_verifyFolderNames() throws Exception {
    // Verify specific folder names are returned correctly
    MvcResult result = mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{id}/folder",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_MULTIPLE_FOLDERS_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();

    String content = result.getResponse().getContentAsString();

    assertTrue(content.contains("Folder 300 for Plan 300"));
    assertTrue(content.contains("Folder 301 for Plan 300"));
    assertTrue(content.contains("Folder 302 for Plan 300"));
  }

  @Test
  void getTestFoldersFromPlan_verifyFolderDescriptions() throws Exception {
    // Verify folder descriptions are included
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{id}/folder",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_SINGLE_FOLDER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].description")
            .value("Single folder with multiple test cases"));
  }

  @Test
  void getTestFoldersFromPlan_withDefaultPagination_shouldUseDefaultValues() throws Exception {
    // Test without explicit offset and limit parameters
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{id}/folder",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_MULTIPLE_FOLDERS_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(3))
        .andExpect(jsonPath("$.page.totalElements").value(3));
  }

  @Test
  void getTestFoldersFromPlan_eachFolderShouldHavePositiveTestCaseCount() throws Exception {
    // Verify that all returned folders have at least 1 test case
    MvcResult result = mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{id}/folder",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_MANY_FOLDERS_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();

    String content = result.getResponse().getContentAsString();

    // Each folder in pagination test has exactly 1 test case
    int countOccurrences = content.split("\"countOfTestCases\":1", -1).length - 1;
    assertEquals(10, countOccurrences);
  }

  @Test
  void getTestFoldersFromPlan_shouldNotReturnFoldersWithoutTestCasesInPlan() throws Exception {
    // Even if a folder exists in the project, it should not be returned
    // if it has no test cases in the specified test plan
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{id}/folder",
            SUPERADMIN_PROJECT_KEY, TEST_PLAN_WITH_SINGLE_FOLDER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].id").value(FOLDER_303_ID));
  }
}
