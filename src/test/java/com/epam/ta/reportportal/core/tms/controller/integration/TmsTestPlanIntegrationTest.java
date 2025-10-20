package com.epam.ta.reportportal.core.tms.controller.integration;

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

import com.epam.ta.reportportal.core.tms.dto.DuplicateTmsTestPlanRS;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchAddTestCasesToPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchRemoveTestCasesFromPlanRQ;
import com.epam.ta.reportportal.dao.tms.TmsTestCaseAttributeRepository;
import com.epam.ta.reportportal.dao.tms.TmsTestCaseRepository;
import com.epam.ta.reportportal.dao.tms.TmsTestPlanRepository;
import com.epam.ta.reportportal.dao.tms.TmsTestPlanTestCaseRepository;
import com.epam.ta.reportportal.entity.tms.TmsTestCase;
import com.epam.ta.reportportal.entity.tms.TmsTestCaseAttribute;
import com.epam.ta.reportportal.entity.tms.TmsTestPlan;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

/**
 * @author <a href="mailto:konstantin_shaplyko@epam.com">Konstantin Shaplyko</a>
 */
@Sql("/db/tms/tms-product-version/tms-test-plan-fill.sql")
@ExtendWith(MockitoExtension.class)
public class TmsTestPlanIntegrationTest extends BaseMvcTest {

  private static final String SUPERADMIN_PROJECT_KEY = "superadmin_personal";

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
            .param("page", "0")
            .param("size", "10")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(9));
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
            .param("page", "0")
            .param("size", "10")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(9));
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

    // When - duplicate test plan
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/" + originalTestPlanId)
                .contentType(MediaType.APPLICATION_JSON)
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

    // When - duplicate test plan
    MvcResult result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/" + originalTestPlanId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("name4"))
        .andExpect(jsonPath("$.description").value("description4"))
        .andExpect(jsonPath("$.attributes").exists())
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

    // When - duplicate test plan
    MvcResult result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/" + originalTestPlanId)
                .contentType(MediaType.APPLICATION_JSON)
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

    // When - duplicate test plan
    MvcResult result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/" + originalTestPlanId)
                .contentType(MediaType.APPLICATION_JSON)
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

    // When - duplicate empty test plan
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/" + emptyTestPlanId)
                .contentType(MediaType.APPLICATION_JSON)
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

    // When/Then - should return 404 Not Found
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/" + nonExistentTestPlanId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void duplicateTestPlanVerifyIndependenceIntegrationTest() throws Exception {
    // Given - test plan 3 with test cases
    Long originalTestPlanId = 3L;

    // When - duplicate test plan
    MvcResult result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan/" + originalTestPlanId)
                .contentType(MediaType.APPLICATION_JSON)
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
}
