package com.epam.ta.reportportal.core.tms.controller.integration;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseVersion;
import com.epam.ta.reportportal.core.tms.db.repository.TmsManualScenarioAttributeRepository;
import com.epam.ta.reportportal.core.tms.db.repository.TmsManualScenarioRepository;
import com.epam.ta.reportportal.core.tms.db.repository.TmsStepRepository;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestCaseAttributeRepository;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestCaseRepository;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestCaseVersionRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseDefaultVersionRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
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
import org.springframework.mock.web.MockMultipartFile;
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

  @Autowired
  private TmsTestCaseVersionRepository testCaseVersionRepository;

  @Autowired
  private TmsManualScenarioRepository manualScenarioRepository;

  @Autowired
  private TmsStepRepository stepRepository;

  @Autowired
  private TmsTestCaseAttributeRepository testCaseAttributeRepository;

  @Autowired
  private TmsManualScenarioAttributeRepository manualScenarioAttributeRepository;

  @Test
  void createTestCaseWithoutDefaultVersionIntegrationTest() throws Exception {
    // Given
    TmsTestCaseAttributeRQ attribute = new TmsTestCaseAttributeRQ();
    attribute.setValue("value3");
    attribute.setAttributeId(3L);

    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Test Case 3");
    testCaseRQ.setDescription("Description for test case 3");
    testCaseRQ.setTestFolder(TmsTestCaseTestFolderRQ.builder().id(3L).build());
    testCaseRQ.setTags(List.of(attribute));

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(post("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void createTestCaseWithDefaultVersionIntegrationTest() throws Exception {
    // Given
    var manualScenarioRQ = new TmsTextManualScenarioRQ();
    manualScenarioRQ.setManualScenarioType(TmsManualScenarioRQ.TmsManualScenarioType.TEXT);
    manualScenarioRQ.setExpectedResult("Expected result");

    var defaultVersionRQ = new TmsTestCaseDefaultVersionRQ();
    defaultVersionRQ.setName("Default Version");
    defaultVersionRQ.setManualScenario(manualScenarioRQ);

    var testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Test Case With Version");
    testCaseRQ.setDescription("Description for test case with version");
    testCaseRQ.setTestFolder(TmsTestCaseTestFolderRQ.builder().id(3L).build());
    testCaseRQ.setDefaultVersion(defaultVersionRQ);

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(post("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Case With Version"));

    // Then - Verify database state
    var createdTestCase = testCaseRepository.findAll().stream()
        .filter(tc -> tc.getName().equals("Test Case With Version"))
        .findFirst();

    assertTrue(createdTestCase.isPresent());
    assertFalse(createdTestCase.get().getVersions().isEmpty());

    var defaultVersion = createdTestCase.get().getVersions().stream()
        .filter(TmsTestCaseVersion::isDefault)
        .findFirst();

    assertTrue(defaultVersion.isPresent());
    assertEquals("Default Version", defaultVersion.get().getName());
    assertNotNull(defaultVersion.get().getManualScenario());
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
  void getTestCasesBySearchCriteriaIntegrationTest() throws Exception {
    // When/Then - Search by name
    mockMvc.perform(get("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("search", "Search")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  void getTestCasesByFolderIdCriteriaIntegrationTest() throws Exception {
    // When/Then - Filter by folder
    mockMvc.perform(get("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("testFolderId", "7")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(4)); // 4 test cases in folder 7
  }

  @Test
  void getTestCasesByFolderIdFullTestSearchIntegrationTest() throws Exception {
    mockMvc.perform(get("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("testFolderId", "8")
            .param("search", "Test for full-text search")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(3)); // 3 test cases in folder 8

    mockMvc.perform(get("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("testFolderId", "8")
            .param("search", "LOW")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(
            1)); // 1 test case in folder 8 with priority == LOW

    mockMvc.perform(get("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("testFolderId", "8")
            .param("search", "3test")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(
            jsonPath("$.content.length()").value(2)); // 2 test cases in folder 8 with name == '3test'
  }

  @Test
  void getTestCasesByMultipleCriteriaIntegrationTest() throws Exception {
    // When/Then - Search by name and filter by folder
    mockMvc.perform(get("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("search", "Login")
            .param("testFolderId", "7")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
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
    testCaseRQ.setTestFolder(TmsTestCaseTestFolderRQ.builder().id(5L).build());
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
    assertEquals(testCaseRQ.getTestFolder().getId(),
        testCase.get().getTestFolder().getId());
  }

  @Test
  void updateTestCaseWithDefaultVersionIntegrationTest() throws Exception {
    // Given
    var manualScenarioRQ = new TmsTextManualScenarioRQ();
    manualScenarioRQ.setManualScenarioType(TmsManualScenarioRQ.TmsManualScenarioType.TEXT);
    manualScenarioRQ.setExpectedResult("Updated expected result");

    var defaultVersionRQ = new TmsTestCaseDefaultVersionRQ();
    defaultVersionRQ.setName("Updated Default Version");
    defaultVersionRQ.setManualScenario(manualScenarioRQ);

    var testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Updated Test Case 17");
    testCaseRQ.setDescription("Updated description for test case 17");
    testCaseRQ.setTestFolder(TmsTestCaseTestFolderRQ.builder().id(6L).build());
    testCaseRQ.setDefaultVersion(defaultVersionRQ);

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(put("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/17")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated Test Case 17"));

    // Then
    Optional<TmsTestCase> testCase = testCaseRepository.findById(17L);
    assertTrue(testCase.isPresent());
    assertEquals("Updated Test Case 17", testCase.get().getName());
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
    testCaseRQ.setTestFolder(TmsTestCaseTestFolderRQ.builder().id(6L).build());
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
    assertEquals(testCaseRQ.getTestFolder().getId(),
        testCase.get().getTestFolder().getId());
  }

  @Test
  void deleteTestCaseWithAllRelatedEntitiesIntegrationTest() throws Exception {
    // Given - Test case 17 has versions, manual scenarios, and steps
    var testCaseId = 17L;

    // When
    mockMvc.perform(delete("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/" + testCaseId)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNoContent());

    // Then - Verify all related entities are deleted
    assertTrue(testCaseRepository.findById(testCaseId).isEmpty());
    assertTrue(testCaseVersionRepository.findAll().stream()
        .filter(v -> v.getTestCase().getId().equals(testCaseId)).toList().isEmpty());
    assertTrue(manualScenarioRepository.findAll().stream()
        .filter(ms -> ms.getTestCaseVersion().getTestCase().getId().equals(testCaseId)).toList()
        .isEmpty());
    assertTrue(stepRepository.findAll().stream()
        .filter(s -> s.getManualScenario().getTestCaseVersion().getTestCase().getId()
            .equals(testCaseId)).toList().isEmpty());
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
    mockMvc.perform(delete("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
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
    mockMvc.perform(patch("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
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
  void importTestCasesFromJsonIntegrationTest() throws Exception {
    // Given
    var jsonContent = """
        [
          {
            "name": "Imported Test Case 1",
            "description": "Description for imported test case 1",
            "priority": 1,
            "testFolder": {
              "id": 3
            }
          },
          {
            "name": "Imported Test Case 2",
            "description": "Description for imported test case 2",
            "priority": 2,
            "testFolder": {
              "id": 4
            }
          }
        ]
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "testcases.json",
        "application/json",
        jsonContent.getBytes()
    );

    // When
    mockMvc.perform(multipart("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/import")
            .file(file)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Imported Test Case 1"))
        .andExpect(jsonPath("$[1].name").value("Imported Test Case 2"));

    // Then - Verify test cases are created in database
    var importedTestCases = testCaseRepository.findAll().stream()
        .filter(tc -> tc.getName().startsWith("Imported Test Case"))
        .toList();

    assertEquals(2, importedTestCases.size());
  }

  @Test
  void importTestCasesFromCsvIntegrationTest() throws Exception {
    // Given
    var csvContent = """
        name,description,testFolder,priority
        CSV Test Case 1,Description for CSV test case 1,Test Folder 3,1
        CSV Test Case 2,Description for CSV test case 2,Test Folder 4,2
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "testcases.csv",
        "text/csv",
        csvContent.getBytes()
    );

    // When
    mockMvc.perform(multipart("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/import")
            .file(file)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("CSV Test Case 1"))
        .andExpect(jsonPath("$[1].name").value("CSV Test Case 2"));

    // Then - Verify test cases are created in database
    var importedTestCases = testCaseRepository.findAll().stream()
        .filter(tc -> tc.getName().startsWith("CSV Test Case"))
        .toList();

    assertEquals(2, importedTestCases.size());
  }

  @Test
  void exportTestCasesToJsonIntegrationTest() throws Exception {
    // When/Then
    mockMvc.perform(get("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/export")
            .param("ids", "4,5,6")
            .param("format", "JSON")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(header().string("Content-Disposition",
            "attachment; filename=\"test_cases_export.json\""));
  }

  @Test
  void exportTestCasesToCsvIntegrationTest() throws Exception {
    // When/Then
    mockMvc.perform(get("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/export")
            .param("ids", "4,5")
            .param("format", "CSV")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(content().contentType("text/csv;charset=UTF-8"))
        .andExpect(header().string("Content-Disposition",
            "attachment; filename=\"test_cases_export.csv\""));
  }

  @Test
  void exportAllTestCasesIntegrationTest() throws Exception {
    // When/Then - Export all test cases (no ids parameter)
    mockMvc.perform(get("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/export")
            .param("format", "JSON")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"));
  }

  @Test
  void exportTestCasesWithAttachmentsIntegrationTest() throws Exception {
    // When/Then
    mockMvc.perform(get("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/export")
            .param("ids", "4,5")
            .param("format", "JSON")
            .param("includeAttachments", "true")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(header().string("Content-Disposition",
            "attachment; filename=\"test_cases_export_with_attachments.json\""));
  }

  // Error handling tests
  @Test
  void deleteTestCasesWithEmptyLocationIdsIntegrationTest() throws Exception {
    // Given
    BatchDeleteTestCasesRQ deleteRequest = BatchDeleteTestCasesRQ.builder()
        .testCaseIds(Collections.emptyList())
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(deleteRequest);

    // When/Then - should return bad request due to @NotEmpty validation
    mockMvc.perform(delete("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
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
    mockMvc.perform(delete("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
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

    var exception = assertThrows(jakarta.servlet.ServletException.class,
        () -> mockMvc.perform(
            patch("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken()))));

    assertThat(exception.getMessage()).contains("violates foreign key constraint");
  }

  @Test
  void batchPatchTestCasesWithNullFolderIdIntegrationTest() throws Exception {
    // Create request with null testFolderId - this should be handled by @NotNull validation
    String jsonContent = "{\"testCaseIds\": [9, 10], \"testFolderId\": null}";

    // When/Then - should fail validation due to @NotNull annotation
    mockMvc.perform(patch("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void importTestCasesWithUnsupportedFormatIntegrationTest() throws Exception {
    // Given
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "testcases.xml",
        "application/xml",
        "<testcases></testcases>".getBytes()
    );

    // When/Then - should return error for unsupported format
    var exception = assertThrows(jakarta.servlet.ServletException.class, () -> mockMvc.perform(
        multipart("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/import")
            .file(file)
            .with(token(oAuthHelper.getSuperadminToken()))));

    assertThat(exception.getMessage()).contains("Unsupported import format: xml");
  }

  @Test
  void exportTestCasesWithInvalidFormatIntegrationTest() throws Exception {
    // When/Then - should return error for unsupported format
    var exception = assertThrows(jakarta.servlet.ServletException.class, () -> mockMvc
        .perform(get("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/export")
            .param("format", "XML")
            .with(token(oAuthHelper.getSuperadminToken()))));

    assertThat(exception.getMessage()).contains("Unsupported export format: XML");
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
    mockMvc.perform(patch("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
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
    mockMvc.perform(delete("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
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
    mockMvc.perform(patch("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
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
    mockMvc.perform(delete("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
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
    mockMvc.perform(patch("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
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
    mockMvc.perform(delete("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
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
    mockMvc.perform(patch("/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
            .contentType("application/json")
            .content(malformedJson)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }
}
