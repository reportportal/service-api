package com.epam.ta.reportportal.core.tms.controller.integration;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.epam.ta.reportportal.core.tms.db.repository.TmsStepsManualScenarioRepository;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestCaseAttributeRepository;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestCaseRepository;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestCaseVersionRepository;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTextManualScenarioRepository;
import com.epam.ta.reportportal.core.tms.dto.DeleteTagsRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioStepRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioType;
import com.epam.ta.reportportal.core.tms.dto.TmsStepsManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDeleteTagsRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDeleteTestCasesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchPatchTestCasesRQ;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
  private static final String DEFAULT_PROJECT_KEY = "default_personal";

  @Autowired
  private TmsTestCaseRepository testCaseRepository;

  @Autowired
  private TmsTestCaseVersionRepository testCaseVersionRepository;

  @Autowired
  private TmsManualScenarioRepository manualScenarioRepository;

  @Autowired
  private TmsTextManualScenarioRepository textManualScenarioRepository;

  @Autowired
  private TmsStepsManualScenarioRepository stepsManualScenarioRepository;

  @Autowired
  private TmsStepRepository stepRepository;

  @Autowired
  private TmsTestCaseAttributeRepository testCaseAttributeRepository;

  @Autowired
  private TmsManualScenarioAttributeRepository manualScenarioAttributeRepository;

  @PersistenceContext
  private EntityManager entityManager;

  @Test
  void createTestCaseWithoutManualScenarioIntegrationTest() throws Exception {
    // Given
    var attribute = new TmsAttributeRQ();
    attribute.setValue("value3");
    attribute.setId(3L);

    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Test Case 3");
    testCaseRQ.setDescription("Description for test case 3");
    testCaseRQ.setTestFolder(TmsTestCaseTestFolderRQ.builder().id(3L).build());
    testCaseRQ.setTags(List.of(attribute));

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void createTestCaseWithNonExistentFolderIdIntegrationTest() throws Exception {
    // Given
    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Test Case with Invalid Folder");
    testCaseRQ.setDescription("Description for test case with invalid folder");
    testCaseRQ.setTestFolder(TmsTestCaseTestFolderRQ.builder().id(999L).build()); // Non-existent folder

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When/Then - should return error for non-existent folder
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("Test Folder with id: 999")));
  }

  @Test
  void createTestCaseWithNewFolderNameIntegrationTest() throws Exception {
    // Given - test case with folder name instead of ID (should create new folder)
    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Test Case with New Folder");
    testCaseRQ.setDescription("Description for test case with new folder");
    testCaseRQ.setTestFolder(TmsTestCaseTestFolderRQ.builder().name("New Test Folder").build());

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Case with New Folder"));

    // Then - Verify new folder was created and assigned to test case
    var createdTestCase = testCaseRepository.findAll().stream()
        .filter(tc -> tc.getName().equals("Test Case with New Folder"))
        .findFirst();

    assertTrue(createdTestCase.isPresent());
    assertNotNull(createdTestCase.get().getTestFolder());
  }

  @Test
  void createTextTestCaseWithManualScenarioIntegrationTest() throws Exception {
    // Given
    var manualScenarioRQ = TmsTextManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.TEXT)
        .instructions("Test instructions")
        .expectedResult("Expected result")
        .build();

    var testCaseRQ = TmsTestCaseRQ.builder()
        .name("Test Case With Text Scenario")
        .description("Description for test case with text scenario")
        .testFolder(TmsTestCaseTestFolderRQ.builder().id(3L).build())
        .manualScenario(manualScenarioRQ)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Case With Text Scenario"))
        .andExpect(jsonPath("$.manualScenario").exists())
        .andExpect(jsonPath("$.manualScenario.manualScenarioType").value("TEXT"));

    // Then - Verify database state
    var createdTestCase = testCaseRepository.findAll().stream()
        .filter(tc -> tc.getName().equals("Test Case With Text Scenario"))
        .findFirst();

    assertTrue(createdTestCase.isPresent());

    if (!createdTestCase.get().getVersions().isEmpty()) {
      var defaultVersion = createdTestCase.get().getVersions().stream()
          .filter(TmsTestCaseVersion::isDefault)
          .findFirst();

      assertTrue(defaultVersion.isPresent());
      assertNotNull(defaultVersion.get().getManualScenario());

      // Verify text scenario is created
      var manualScenario = defaultVersion.get().getManualScenario();
      assertNotNull(manualScenario.getTextScenario());
      assertEquals("Expected result", manualScenario.getTextScenario().getExpectedResult());
    }
  }

  @Test
  void createStepsTestCaseWithManualScenarioIntegrationTest() throws Exception {
    // Given
    var firstStep = TmsManualScenarioStepRQ.builder()
        .instructions("Instructions 1")
        .expectedResult("Expected result 1")
        .build();
    var secondStep = TmsManualScenarioStepRQ.builder()
        .instructions("Instructions 2")
        .expectedResult("Expected result 2")
        .build();
    var manualScenarioRQ = TmsStepsManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.STEPS)
        .steps(List.of(firstStep, secondStep))
        .build();

    var testCaseRQ = TmsTestCaseRQ.builder()
        .name("Test Case With Steps Scenario")
        .description("Description for test case with steps scenario")
        .testFolder(TmsTestCaseTestFolderRQ.builder().id(3L).build())
        .manualScenario(manualScenarioRQ)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Case With Steps Scenario"))
        .andExpect(jsonPath("$.manualScenario").exists())
        .andExpect(jsonPath("$.manualScenario.manualScenarioType").value("STEPS"));

    // Then - Verify database state
    var createdTestCase = testCaseRepository.findAll().stream()
        .filter(tc -> tc.getName().equals("Test Case With Steps Scenario"))
        .findFirst();

    assertTrue(createdTestCase.isPresent());

    // If versions are still created automatically, check the default version
    if (!createdTestCase.get().getVersions().isEmpty()) {
      var defaultVersion = createdTestCase.get().getVersions().stream()
          .filter(TmsTestCaseVersion::isDefault)
          .findFirst();

      assertTrue(defaultVersion.isPresent());
      assertNotNull(defaultVersion.get().getManualScenario());

      var manualScenario = defaultVersion.get().getManualScenario();

      // Verify steps scenario is created
      assertNotNull(manualScenario.getStepsScenario());
      assertThat(manualScenario.getStepsScenario().getSteps())
          .isNotNull()
          .isNotEmpty()
          .hasSize(2);
    }
  }

  @Test
  void createStepsTestCaseWithManualScenarioInProjectDefaultIntegrationTest() throws Exception {
    // Given
    var firstStep = TmsManualScenarioStepRQ.builder()
        .instructions("Instructions 1")
        .expectedResult("Expected result 1")
        .build();
    var secondStep = TmsManualScenarioStepRQ.builder()
        .instructions("Instructions 2")
        .expectedResult("Expected result 2")
        .build();
    var manualScenarioRQ = TmsStepsManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.STEPS)
        .steps(List.of(firstStep, secondStep))
        .build();

    var testCaseRQ = TmsTestCaseRQ.builder()
        .name("Test Case With Steps Scenario for default project")
        .description("Description for test case with steps scenario for default project")
        .testFolder(TmsTestCaseTestFolderRQ.builder().id(9L).build())
        .manualScenario(manualScenarioRQ)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(post("/v1/project/" + DEFAULT_PROJECT_KEY + "/tms/test-case")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Case With Steps Scenario for default project"));

    // Then - Verify database state
    var createdTestCase = testCaseRepository.findAll().stream()
        .filter(tc -> tc.getName().equals("Test Case With Steps Scenario for default project"))
        .findFirst();

    assertTrue(createdTestCase.isPresent());
    assertNotNull(createdTestCase.get().getTestFolder());

    var testFolder = createdTestCase.get().getTestFolder();
    assertEquals(2, testFolder.getProject().getId());

    // If versions are still created automatically, check the default version
    if (!createdTestCase.get().getVersions().isEmpty()) {
      var defaultVersion = createdTestCase.get().getVersions().stream()
          .filter(TmsTestCaseVersion::isDefault)
          .findFirst();

      assertTrue(defaultVersion.isPresent());
      assertNotNull(defaultVersion.get().getManualScenario());

      var manualScenario = defaultVersion.get().getManualScenario();

      // Verify steps scenario is created
      assertNotNull(manualScenario.getStepsScenario());
      assertThat(manualScenario.getStepsScenario().getSteps())
          .isNotNull()
          .isNotEmpty()
          .hasSize(2);
    }
  }

  @Test
  void createStepsTestCaseWithManualScenarioWithoutStepsIntegrationTest() throws Exception {
    // Given
    var manualScenarioRQ = TmsStepsManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.STEPS)
        .build();

    var testCaseRQ = TmsTestCaseRQ.builder()
        .name("Test Case With Steps Scenario without steps")
        .description("Description for test case with steps scenario")
        .testFolder(TmsTestCaseTestFolderRQ.builder().id(3L).build())
        .manualScenario(manualScenarioRQ)
        .priority("HIGH")
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Case With Steps Scenario without steps"));

    // Then - Verify database state
    var createdTestCase = testCaseRepository.findAll().stream()
        .filter(tc -> tc.getName().equals("Test Case With Steps Scenario without steps"))
        .findFirst();

    assertTrue(createdTestCase.isPresent());

    // If versions are still created automatically, check the default version
    if (!createdTestCase.get().getVersions().isEmpty()) {
      var defaultVersion = createdTestCase.get().getVersions().stream()
          .filter(TmsTestCaseVersion::isDefault)
          .findFirst();

      assertTrue(defaultVersion.isPresent());
      assertNotNull(defaultVersion.get().getManualScenario());

      var manualScenario = defaultVersion.get().getManualScenario();

      // Verify steps scenario is created but without steps
      assertNotNull(manualScenario.getStepsScenario());
      assertThat(manualScenario.getStepsScenario().getSteps()).isNullOrEmpty();
    }
  }

  @Test
  void getTestCaseByIdIntegrationTest() throws Exception {
    // Given
    Optional<TmsTestCase> testCase = testCaseRepository.findById(4L);

    // When/Then
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/4")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(testCase.get().getId()))
        .andExpect(jsonPath("$.name").value(testCase.get().getName()))
        .andExpect(jsonPath("$.description").value(testCase.get().getDescription()));
  }

  @Test
  void getTestCasesBySearchCriteriaIntegrationTest() throws Exception {
    // When/Then - Search by name
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("search", "Search")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  void getTestCasesByFolderIdCriteriaIntegrationTest() throws Exception {
    // When/Then - Filter by folder
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("testFolderId", "7")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(5)); // 5 test cases in folder 7
  }

  @Test
  void getTestCasesByFolderIdFullTestSearchIntegrationTest() throws Exception {
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("testFolderId", "8")
            .param("search", "Test for full-text search")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2)); // 2 such test cases in folder 8

    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("testFolderId", "8")
            .param("search", "LOW")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(
            1)); // 1 test case in folder 8 with priority == LOW

    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("testFolderId", "8")
            .param("search", "3test")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(
            jsonPath("$.content.length()").value(
                1)); // 1 test cases with name == '3test' in folder 8
  }

  @Test
  void getTestCasesByMultipleCriteriaIntegrationTest() throws Exception {
    // When/Then - Search by name and filter by folder
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("search", "Login")
            .param("testFolderId", "7")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  void updateTestCaseIntegrationTest() throws Exception {
    // Given
    var attribute = new TmsAttributeRQ();
    attribute.setValue("value4");
    attribute.setId(4L);

    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Updated Test Case 5");
    testCaseRQ.setDescription("Updated description for test case 5");
    testCaseRQ.setTestFolder(TmsTestCaseTestFolderRQ.builder().id(5L).build());
    testCaseRQ.setTags(List.of(attribute));

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(put("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/5")
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
  void updateTestCaseWithNonExistentFolderIntegrationTest() throws Exception {
    // Given
    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Updated Test Case with Invalid Folder");
    testCaseRQ.setDescription("Updated description");
    testCaseRQ.setTestFolder(TmsTestCaseTestFolderRQ.builder().id(999L).build()); // Non-existent folder

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When/Then - should return error for non-existent folder
    mockMvc.perform(put("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/5")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("Test Folder with id: 999")));
  }

  @Test
  void updateTextTestCaseWithManualScenarioIntegrationTest() throws Exception {
    // Given
    var manualScenarioRQ = TmsTextManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.TEXT)
        .executionEstimationTime(123)
        .linkToRequirements("http://requirements.com")
        .instructions("Updated instructions")
        .expectedResult("Updated expected result")
        .build();

    var testCaseRQ = TmsTestCaseRQ.builder()
        .name("Updated Test Case 17")
        .description("Updated description for test case 17")
        .testFolder(TmsTestCaseTestFolderRQ.builder().id(7L).build())
        .manualScenario(manualScenarioRQ)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(put("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/17")
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
    var attribute = new TmsAttributeRQ();
    attribute.setValue("value6");
    attribute.setId(6L);
    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Patched Test Case 6");
    testCaseRQ.setDescription("Patched description for test case 6");
    testCaseRQ.setTestFolder(TmsTestCaseTestFolderRQ.builder().id(6L).build());
    testCaseRQ.setTags(List.of(attribute));

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/6")
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
  void patchTestCaseWithNonExistentFolderIntegrationTest() throws Exception {
    // Given
    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Patched Test Case with Invalid Folder");
    testCaseRQ.setTestFolder(TmsTestCaseTestFolderRQ.builder().id(999L).build()); // Non-existent folder

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When/Then - should return error for non-existent folder
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/6")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("Test Folder with id: 999")));
  }

  @Test
  void deleteTestCaseWithAllRelatedEntitiesIntegrationTest() throws Exception {
    // Given - Test case 34 has versions, manual scenarios, and steps
    var testCaseId = 34L;

    // When
    mockMvc
        .perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/" + testCaseId)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNoContent());

    // Then - Verify all related entities are deleted
    assertTrue(testCaseRepository.findById(testCaseId).isEmpty());
    assertTrue(testCaseVersionRepository.findAll().stream()
        .filter(v -> v.getTestCase().getId().equals(testCaseId)).toList().isEmpty());
    assertTrue(manualScenarioRepository.findAll().stream()
        .filter(ms -> ms.getTestCaseVersion().getTestCase().getId().equals(testCaseId)).toList()
        .isEmpty());
    assertTrue(textManualScenarioRepository.findAll().stream()
        .filter(tms -> tms.getManualScenario().getTestCaseVersion().getTestCase().getId()
            .equals(testCaseId)).toList().isEmpty());
    assertTrue(stepsManualScenarioRepository.findAll().stream()
        .filter(sms -> sms.getManualScenario().getTestCaseVersion().getTestCase().getId()
            .equals(testCaseId)).toList().isEmpty());
    assertTrue(stepRepository.findAll().stream()
        .filter(s -> s.getStepsManualScenario().getManualScenario().getTestCaseVersion().getTestCase().getId()
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
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
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
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
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
  void batchPatchTestCasesWithPriorityIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = List.of(13L, 14L);
    String newPriority = "HIGH";

    BatchPatchTestCasesRQ batchPatchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .priority(newPriority)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(batchPatchRequest);

    // When
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // Then
    Optional<TmsTestCase> testCase13After = testCaseRepository.findById(13L);
    Optional<TmsTestCase> testCase14After = testCaseRepository.findById(14L);

    assertTrue(testCase13After.isPresent());
    assertTrue(testCase14After.isPresent());
    assertEquals(newPriority, testCase13After.get().getPriority());
    assertEquals(newPriority, testCase14After.get().getPriority());
  }

  @Test
  void batchPatchTestCasesWithTagsIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = List.of(15L, 16L);
    var attributeValue1 = "batch-tag-1";
    var attributeValue2 = "batch-tag-2";

    var attribute1 = new TmsAttributeRQ();
    attribute1.setValue(attributeValue1);
    attribute1.setId(1L);

    var attribute2 = new TmsAttributeRQ();
    attribute2.setValue(attributeValue2);
    attribute2.setId(2L);

    List<TmsAttributeRQ> tags = List.of(attribute1, attribute2);

    BatchPatchTestCasesRQ batchPatchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .tags(tags)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(batchPatchRequest);

    // When
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // Then - Check that tags are added to both test cases
    Optional<TmsTestCase> testCase15After = testCaseRepository.findById(15L);
    Optional<TmsTestCase> testCase16After = testCaseRepository.findById(16L);

    assertTrue(testCase15After.isPresent());
    assertTrue(testCase16After.isPresent());

    // Check that the tags were added to the test cases
    var testCase15Tags = testCaseAttributeRepository.findAllById_TestCaseId(15L);
    var testCase16Tags = testCaseAttributeRepository.findAllById_TestCaseId(16L);

    assertFalse(testCase15Tags.isEmpty());
    assertFalse(testCase16Tags.isEmpty());

    // Verify specific tag values are present
    assertTrue(testCase15Tags.stream().anyMatch(tag -> tag.getValue().equals(attributeValue1)));
    assertTrue(testCase15Tags.stream().anyMatch(tag -> tag.getValue().equals(attributeValue2)));
    assertTrue(testCase16Tags.stream().anyMatch(tag -> tag.getValue().equals(attributeValue1)));
    assertTrue(testCase16Tags.stream().anyMatch(tag -> tag.getValue().equals(attributeValue2)));
  }

  @Test
  void batchPatchTestCasesWithAllFieldsIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = List.of(30L, 31L);
    Long newFolderId = 5L;
    String newPriority = "HIGH";
    String tagValue = "comprehensive-tag";

    var attribute = new TmsAttributeRQ();
    attribute.setValue(tagValue);
    attribute.setId(3L);

    List<TmsAttributeRQ> tags = List.of(attribute);

    BatchPatchTestCasesRQ batchPatchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(newFolderId)
        .priority(newPriority)
        .tags(tags)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(batchPatchRequest);

    // When
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    entityManager.clear();

    // Then
    Optional<TmsTestCase> testCase30After = testCaseRepository.findById(30L);
    Optional<TmsTestCase> testCase31After = testCaseRepository.findById(31L);

    assertTrue(testCase30After.isPresent());
    assertTrue(testCase31After.isPresent());

    // Check folder update
    assertEquals(newFolderId, testCase30After.get().getTestFolder().getId());
    assertEquals(newFolderId, testCase31After.get().getTestFolder().getId());

    // Check priority update
    assertEquals(newPriority, testCase30After.get().getPriority());
    assertEquals(newPriority, testCase31After.get().getPriority());

    // Check tags update
    var testCase30Tags = testCaseAttributeRepository.findAllById_TestCaseId(30L);
    var testCase31Tags = testCaseAttributeRepository.findAllById_TestCaseId(31L);

    assertFalse(testCase30Tags.isEmpty());
    assertFalse(testCase31Tags.isEmpty());

    assertTrue(testCase30Tags.stream().anyMatch(tag -> tag.getValue().equals(tagValue)));
    assertTrue(testCase31Tags.stream().anyMatch(tag -> tag.getValue().equals(tagValue)));
  }

  @Test
  void batchPatchTestCasesWithEmptyTagsIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = List.of(20L, 21L);
    Long newFolderId = 4L;

    BatchPatchTestCasesRQ batchPatchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(newFolderId)
        .tags(Collections.emptyList())
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(batchPatchRequest);

    // When
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // Then - Only folder should be updated, no tags should be added
    Optional<TmsTestCase> testCase20After = testCaseRepository.findById(20L);
    Optional<TmsTestCase> testCase21After = testCaseRepository.findById(21L);

    assertTrue(testCase20After.isPresent());
    assertTrue(testCase21After.isPresent());
    assertEquals(newFolderId, testCase20After.get().getTestFolder().getId());
    assertEquals(newFolderId, testCase21After.get().getTestFolder().getId());
  }

  @Test
  void batchPatchTestCasesWithNullTagsIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = List.of(22L, 23L);
    String newPriority = "LOW";

    BatchPatchTestCasesRQ batchPatchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .priority(newPriority)
        .tags(null)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(batchPatchRequest);

    // When
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // Then - Only priority should be updated, no tags should be added
    Optional<TmsTestCase> testCase22After = testCaseRepository.findById(22L);
    Optional<TmsTestCase> testCase23After = testCaseRepository.findById(23L);

    assertTrue(testCase22After.isPresent());
    assertTrue(testCase23After.isPresent());
    assertEquals(newPriority, testCase22After.get().getPriority());
    assertEquals(newPriority, testCase23After.get().getPriority());
  }

  @Test
  void batchPatchTestCasesWithOnlyTagsIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = List.of(24L, 25L);
    var tagValue = "only-tags-test";

    var attribute = new TmsAttributeRQ();
    attribute.setValue(tagValue);
    attribute.setId(4L);

    List<TmsAttributeRQ> tags = List.of(attribute);

    BatchPatchTestCasesRQ batchPatchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .tags(tags)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(batchPatchRequest);

    // When
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // Then - Only tags should be added
    var testCase24Tags = testCaseAttributeRepository.findAllById_TestCaseId(24L);
    var testCase25Tags = testCaseAttributeRepository.findAllById_TestCaseId(25L);

    assertFalse(testCase24Tags.isEmpty());
    assertFalse(testCase25Tags.isEmpty());

    assertTrue(testCase24Tags.stream().anyMatch(tag -> tag.getValue().equals(tagValue)));
    assertTrue(testCase25Tags.stream().anyMatch(tag -> tag.getValue().equals(tagValue)));
  }

  @Test
  void batchPatchTestCasesWithAllNullFieldsIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = List.of(26L, 27L);

    BatchPatchTestCasesRQ batchPatchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(null)
        .priority(null)
        .tags(null)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(batchPatchRequest);

    // When
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void batchPatchTestCasesWithInvalidTagAttributeIdIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = List.of(28L, 29L);

    var attribute = new TmsAttributeRQ();
    attribute.setValue("invalid-attribute-id");
    attribute.setId(999L); // Non-existent attribute ID

    List<TmsAttributeRQ> tags = List.of(attribute);

    BatchPatchTestCasesRQ batchPatchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .tags(tags)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(batchPatchRequest);

    // When/Then - Should fail due to foreign key constraint
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(
            content().string(containsString(
                "TMS Attributes with IDs [999]' not found"
            ))
        );
  }

  @Test
  void batchPatchTestCasesWithNonExistentFolderIdIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = List.of(32L, 33L);
    Long nonExistentFolderId = 999L;

    BatchPatchTestCasesRQ batchPatchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(nonExistentFolderId)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(batchPatchRequest);

    // When/Then - should return error for non-existent folder
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("'Test Folder with id: 999 for project: 1' not found")));
  }

  @Test
  void importTestCasesFromJsonIntegrationTest() throws Exception {
    // Given
    var jsonContent = """
        [
          {
            "name": "Imported Test Case 1",
            "description": "Description for imported test case 1",
            "priority": "HIGH",
            "externalId": "123"
          },
          {
            "name": "Imported Test Case 2",
            "description": "Description for imported test case 2",
            "priority": "MEDIUM",
            "externalId": "321"
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
    mockMvc.perform(multipart("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/import")
            .file(file)
            .param("testFolderId", String.valueOf(3L))
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
    assertEquals(3L, importedTestCases.getFirst().getTestFolder().getId());
    assertEquals(3L, importedTestCases.get(1).getTestFolder().getId());
  }

  @Test
  void importTestCasesFromJsonWithNonExistentFolderIntegrationTest() throws Exception {
    // Given
    var jsonContent = """
        [
          {
            "name": "Imported Test Case with Invalid Folder",
            "description": "Description",
            "priority": "HIGH",
            "externalId": "123"
          }
        ]
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "testcases.json",
        "application/json",
        jsonContent.getBytes()
    );

    // When/Then - should return error for non-existent folder
    mockMvc.perform(multipart("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/import")
            .file(file)
            .param("testFolderId", String.valueOf(999L))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("Test Folder with id: 999")));
  }

  @Test
  void importTestCasesFromJsonWithNewFolderNameIntegrationTest() throws Exception {
    // Given
    var jsonContent = """
        [
          {
            "name": "Imported Test Case with New Folder",
            "description": "Description for imported test case with new folder",
            "priority": "HIGH",
            "externalId": "123"
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
    mockMvc.perform(multipart("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/import")
            .file(file)
            .param("testFolderName", "New Import Folder")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("Imported Test Case with New Folder"));

    // Then - Verify test case is created in database with new folder
    var importedTestCases = testCaseRepository.findAll().stream()
        .filter(tc -> tc.getName().equals("Imported Test Case with New Folder"))
        .toList();

    assertEquals(1, importedTestCases.size());
    assertNotNull(importedTestCases.getFirst().getTestFolder());
  }

  @Test
  void importTestCasesFromCsvIntegrationTest() throws Exception {
    // Given
    var csvContent = """
        name,description,priority,externalId
        CSV Test Case 1,Description for CSV test case 1,HIGH,123
        CSV Test Case 2,Description for CSV test case 2,MEDIUM,321
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "testcases.csv",
        "text/csv",
        csvContent.getBytes()
    );

    // When
    mockMvc.perform(multipart("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/import")
            .file(file)
            .param("testFolderId", String.valueOf(4L))
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
    assertEquals(4L, importedTestCases.getFirst().getTestFolder().getId());
    assertEquals(4L, importedTestCases.get(1).getTestFolder().getId());
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
    mockMvc.perform(
            multipart("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/import")
                .file(file)
                .param("testFolderId", String.valueOf(3L))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest())
        .andExpect(
            content().string(containsString(
                "Unsupported import format: xml"
            ))
        );
  }

  @Test
  void importTestCasesWithInvalidTestFolderValidationIntegrationTest() throws Exception {
    // Given
    var jsonContent = """
        [
          {
            "name": "Test Case",
            "description": "Description",
            "priority": "HIGH",
            "externalId": "123"
          }
        ]
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "testcases.json",
        "application/json",
        jsonContent.getBytes()
    );

    // When/Then - should return validation error
    mockMvc.perform(multipart("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/import")
            .file(file)
            .param("testFolderId", String.valueOf(3L))
            .param("testFolderName", "Test Folder")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Either testFolderId or testFolderName must be provided and not empty")));
  }

  @Test
  void importTestCasesWithMissingTestFolderValidationIntegrationTest() throws Exception {
    // Given
    var jsonContent = """
        [
          {
            "name": "Test Case",
            "description": "Description", 
            "priority": "HIGH",
            "externalId": "123"
          }
        ]
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "testcases.json",
        "application/json",
        jsonContent.getBytes()
    );

    // When/Then - should return validation error
    mockMvc.perform(multipart("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/import")
            .file(file)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Either testFolderId or testFolderName must be provided and not empty")));
  }

  @Test
  void importTestCasesWithEmptyTestFolderNameValidationIntegrationTest() throws Exception {
    // Given
    var jsonContent = """
        [
          {
            "name": "Test Case",
            "description": "Description", 
            "priority": "HIGH",
            "externalId": "123"
          }
        ]
        """;

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "testcases.json",
        "application/json",
        jsonContent.getBytes()
    );

    // When/Then - should return validation error
    mockMvc.perform(multipart("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/import")
            .file(file)
            .param("testFolderId", "")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Either testFolderId or testFolderName must be provided and not empty")));
  }

  @Test
  void exportTestCasesToJsonIntegrationTest() throws Exception {
    // When/Then
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/export")
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
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/export")
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
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/export")
            .param("format", "JSON")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"));
  }

  @Test
  void exportTestCasesWithAttachmentsIntegrationTest() throws Exception {
    // When/Then
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/export")
            .param("ids", "4,5")
            .param("format", "JSON")
            .param("includeAttachments", "true")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(header().string("Content-Disposition",
            "attachment; filename=\"test_cases_export_with_attachments.json\""));
  }

  @Test
  void exportTestCasesWithInvalidFormatIntegrationTest() throws Exception {
    // When/Then - should return error for unsupported format
    mockMvc
        .perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/export")
            .param("format", "XML")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest())
        .andExpect(
            content().string(containsString(
                "Unsupported export format: XML"
            ))
        );
  }

  @Test
  void deleteTagsFromTestCaseIntegrationTest() throws Exception {
    // Given - test case 4 has attribute 4 with value 'test value 4'
    var testCaseId = 4L;
    var tagIdsToDelete = List.of(4L);

    // Verify tag exists before deletion
    var tagsBefore = testCaseAttributeRepository.findAllById_TestCaseId(testCaseId);
    assertTrue(tagsBefore.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(4L)));

    var deleteRequest = DeleteTagsRQ.builder()
        .tagIds(tagIdsToDelete)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(deleteRequest);

    // When
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/" + testCaseId + "/tags")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNoContent());

    // Then - Verify tag is deleted
    var tagsAfter = testCaseAttributeRepository.findAllById_TestCaseId(testCaseId);
    assertFalse(tagsAfter.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(4L)));
  }

  @Test
  void deleteTagsFromTestCaseWithMultipleTagsIntegrationTest() throws Exception {
    // Given - test case 9 has attribute 4, let's add another attribute for this test
    var testCaseId = 9L;

    // First verify existing tag
    var tagsBefore = testCaseAttributeRepository.findAllById_TestCaseId(testCaseId);
    assertTrue(tagsBefore.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(4L)));

    var tagIdsToDelete = List.of(4L); // Only delete attribute 4

    var deleteRequest = DeleteTagsRQ.builder()
        .tagIds(tagIdsToDelete)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(deleteRequest);

    // When
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/" + testCaseId + "/tags")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNoContent());

    // Then - Verify specific tag is deleted
    var tagsAfter = testCaseAttributeRepository.findAllById_TestCaseId(testCaseId);
    assertFalse(tagsAfter.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(4L)));
  }

  @Test
  void deleteTagsFromTestCaseWithNonExistentTagsIntegrationTest() throws Exception {
    // Given - try to delete tags that don't exist on this test case
    var testCaseId = 4L;
    var nonExistentTagIds = List.of(999L, 888L);

    var deleteRequest = DeleteTagsRQ.builder()
        .tagIds(nonExistentTagIds)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(deleteRequest);

    // When/Then - should succeed (no error for non-existent tags)
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/" + testCaseId + "/tags")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteTagsFromTestCaseWithEmptyTagListIntegrationTest() throws Exception {
    // Given
    var testCaseId = 5L;
    var emptyTagIds = Collections.<Long>emptyList();

    var deleteRequest = DeleteTagsRQ.builder()
        .tagIds(emptyTagIds)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(deleteRequest);

    // When/Then - should return validation error
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/" + testCaseId + "/tags")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void deleteTagsFromTestCaseWithNonExistentTestCaseIntegrationTest() throws Exception {
    // Given
    var nonExistentTestCaseId = 999L;
    var tagIds = List.of(1L, 2L);

    var deleteRequest = DeleteTagsRQ.builder()
        .tagIds(tagIds)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(deleteRequest);

    // When/Then - should return not found
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/" + nonExistentTestCaseId + "/tags")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void batchDeleteTagsFromTestCasesIntegrationTest() throws Exception {
    // Given - test cases 22 and 23 have existing tags
    var testCaseIds = List.of(22L, 23L);
    var tagIdsToDelete = List.of(1L, 2L);

    // Verify tags exist before deletion
    var tags22Before = testCaseAttributeRepository.findAllById_TestCaseId(22L);
    var tags23Before = testCaseAttributeRepository.findAllById_TestCaseId(23L);
    assertTrue(tags22Before.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(1L)));
    assertTrue(tags23Before.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(2L)));

    var deleteRequest = BatchDeleteTagsRQ.builder()
        .testCaseIds(testCaseIds)
        .tagIds(tagIdsToDelete)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(deleteRequest);

    // When
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/tags/batch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNoContent());

    // Then - Verify tags are deleted
    var tags22After = testCaseAttributeRepository.findAllById_TestCaseId(22L);
    var tags23After = testCaseAttributeRepository.findAllById_TestCaseId(23L);

    assertFalse(tags22After.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(1L)));
    assertFalse(tags23After.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(2L)));
  }

  @Test
  void batchDeleteTagsFromTestCasesWithSingleTestCaseIntegrationTest() throws Exception {
    // Given
    var testCaseIds = List.of(24L);
    var tagIdsToDelete = List.of(3L);

    var deleteRequest = BatchDeleteTagsRQ.builder()
        .testCaseIds(testCaseIds)
        .tagIds(tagIdsToDelete)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(deleteRequest);

    // When
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/tags/batch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNoContent());

    // Then - Verify tag is deleted
    var tagsAfter = testCaseAttributeRepository.findAllById_TestCaseId(24L);
    assertFalse(tagsAfter.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(3L)));
  }

  @Test
  void batchDeleteTagsFromTestCasesWithNonExistentTagsIntegrationTest() throws Exception {
    // Given
    var testCaseIds = List.of(22L, 23L);
    var nonExistentTagIds = List.of(999L, 888L);

    var deleteRequest = BatchDeleteTagsRQ.builder()
        .testCaseIds(testCaseIds)
        .tagIds(nonExistentTagIds)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(deleteRequest);

    // When/Then - should succeed (no error for non-existent tags)
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/tags/batch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNoContent());
  }

  @Test
  void batchDeleteTagsFromTestCasesWithEmptyTestCaseIdsIntegrationTest() throws Exception {
    // Given
    var emptyTestCaseIds = Collections.<Long>emptyList();
    var tagIds = List.of(1L, 2L);

    var deleteRequest = BatchDeleteTagsRQ.builder()
        .testCaseIds(emptyTestCaseIds)
        .tagIds(tagIds)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(deleteRequest);

    // When/Then - should return validation error
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/tags/batch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void batchDeleteTagsFromTestCasesWithEmptyTagIdsIntegrationTest() throws Exception {
    // Given
    var testCaseIds = List.of(22L, 23L);
    var emptyTagIds = Collections.<Long>emptyList();

    var deleteRequest = BatchDeleteTagsRQ.builder()
        .testCaseIds(testCaseIds)
        .tagIds(emptyTagIds)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(deleteRequest);

    // When/Then - should return validation error
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/tags/batch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void batchDeleteTagsFromTestCasesWithNullFieldsIntegrationTest() throws Exception {
    // Given - create request with null fields
    String jsonContent = "{\"testCaseIds\": null, \"tagIds\": null}";

    // When/Then - should return validation error
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/tags/batch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void batchDeleteTagsFromTestCasesWithMixedExistingAndNonExistentTestCasesIntegrationTest() throws Exception {
    // Given
    var testCaseIds = List.of(25L, 999L); // 25L exists, 999L doesn't
    var tagIds = List.of(1L);

    var deleteRequest = BatchDeleteTagsRQ.builder()
        .testCaseIds(testCaseIds)
        .tagIds(tagIds)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(deleteRequest);

    // When/Then - should fail with not found exception
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/tags/batch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("'Test Cases with ids: [999] for projectId: 1' not found")));
  }

  @Test
  void deleteTagsFromTestCaseWithMalformedJsonIntegrationTest() throws Exception {
    // Given
    var testCaseId = 4L;
    String malformedJson = "{\"tagIds\": [1, 2, 3";

    // When/Then - should return bad request
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/" + testCaseId + "/tags")
            .contentType("application/json")
            .content(malformedJson)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void batchDeleteTagsFromTestCasesWithMalformedJsonIntegrationTest() throws Exception {
    // Given
    String malformedJson = "{\"testCaseIds\": [1, 2], \"tagIds\": [1, 2";

    // When/Then - should return bad request
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/tags/batch")
            .contentType("application/json")
            .content(malformedJson)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
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
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
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
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
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

    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("'Test Folder with id: 999 for project: 1' not found")));
  }

  @Test
  void batchPatchTestCasesWithNullFolderIdIntegrationTest() throws Exception {
    // Create request with null testFolderId - this should be handled by @NotNull validation
    String jsonContent = "{\"testCaseIds\": [9, 10], \"testFolderId\": null}";

    // When/Then - should fail validation due to @NotNull annotation
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
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
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
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
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
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
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
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
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
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
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
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
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
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
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
            .contentType("application/json")
            .content(malformedJson)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getTestCasesByTestPlanIdCriteriaIntegrationTest() throws Exception {
    // When/Then - Filter by test plan
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("testPlanId", "1")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(7)); // 7 test cases in test plan 1 (4,5,6,13,14,15,16)
  }

  @Test
  void getTestCasesByTestPlanIdAndFolderIdCriteriaIntegrationTest() throws Exception {
    // When/Then - Filter by test plan and folder
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("testPlanId", "1")
            .param("testFolderId", "7")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(4)); // 4 test cases in test plan 1 and folder 7 (13,14,15,16)
  }

  @Test
  void getTestCasesByTestPlanIdAndSearchCriteriaIntegrationTest() throws Exception {
    // When/Then - Filter by test plan and search
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("testPlanId", "1")
            .param("search", "Test Case 5")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1)); // 1 test case with "Test Case 5" in name in test plan 1
  }

  @Test
  void getTestCasesByAllThreeCriteriaIntegrationTest() throws Exception {
    // When/Then - Filter by test plan, folder, and search
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("testPlanId", "1")
            .param("testFolderId", "7")
            .param("search", "Login")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1)); // 1 test case matching all criteria (15)
  }

  @Test
  void getTestCasesByNonExistentTestPlanIdIntegrationTest() throws Exception {
    // When/Then - Filter by non-existent test plan
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("testPlanId", "999")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0)); // 0 test cases for non-existent test plan
  }

  @Test
  void getTestCasesByTestPlanId2CriteriaIntegrationTest() throws Exception {
    // When/Then - Filter by test plan 2
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("testPlanId", "2")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(5)); // 5 test cases in test plan 2 (7,8,9,15,19)
  }

  @Test
  void getTestCasesByTestPlanId3CriteriaIntegrationTest() throws Exception {
    // When/Then - Filter by test plan 3
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("testPlanId", "3")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(4)); // 4 test cases in test plan 3 (10,11,12,20)
  }

  @Test
  void getTestCasesByTestPlanIdWithComplexSearchIntegrationTest() throws Exception {
    // When/Then - Complex search with test plan filter
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("testPlanId", "4")
            .param("testFolderId", "7")
            .param("search", "Test")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  void getTestCasesByDifferentTestPlanAndFolderCombinationsIntegrationTest() throws Exception {
    // Test Plan 2 + Folder 8 should return test case 19
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("testPlanId", "2")
            .param("testFolderId", "8")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1)); // test case 19

    // Test Plan 3 + Folder 8 should return test case 20
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("testPlanId", "3")
            .param("testFolderId", "8")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1)); // test case 20
  }
}
