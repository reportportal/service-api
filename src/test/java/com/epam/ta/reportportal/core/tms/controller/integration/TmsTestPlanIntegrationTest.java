package com.epam.ta.reportportal.core.tms.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlan;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestPlanRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchAddTestCasesToPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchRemoveTestCasesFromPlanRQ;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

/**
 * @author <a href="mailto:konstantin_shaplyko@epam.com">Konstantin Shaplyko</a>
 */
@Sql("/db/tms/tms-product-version/tms-test-plan-fill.sql")
@ExtendWith(MockitoExtension.class)
public class TmsTestPlanIntegrationTest extends BaseMvcTest {

  private static final String SUPERADMIN_PROJECT_KEY = "superadmin_personal";
  private final ObjectMapper objectMapper = new ObjectMapper();
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
        .andExpect(jsonPath("$.content.length()").value(3));
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
            .param("search", "")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0));
  }

  @Test
  void getTestPlansByCriteriaWithNonExistentSearchIntegrationTest() throws Exception {
    // Test search with non-existent term
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-plan")
            .param("search", "nonexistent_search_term")
            .param("page", "0")
            .param("size", "10")
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
        .andExpect(jsonPath("$.content.length()").value(3));
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
    attributQ.setValue("value5");
    attributQ.setId(5L);

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
}
