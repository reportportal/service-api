package com.epam.ta.reportportal.core.tms.controller.integration;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestCaseRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDeleteTestCasesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchPatchTestCasesRQ;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * Integration tests for TestCaseController
 */
@Sql("/db/tms/tms-test-case/tms-test-case-fill.sql")
@ExtendWith(MockitoExtension.class)
public class TmsTestCaseIntegrationTest extends BaseMvcTest {

  private static final String SUPERADMIN_PROJECT_KEY = "superadmin_personal";

  @Autowired
  private TmsTestCaseRepository testCaseRepository;

  @Test
  void createTestCaseIntegrationTest() throws Exception {
    // Given
    TmsTestCaseAttributeRQ attribute = new TmsTestCaseAttributeRQ();
    attribute.setValue("value3");
    attribute.setAttributeId(3L);

    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Test Case 3");
    testCaseRQ.setDescription("Description for test case 3");
    testCaseRQ.setTestFolderId(3L);
    testCaseRQ.setTags(List.of(attribute));

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(post("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // Then
    Optional<TmsTestCase> testCase = testCaseRepository.findById(1L);

    assertTrue(testCase.isPresent());
    assertEquals(testCaseRQ.getName(), testCase.get().getName());
    assertEquals(testCaseRQ.getDescription(), testCase.get().getDescription());
    assertEquals(testCaseRQ.getTestFolderId(), testCase.get().getTestFolder().getId());
  }

  @Test
  void getTestCaseByIdIntegrationTest() throws Exception {
    // Given
    Optional<TmsTestCase> testCase = testCaseRepository.findById(4L);

    // When/Then
    mockMvc.perform(get("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/4")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(testCase.get().getId()))
        .andExpect(jsonPath("$.name").value(testCase.get().getName()))
        .andExpect(jsonPath("$.description").value(testCase.get().getDescription()));
  }

  @Test
  void updateTestCaseIntegrationTest() throws Exception {
    // Given
    TmsTestCaseAttributeRQ attribute = new TmsTestCaseAttributeRQ();
    attribute.setValue("value4");
    attribute.setAttributeId(4L);

    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Updated Test Case 5");
    testCaseRQ.setDescription("Updated description for test case 5");
    testCaseRQ.setTestFolderId(5L);
    testCaseRQ.setTags(List.of(attribute));

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(put("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/5")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // Then
    Optional<TmsTestCase> testCase = testCaseRepository.findById(5L);

    assertTrue(testCase.isPresent());
    assertEquals(testCaseRQ.getName(), testCase.get().getName());
    assertEquals(testCaseRQ.getDescription(), testCase.get().getDescription());
    assertEquals(testCaseRQ.getTestFolderId(), testCase.get().getTestFolder().getId());
  }

  @Test
  void patchTestCaseIntegrationTest() throws Exception {
    // Given
    TmsTestCaseAttributeRQ attribute = new TmsTestCaseAttributeRQ();
    attribute.setValue("value6");
    attribute.setAttributeId(6L);
    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Patched Test Case 6");
    testCaseRQ.setDescription("Patched description for test case 6");
    testCaseRQ.setTestFolderId(6L);
    testCaseRQ.setTags(List.of(attribute));

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(patch("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/6")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // Then
    Optional<TmsTestCase> testCase = testCaseRepository.findById(6L);

    assertTrue(testCase.isPresent());
    assertEquals(testCaseRQ.getName(), testCase.get().getName());
    assertEquals(testCaseRQ.getDescription(), testCase.get().getDescription());
    assertEquals(testCaseRQ.getTestFolderId(), testCase.get().getTestFolder().getId());
  }

  @Test
  void deleteTestCasesIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = List.of(7L, 8L);
    BatchDeleteTestCasesRQ deleteRequest = BatchDeleteTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(deleteRequest);

    // When
    mockMvc.perform(delete("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/delete")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNoContent());

    // Then
    assertTrue(testCaseRepository.findById(7L).isEmpty());
    assertTrue(testCaseRepository.findById(8L).isEmpty());
  }

  @Test
  void batchPatchTestCasesIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = List.of(9L, 10L);
    Long newFolderId = 6L;

    BatchPatchTestCasesRQ batchPatchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(newFolderId)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(batchPatchRequest);

    // When
    mockMvc.perform(patch("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/patch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // Then
    Optional<TmsTestCase> testCase9After = testCaseRepository.findById(9L);
    Optional<TmsTestCase> testCase10After = testCaseRepository.findById(10L);

    assertTrue(testCase9After.isPresent());
    assertTrue(testCase10After.isPresent());
    assertEquals(newFolderId, testCase9After.get().getTestFolder().getId());
    assertEquals(newFolderId, testCase10After.get().getTestFolder().getId());
  }

  @Test
  void deleteTestCasesWithEmptyLocationIdsIntegrationTest() throws Exception {
    // Given
    BatchDeleteTestCasesRQ deleteRequest = BatchDeleteTestCasesRQ.builder()
        .testCaseIds(Collections.emptyList())
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(deleteRequest);

    // When/Then - should return bad request due to @NotEmpty validation
    mockMvc.perform(delete("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/delete")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void deleteTestCasesWithNonExistentIdsIntegrationTest() throws Exception {
    // Given
    List<Long> nonExistentIds = List.of(999L, 1000L);
    BatchDeleteTestCasesRQ deleteRequest = BatchDeleteTestCasesRQ.builder()
        .testCaseIds(nonExistentIds)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(deleteRequest);

    // When/Then - should not throw exception, just silently ignore non-existent IDs
    mockMvc.perform(delete("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/delete")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNoContent());
  }

  @Test
  void batchPatchTestCasesWithInvalidFolderIdIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = List.of(9L, 10L);
    Long nonExistentFolderId = 999L;

    BatchPatchTestCasesRQ batchPatchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(nonExistentFolderId)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(batchPatchRequest);


    mockMvc.perform(patch("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/patch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(jsonPath("$.message").value(containsString("violates foreign key constraint")));
  }

  @Test
  void batchPatchTestCasesWithNullFolderIdIntegrationTest() throws Exception {

    // Create request with null testFolderId - this should be handled by @NotNull validation
    String jsonContent = "{\"testCaseIds\": [9, 10], \"testFolderId\": null}";

    // When/Then - should fail validation due to @NotNull annotation
    mockMvc.perform(patch("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/patch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void batchPatchTestCasesWithEmptyTestCaseIdsIntegrationTest() throws Exception {
    // Given
    BatchPatchTestCasesRQ batchPatchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(Collections.emptyList())
        .testFolderId(9L)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(batchPatchRequest);

    // When/Then - should return bad request due to @NotEmpty validation
    mockMvc.perform(patch("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/patch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void deleteTestCasesWithNullTestCaseIdsIntegrationTest() throws Exception {
    // Given - create request with null locationIds
    String jsonContent = "{\"testCaseIds\": null}";

    // When/Then - should return bad request due to @NotNull validation
    mockMvc.perform(delete("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/delete")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void batchPatchTestCasesWithNullTestCaseIdsIntegrationTest() throws Exception {
    // Given - create request with null locationIds
    String jsonContent = "{\"testCaseIds\": null, \"testFolderId\": 6}";

    // When/Then - should return bad request due to @NotNull validation
    mockMvc.perform(patch("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/patch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void deleteTestCasesWithSingleIdIntegrationTest() throws Exception {
    // Given
    List<Long> singleTestCaseId = List.of(11L);
    BatchDeleteTestCasesRQ deleteRequest = BatchDeleteTestCasesRQ.builder()
        .testCaseIds(singleTestCaseId)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(deleteRequest);

    // When
    mockMvc.perform(delete("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/delete")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNoContent());

    // Then
    assertTrue(testCaseRepository.findById(11L).isEmpty());
  }

  @Test
  void batchPatchTestCasesWithSingleIdIntegrationTest() throws Exception {
    // Given
    List<Long> singleTestCaseId = List.of(12L);
    Long newFolderId = 6L;

    BatchPatchTestCasesRQ batchPatchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(singleTestCaseId)
        .testFolderId(newFolderId)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(batchPatchRequest);

    // When
    mockMvc.perform(patch("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/patch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // Then
    Optional<TmsTestCase> testCaseAfter = testCaseRepository.findById(12L);
    assertTrue(testCaseAfter.isPresent());
    assertEquals(newFolderId, testCaseAfter.get().getTestFolder().getId());
  }

  @Test
  void deleteTestCasesWithMalformedJsonIntegrationTest() throws Exception {
    // Given - malformed JSON
    String malformedJson = "{\"locationIds\": [1, 2, 3";

    // When/Then - should return bad request due to malformed JSON
    mockMvc.perform(delete("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/delete")
            .contentType("application/json")
            .content(malformedJson)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void batchPatchTestCasesWithMalformedJsonIntegrationTest() throws Exception {
    // Given - malformed JSON
    String malformedJson = "{\"locationIds\": [1, 2], \"testFolderId\": 6";

    // When/Then - should return bad request due to malformed JSON
    mockMvc.perform(patch("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/patch")
            .contentType("application/json")
            .content(malformedJson)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }
}
