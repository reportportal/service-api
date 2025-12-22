package com.epam.reportportal.core.tms.controller.integration;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
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

import com.epam.reportportal.core.tms.dto.DeleteTagsRQ;
import com.epam.reportportal.core.tms.dto.NewTestFolderRQ;
import com.epam.reportportal.core.tms.dto.TmsManualScenarioAttachmentRQ;
import com.epam.reportportal.core.tms.dto.TmsManualScenarioPreconditionsRQ;
import com.epam.reportportal.core.tms.dto.TmsManualScenarioType;
import com.epam.reportportal.core.tms.dto.TmsStepRQ;
import com.epam.reportportal.core.tms.dto.TmsStepsManualScenarioRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseAttributeRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.reportportal.core.tms.dto.TmsTextManualScenarioRS;
import com.epam.reportportal.core.tms.dto.UploadAttachmentRS;
import com.epam.reportportal.core.tms.dto.batch.BatchDeleteTestCasesRQ;
import com.epam.reportportal.core.tms.dto.batch.BatchDuplicateTestCasesRQ;
import com.epam.reportportal.core.tms.dto.batch.BatchPatchTestCaseAttributesRQ;
import com.epam.reportportal.core.tms.dto.batch.BatchPatchTestCasesRQ;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsAttachmentRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsManualScenarioAttributeRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsManualScenarioRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsStepRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsStepsManualScenarioRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsTestCaseAttributeRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsTestCaseVersionRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsTestFolderRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsTextManualScenarioRepository;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCaseVersion;
import com.epam.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
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
@Disabled
public class TmsTestCaseIntegrationTest extends BaseMvcTest {

  private static final String SUPERADMIN_PROJECT_KEY = "superadmin_personal";
  private static final String DEFAULT_PROJECT_KEY = "default_personal";

  @Autowired
  private TmsTestCaseRepository testCaseRepository;

  @Autowired
  private TmsTestFolderRepository tmsTestFolderRepository;

  @Autowired
  private TmsTestCaseVersionRepository testCaseVersionRepository;

  @Autowired
  private TmsTestCaseVersionRepository tmsTestCaseVersionRepository;

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

  @Autowired
  private TmsAttachmentRepository tmsAttachmentRepository;

  @PersistenceContext
  private EntityManager entityManager;

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void createTestCaseWithoutManualScenarioIntegrationTest() throws Exception {
    // Given
    var attribute = new TmsTestCaseAttributeRQ();
    attribute.setValue("value3");
    attribute.setId(3L);

    var testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Test Case Without Manual Scenario");
    testCaseRQ.setDescription("Description for test case without manual scenario");
    testCaseRQ.setTestFolderId(3L);
    testCaseRQ.setAttributes(List.of(attribute));
    testCaseRQ.setPriority("MEDIUM");
    // No manualScenario specified

    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Case Without Manual Scenario"))
        .andExpect(
            jsonPath("$.description").value("Description for test case without manual scenario"))
        .andExpect(jsonPath("$.priority").value("MEDIUM"))
        .andExpect(jsonPath("$.manualScenario").doesNotExist()); // No manual scenario in response

    // Then - Verify test case is created with default version but without manual scenario
    var createdTestCase = testCaseRepository.findAll().stream()
        .filter(tc -> tc.getName().equals("Test Case Without Manual Scenario"))
        .findFirst();

    assertTrue(createdTestCase.isPresent());
    assertEquals(3L, createdTestCase.get().getTestFolder().getId());
    assertEquals("MEDIUM", createdTestCase.get().getPriority());

    // Verify default version is created
    assertFalse(createdTestCase.get().getVersions().isEmpty());

    var defaultVersion = createdTestCase.get().getVersions().stream()
        .filter(TmsTestCaseVersion::isDefault)
        .findFirst();

    assertTrue(defaultVersion.isPresent());

    // Verify no manual scenario is attached to the default version
    assertNull(defaultVersion.get().getManualScenario());

    // Verify test case has the correct attributes
    var testCaseTags = testCaseAttributeRepository.findAllById_TestCaseId(
        createdTestCase.get().getId());
    assertFalse(testCaseTags.isEmpty());
    assertTrue(testCaseTags.stream().anyMatch(tag ->
        tag.getValue().equals("value3") && tag.getId().getAttributeId().equals(3L)));
  }

  @Test
  void createTestCaseWithExistingFolderIdIntegrationTest() throws Exception {
    // Given
    var attribute = new TmsTestCaseAttributeRQ();
    attribute.setValue("value3");
    attribute.setId(3L);

    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Test Case with Existing Folder ID");
    testCaseRQ.setDescription("Description for test case with existing folder ID");
    testCaseRQ.setTestFolderId(3L); // Use existing folder by ID
    testCaseRQ.setAttributes(List.of(attribute));

    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Case with Existing Folder ID"));

    // Then - Verify test case is created with correct folder
    var createdTestCase = testCaseRepository.findAll().stream()
        .filter(tc -> tc.getName().equals("Test Case with Existing Folder ID"))
        .findFirst();

    assertTrue(createdTestCase.isPresent());
    assertEquals(3L, createdTestCase.get().getTestFolder().getId());
  }

  @Test
  void createTestCaseWithNonExistentFolderIdIntegrationTest() throws Exception {
    // Given
    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Test Case with Invalid Folder");
    testCaseRQ.setDescription("Description for test case with invalid folder");
    testCaseRQ.setTestFolderId(999L); // Non-existent folder ID

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
  void createTestCaseWithNewRootFolderIntegrationTest() throws Exception {
    // Given - test case with folder name instead of ID (should create new folder)
    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Test Case with New Folder");
    testCaseRQ.setDescription("Description for test case with new folder");
    testCaseRQ.setTestFolder(NewTestFolderRQ.builder().name("New Test Folder").build());

    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Case with New Folder"));

    entityManager.clear();

    // Then - Verify new folder was created and assigned to test case
    var createdTestCase = testCaseRepository.findAll().stream()
        .filter(tc -> tc.getName().equals("Test Case with New Folder"))
        .findFirst();

    assertTrue(createdTestCase.isPresent());
    assertNotNull(createdTestCase.get().getTestFolder());

    var testFolder = tmsTestFolderRepository.findById(
        createdTestCase.get().getTestFolder().getId());

    assertTrue(testFolder.isPresent());
    assertEquals("New Test Folder", testFolder.get().getName());
  }

  @Test
  void createTestCaseWithNewNestedFolderIntegrationTest() throws Exception {
    // Given - test case with new folder that has a parent
    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Test Case with Nested Folder");
    testCaseRQ.setDescription("Description for test case with nested folder");
    testCaseRQ.setTestFolder(NewTestFolderRQ.builder()
        .name("New Nested Folder")
        .parentTestFolderId(3L) // Parent folder ID
        .build());

    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Case with Nested Folder"));

    entityManager.clear();

    // Then - Verify new nested folder was created
    var createdTestCase = testCaseRepository.findAll().stream()
        .filter(tc -> tc.getName().equals("Test Case with Nested Folder"))
        .findFirst();

    assertTrue(createdTestCase.isPresent());
    assertNotNull(createdTestCase.get().getTestFolder());

    var testFolder = tmsTestFolderRepository.findById(
        createdTestCase.get().getTestFolder().getId());

    assertTrue(testFolder.isPresent());
    assertEquals("New Nested Folder", testFolder.get().getName());
    assertEquals(3L, testFolder.get().getParentTestFolder().getId());
  }

  @Test
  void createTestCaseWithoutFolderValidationIntegrationTest() throws Exception {
    // Given - test case without any folder information
    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Test Case without Folder");
    testCaseRQ.setDescription("Description for test case without folder");
    // No testFolderId or testFolder specified

    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When/Then - should return validation error
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(
            content().string(
                containsString("Validation failure")));
  }

  @Test
  void createTestCaseWithBothFolderOptionsValidationIntegrationTest() throws Exception {
    // Given - test case with both testFolderId and testFolder (should be validation error)
    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Test Case with Both Folder Options");
    testCaseRQ.setDescription("Description for test case with both folder options");
    testCaseRQ.setTestFolderId(3L);
    testCaseRQ.setTestFolder(NewTestFolderRQ.builder().name("New Folder").build());

    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When/Then - should return validation error
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(
            content().string(containsString(
                "Validation failure")));
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
        .testFolderId(3L)
        .manualScenario(manualScenarioRQ)
        .build();

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
    assertEquals(3L, createdTestCase.get().getTestFolder().getId());

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
    var firstStep = TmsStepRQ.builder()
        .instructions("Instructions 1")
        .expectedResult("Expected result 1")
        .build();
    var secondStep = TmsStepRQ.builder()
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
        .testFolderId(3L)
        .manualScenario(manualScenarioRQ)
        .build();

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
    assertEquals(3L, createdTestCase.get().getTestFolder().getId());

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
    var firstStep = TmsStepRQ.builder()
        .instructions("Instructions 1")
        .expectedResult("Expected result 1")
        .build();
    var secondStep = TmsStepRQ.builder()
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
        .testFolderId(9L)
        .manualScenario(manualScenarioRQ)
        .build();

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
    assertEquals(9L, createdTestCase.get().getTestFolder().getId());

    var testFolder = createdTestCase.get().getTestFolder();
    assertEquals(2, testFolder.getProject().getId());

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
        .testFolderId(3L)
        .manualScenario(manualScenarioRQ)
        .priority("HIGH")
        .build();

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
    assertEquals(3L, createdTestCase.get().getTestFolder().getId());

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
            .param("filter.eq.testFolderId", "7")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(5)); // 5 test cases in folder 7
  }

  @Test
  void getTestCasesByFolderIdFullTestSearchIntegrationTest() throws Exception {
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("filter.eq.testFolderId", "8")
            .param("filter.fts.search", "Test for full-text search")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2)); // 2 such test cases in folder 8

    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("filter.eq.testFolderId", "8")
            .param("filter.fts.search", "LOW")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(
            1)); // 1 test case in folder 8 with priority == LOW

    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("filter.eq.testFolderId", "8")
            .param("filter.fts.search", "3test")
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
  void updateTestCaseWithExistingFolderIdIntegrationTest() throws Exception {
    // Given
    var attribute = new TmsTestCaseAttributeRQ();
    attribute.setValue("value4");
    attribute.setId(4L);

    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Updated Test Case 5");
    testCaseRQ.setDescription("Updated description for test case 5");
    testCaseRQ.setTestFolderId(5L); // Use existing folder ID
    testCaseRQ.setAttributes(List.of(attribute));

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
    assertEquals(testCaseRQ.getTestFolderId(), testCase.get().getTestFolder().getId());
  }

  @Test
  void updateTestCaseWithNewFolderIntegrationTest() throws Exception {
    // Give

    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Updated Test Case 5 with New Folder");
    testCaseRQ.setDescription("Updated description for test case 5 with new folder");
    testCaseRQ.setTestFolder(NewTestFolderRQ.builder()
        .name("Updated New Folder")
        .build()); // Create new folder

    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(put("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/5")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(5L))
        .andExpect(jsonPath("$.name").value(testCaseRQ.getName()))
        .andExpect(jsonPath("$.description").value(testCaseRQ.getDescription()));
  }

  @Test
  void updateTestCaseWithNonExistentFolderIntegrationTest() throws Exception {
    // Given
    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Updated Test Case with Invalid Folder");
    testCaseRQ.setDescription("Updated description");
    testCaseRQ.setTestFolderId(999L); // Non-existent folder ID

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
        .testFolderId(7L)
        .manualScenario(manualScenarioRQ)
        .build();

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
    assertEquals(7L, testCase.get().getTestFolder().getId());
  }

  @Test
  void patchTestCaseWithExistingFolderIdIntegrationTest() throws Exception {
    // Given
    var attribute = new TmsTestCaseAttributeRQ();
    attribute.setValue("value6");
    attribute.setId(6L);
    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Patched Test Case 6");
    testCaseRQ.setDescription("Patched description for test case 6");
    testCaseRQ.setTestFolderId(6L);
    testCaseRQ.setAttributes(List.of(attribute));

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
    assertEquals(testCaseRQ.getTestFolderId(), testCase.get().getTestFolder().getId());
  }

  @Test
  void patchTestCaseWithNewFolderIntegrationTest() throws Exception {
    // Given
    var attribute = new TmsTestCaseAttributeRQ();
    attribute.setValue("value6");
    attribute.setId(6L);
    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Patched Test Case 6 with New Folder");
    testCaseRQ.setDescription("Patched description for test case 6 with new folder");
    testCaseRQ.setTestFolder(NewTestFolderRQ.builder()
        .name("Patched New Folder")
        .build());
    testCaseRQ.setAttributes(List.of(attribute));

    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/6")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(6L))
        .andExpect(jsonPath("$.name").value(testCaseRQ.getName()))
        .andExpect(jsonPath("$.description").value(testCaseRQ.getDescription()));

    entityManager.flush();

    // Then
    Optional<TmsTestCase> testCase = testCaseRepository.findById(6L);

    assertTrue(testCase.isPresent());

    var testFolder = tmsTestFolderRepository.findById(testCase.get().getTestFolder().getId());

    assertTrue(testFolder.isPresent());
    assertEquals("Patched New Folder", testFolder.get().getName());
  }

  @Test
  void patchTestCaseWithNonExistentFolderIntegrationTest() throws Exception {
    // Given
    TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Patched Test Case with Invalid Folder");
    testCaseRQ.setTestFolderId(999L); // Non-existent folder ID

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
        .filter(
            s -> s.getStepsManualScenario().getManualScenario().getTestCaseVersion().getTestCase()
                .getId()
                .equals(testCaseId)).toList().isEmpty());
  }

  @Test
  void deleteTestCasesIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = List.of(7L, 8L);
    BatchDeleteTestCasesRQ deleteRequest = BatchDeleteTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .build();

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
  void batchPatchTestCasesWithAllFieldsIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = List.of(30L, 31L);
    Long newFolderId = 5L;
    String newPriority = "HIGH";

    BatchPatchTestCasesRQ batchPatchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(newFolderId)
        .priority(newPriority)
        .build();

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
  }

  @Test
  void batchPatchTestCasesWithAllNullFieldsIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = List.of(26L, 27L);

    BatchPatchTestCasesRQ batchPatchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(null)
        .priority(null)
        .build();

    String jsonContent = mapper.writeValueAsString(batchPatchRequest);

    // When
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
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

    String jsonContent = mapper.writeValueAsString(batchPatchRequest);

    // When/Then - should return error for non-existent folder
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(content().string(
            containsString("'Test Folder with id: 999 for project: 1' not found")));
  }

  @Test
  void importTestCasesToExistingFolderIntegrationTest() throws Exception {
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

    entityManager.clear();

    // Then - Verify test case is created in database with new folder
    var importedTestCases = testCaseRepository.findAll().stream()
        .filter(tc -> tc.getName().equals("Imported Test Case with New Folder"))
        .toList();

    assertEquals(1, importedTestCases.size());
    assertNotNull(importedTestCases.getFirst().getTestFolder());

    var testFolder = tmsTestFolderRepository.findById(
        importedTestCases.getFirst().getTestFolder().getId());

    assertTrue(testFolder.isPresent());
    assertEquals("New Import Folder", testFolder.get().getName());
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
        .andExpect(content().string(containsString(
            "Either testFolderId or testFolderName must be provided and not empty")));
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
        .andExpect(content().string(containsString(
            "Either testFolderId or testFolderName must be provided and not empty")));
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
        .andExpect(content().string(containsString(
            "Either testFolderId or testFolderName must be provided and not empty")));
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

    String jsonContent = mapper.writeValueAsString(deleteRequest);

    // When
    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/" + testCaseId + "/tags")
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

    String jsonContent = mapper.writeValueAsString(deleteRequest);

    // When
    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/" + testCaseId + "/tags")
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

    String jsonContent = mapper.writeValueAsString(deleteRequest);

    // When/Then - should succeed (no error for non-existent tags)
    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/" + testCaseId + "/tags")
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

    String jsonContent = mapper.writeValueAsString(deleteRequest);

    // When/Then - should return validation error
    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/" + testCaseId + "/tags")
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

    String jsonContent = mapper.writeValueAsString(deleteRequest);

    // When/Then - should return not found
    mockMvc.perform(delete(
            "/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/" + nonExistentTestCaseId
                + "/tags")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void patchTestCaseAttributesAddAndRemoveIntegrationTest() throws Exception {
    // Given - prepare test cases with initial attributes
    var testCaseIds = List.of(4L, 5L);
    var attributesToRemove = List.of(4L); // Remove attribute 4
    var attributeIdsToAdd = List.of(1L, 2L); // Add attributes 1 and 2

    // Verify initial state - test case 4 has attribute 4
    var tagsBeforeTestCase4 = testCaseAttributeRepository.findAllById_TestCaseId(4L);
    assertTrue(
        tagsBeforeTestCase4.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(4L)));

    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(testCaseIds)
        .attributesToRemove(attributesToRemove)
        .attributeIdsToAdd(attributeIdsToAdd)
        .build();

    String jsonContent = mapper.writeValueAsString(patchRequest);

    // When
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/attributes/batch")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // Then - Verify attributes are added and removed correctly
    var tagsAfterTestCase4 = testCaseAttributeRepository.findAllById_TestCaseId(4L);
    var tagsAfterTestCase5 = testCaseAttributeRepository.findAllById_TestCaseId(5L);

    // Verify attribute 4 is removed from test case 4
    assertFalse(
        tagsAfterTestCase4.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(4L)));

    // Verify attributes 1 and 2 are added to both test cases
    assertTrue(
        tagsAfterTestCase4.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(1L)));
    assertTrue(
        tagsAfterTestCase4.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(2L)));
    assertTrue(
        tagsAfterTestCase5.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(1L)));
    assertTrue(
        tagsAfterTestCase5.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(2L)));
  }

  @Test
  void patchTestCaseAttributesOnlyAddIntegrationTest() throws Exception {
    // Given - only add attributes, no removal
    var testCaseIds = List.of(6L, 7L);
    var attributeIdsToAdd = List.of(3L, 4L);

    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(testCaseIds)
        .attributeIdsToAdd(attributeIdsToAdd)
        .build();

    String jsonContent = mapper.writeValueAsString(patchRequest);

    // When
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/attributes/batch")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // Then - Verify attributes are added
    var tagsAfterTestCase6 = testCaseAttributeRepository.findAllById_TestCaseId(6L);
    var tagsAfterTestCase7 = testCaseAttributeRepository.findAllById_TestCaseId(7L);

    assertTrue(
        tagsAfterTestCase6.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(3L)));
    assertTrue(
        tagsAfterTestCase6.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(4L)));
    assertTrue(
        tagsAfterTestCase7.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(3L)));
    assertTrue(
        tagsAfterTestCase7.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(4L)));
  }

  @Test
  void patchTestCaseAttributesOnlyRemoveIntegrationTest() throws Exception {
    // Given - only remove attributes, no addition
    var testCaseIds = List.of(9L);
    var attributesToRemove = List.of(4L);

    // Verify initial state
    var tagsBeforeTestCase9 = testCaseAttributeRepository.findAllById_TestCaseId(9L);
    assertTrue(
        tagsBeforeTestCase9.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(4L)));

    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(testCaseIds)
        .attributesToRemove(attributesToRemove)
        .build();

    String jsonContent = mapper.writeValueAsString(patchRequest);

    // When
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/attributes/batch")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // Then - Verify attribute is removed
    var tagsAfterTestCase9 = testCaseAttributeRepository.findAllById_TestCaseId(9L);
    assertFalse(
        tagsAfterTestCase9.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(4L)));
  }

  @Test
  void patchTestCaseAttributesWithOverlapIntegrationTest() throws Exception {
    // Given - same attribute in both add and remove lists (should be ignored)
    var testCaseIds = List.of(10L);
    var attributesToRemove = List.of(1L, 2L);
    var attributeIdsToAdd = List.of(1L, 3L); // 1L is in both lists

    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(testCaseIds)
        .attributesToRemove(attributesToRemove)
        .attributeIdsToAdd(attributeIdsToAdd)
        .build();

    String jsonContent = mapper.writeValueAsString(patchRequest);

    // When
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/attributes/batch")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // Then - Verify only attribute 2 is removed and attribute 3 is added
    // Attribute 1 should remain unchanged due to overlap
    var tagsAfterTestCase10 = testCaseAttributeRepository.findAllById_TestCaseId(10L);
    assertTrue(
        tagsAfterTestCase10.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(3L)));
    assertFalse(
        tagsAfterTestCase10.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(2L)));
  }

  @Test
  void patchTestCaseAttributesWithEmptyTestCaseIdsIntegrationTest() throws Exception {
    // Given
    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(Collections.emptyList())
        .attributeIdsToAdd(List.of(1L))
        .build();

    String jsonContent = mapper.writeValueAsString(patchRequest);

    // When/Then - should return validation error due to @NotEmpty
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/attributes/batch")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void patchTestCaseAttributesWithNonExistentTestCasesIntegrationTest() throws Exception {
    // Given
    var nonExistentTestCaseIds = List.of(999L, 888L);
    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(nonExistentTestCaseIds)
        .attributeIdsToAdd(List.of(1L))
        .build();

    String jsonContent = mapper.writeValueAsString(patchRequest);

    // When/Then - should return not found error
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/attributes/batch")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("Test Cases with ids: [999, 888]")));
  }

  @Test
  void patchTestCaseAttributesWithMixedExistingAndNonExistentTestCasesIntegrationTest()
      throws Exception {
    // Given
    var mixedTestCaseIds = List.of(4L, 999L); // 4L exists, 999L doesn't
    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(mixedTestCaseIds)
        .attributeIdsToAdd(List.of(1L))
        .build();

    String jsonContent = mapper.writeValueAsString(patchRequest);

    // When/Then - should return not found error for non-existent test cases
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/attributes/batch")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("Test Cases with ids: [999]")));
  }

  @Test
  void patchTestCaseAttributesWithEmptyAttributeListsIntegrationTest() throws Exception {
    // Given - both lists are empty (should fail custom validation)
    var testCaseIds = List.of(11L);
    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(testCaseIds)
        .attributesToRemove(Collections.emptyList())
        .attributeIdsToAdd(Collections.emptyList())
        .build();

    String jsonContent = mapper.writeValueAsString(patchRequest);

    // When/Then - should fail custom validation @ValidBatchPatchTestCaseAttributesRQ
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/attributes/batch")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void patchTestCaseAttributesWithNullAttributeListsIntegrationTest() throws Exception {
    // Given - both lists are null (should fail custom validation)
    var testCaseIds = List.of(12L);
    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(testCaseIds)
        .attributesToRemove(null)
        .attributeIdsToAdd(null)
        .build();

    String jsonContent = mapper.writeValueAsString(patchRequest);

    // When/Then - should fail custom validation @ValidBatchPatchTestCaseAttributesRQ
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/attributes/batch")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void patchTestCaseAttributesWithMalformedJsonIntegrationTest() throws Exception {
    // Given - malformed JSON
    String malformedJson = "{\"testCaseIds\": [4, 5], \"attributeIdsToAdd\": [1, 2";

    // When/Then - should return bad request
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/attributes/batch")
                .contentType("application/json")
                .content(malformedJson)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void patchTestCaseAttributesWithNullTestCaseIdsIntegrationTest() throws Exception {
    // Given - null test case IDs
    String jsonContent = "{\"testCaseIds\": null, \"attributeIdsToAdd\": [1, 2]}";

    // When/Then - should return validation error due to @NotEmpty
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/attributes/batch")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void patchTestCaseAttributesWithValidSingleAttributeListIntegrationTest() throws Exception {
    // Given - only one of the lists is populated (should pass custom validation)
    var testCaseIds = List.of(13L);
    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(testCaseIds)
        .attributesToRemove(null)
        .attributeIdsToAdd(List.of(1L, 2L))
        .build();

    String jsonContent = mapper.writeValueAsString(patchRequest);

    // When/Then - should succeed because at least one attribute list is not empty
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/attributes/batch")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // Verify attributes were added
    var tagsAfterTestCase13 = testCaseAttributeRepository.findAllById_TestCaseId(13L);
    assertTrue(
        tagsAfterTestCase13.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(1L)));
    assertTrue(
        tagsAfterTestCase13.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(2L)));
  }

  @Test
  void patchTestCaseAttributesWithValidRemoveOnlyIntegrationTest() throws Exception {
    // Given - only remove list is populated (should pass custom validation)
    var testCaseIds = List.of(37L);

    // Verify test case has attribute 2 before removal
    var tagsBeforeTestCase14 = testCaseAttributeRepository.findAllById_TestCaseId(37L);
    assertTrue(
        tagsBeforeTestCase14.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(2L)));

    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(testCaseIds)
        .attributesToRemove(List.of(2L))
        .attributeIdsToAdd(null)
        .build();

    String jsonContent = mapper.writeValueAsString(patchRequest);

    // When/Then - should succeed because at least one attribute list is not empty
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/attributes/batch")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    entityManager.clear();

    // Verify attribute was removed
    var tagsAfterTestCase14 = testCaseAttributeRepository.findAllById_TestCaseId(14L);
    assertFalse(
        tagsAfterTestCase14.stream().anyMatch(tag -> tag.getId().getAttributeId().equals(2L)));
  }

  @Test
  void deleteTestCasesWithEmptyLocationIdsIntegrationTest() throws Exception {
    // Given
    BatchDeleteTestCasesRQ deleteRequest = BatchDeleteTestCasesRQ.builder()
        .testCaseIds(Collections.emptyList())
        .build();

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

    String jsonContent = mapper.writeValueAsString(batchPatchRequest);

    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(content().string(
            containsString("'Test Folder with id: 999 for project: 1' not found")));
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
            .param("filter.eq.testPlanId", "1")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(
            7)); // 7 test cases in test plan 1 (4,5,6,13,14,15,16)
  }

  @Test
  void getTestCasesByTestPlanIdAndFolderIdCriteriaIntegrationTest() throws Exception {
    // When/Then - Filter by test plan and folder
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("filter.eq.testPlanId", "1")
            .param("filter.eq.testFolderId", "7")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(
            4)); // 4 test cases in test plan 1 and folder 7 (13,14,15,16)
  }

  @Test
  void getTestCasesByTestPlanIdAndSearchCriteriaIntegrationTest() throws Exception {
    // When/Then - Filter by test plan and search
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("filter.eq.testPlanId", "1")
            .param("filter.fts.search", "Test Case 5")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(
            1)); // 1 test case with "Test Case 5" in name in test plan 1
  }

  @Test
  void getTestCasesByAllThreeCriteriaIntegrationTest() throws Exception {
    // When/Then - Filter by test plan, folder, and search
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("filter.eq.testPlanId", "1")
            .param("filter.eq.testFolderId", "7")
            .param("filter.fts.search", "Login")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(
            jsonPath("$.content.length()").value(1)); // 1 test case matching all criteria (15)
  }

  @Test
  void getTestCasesByNonExistentTestPlanIdIntegrationTest() throws Exception {
    // When/Then - Filter by non-existent test plan
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("filter.eq.testPlanId", "999")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(
            jsonPath("$.content.length()").value(0)); // 0 test cases for non-existent test plan
  }

  @Test
  void getTestCasesByTestPlanId2CriteriaIntegrationTest() throws Exception {
    // When/Then - Filter by test plan 2
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("filter.eq.testPlanId", "2")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(
            jsonPath("$.content.length()").value(5)); // 5 test cases in test plan 2 (7,8,9,15,19)
  }

  @Test
  void getTestCasesByTestPlanId3CriteriaIntegrationTest() throws Exception {
    // When/Then - Filter by test plan 3
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("filter.eq.testPlanId", "3")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(
            jsonPath("$.content.length()").value(4)); // 4 test cases in test plan 3 (10,11,12,20)
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
            .param("filter.eq.testPlanId", "2")
            .param("filter.eq.testFolderId", "8")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1)); // test case 19

    // Test Plan 3 + Folder 8 should return test case 20
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("filter.eq.testPlanId", "3")
            .param("filter.eq.testFolderId", "8")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1)); // test case 20
  }

  @Test
  void duplicateTestCasesIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = List.of(4L, 5L);
    BatchDuplicateTestCasesRQ duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testFolderId(10L)
        .testCaseIds(testCaseIds)
        .build();

    String jsonContent = mapper.writeValueAsString(duplicateRequest);

    // Get initial count of test cases
    long initialCount = testCaseRepository.count();

    // When
    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/duplicate")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andReturn();

    // Then - Verify new test cases were created
    long finalCount = testCaseRepository.count();
    assertEquals(initialCount + 2, finalCount);

    var responseBody = result.getResponse().getContentAsString();

    var duplicatesTestCasesResponse = mapper.readValue(responseBody,
        new TypeReference<List<TmsTestCaseRS>>() {
        });

    var duplicatedTestCases = testCaseRepository.findAllById(
        duplicatesTestCasesResponse.stream().map(TmsTestCaseRS::getId).toList()
    );

    assertFalse(duplicatedTestCases.isEmpty());
    assertEquals(2, duplicatedTestCases.size());
    duplicatedTestCases
        .forEach(testCase -> {
          assertEquals(10L, testCase.getTestFolder().getId());
        });
  }

  @Test
  void duplicateSingleTestCaseIntegrationTest() throws Exception {
    // Given
    List<Long> testCaseIds = List.of(6L);
    BatchDuplicateTestCasesRQ duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testFolderId(10L)
        .testCaseIds(testCaseIds)
        .build();

    String jsonContent = mapper.writeValueAsString(duplicateRequest);

    // Get original test case for comparison
    Optional<TmsTestCase> originalTestCase = testCaseRepository.findById(6L);
    assertTrue(originalTestCase.isPresent());

    // When
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/duplicate")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].testFolder").exists())
        .andExpect(jsonPath("$[0].testFolder.id").exists())
        .andExpect(jsonPath("$[0].testFolder.id").value(duplicateRequest.getTestFolderId()));
  }

  @Test
  void duplicateTestCasesWithManualScenarioIntegrationTest() throws Exception {
    // Given - test case 17 has a text manual scenario
    List<Long> testCaseIds = List.of(17L);
    BatchDuplicateTestCasesRQ duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testFolderId(10L)
        .testCaseIds(testCaseIds)
        .build();

    String jsonContent = mapper.writeValueAsString(duplicateRequest);

    // When
    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/duplicate")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].testFolder").exists())
        .andExpect(jsonPath("$[0].testFolder.id").exists())
        .andExpect(jsonPath("$[0].testFolder.id").value(duplicateRequest.getTestFolderId()))
        .andExpect(jsonPath("$[0].manualScenario").exists())
        .andExpect(jsonPath("$[0].manualScenario.manualScenarioType").value("TEXT"))
        .andReturn();

    var responseBody = result.getResponse().getContentAsString();

    var duplicatesTestCasesResponse = mapper.readValue(responseBody,
        new TypeReference<List<TmsTestCaseRS>>() {
        });

    // Then - Verify duplicated test case has manual scenario
    var duplicatedTestCases = testCaseRepository.findAllById(
        duplicatesTestCasesResponse.stream().map(TmsTestCaseRS::getId).toList()
    );

    assertEquals(1, duplicatedTestCases.size());

    var defaultVersion = tmsTestCaseVersionRepository
        .findDefaultVersionByTestCaseId(duplicatedTestCases.getFirst().getId());

    assertTrue(defaultVersion.isPresent());
    assertNotNull(defaultVersion.get().getManualScenario());
    assertNotNull(defaultVersion.get().getManualScenario().getTextScenario());
  }

  @Test
  void duplicateTestCasesWithStepsManualScenarioIntegrationTest() throws Exception {
    // Given - test case 34 has a steps manual scenario
    List<Long> testCaseIds = List.of(34L);
    BatchDuplicateTestCasesRQ duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testFolder(NewTestFolderRQ.builder()
            .name("new parent folder from test case duplication")
            .parentTestFolderId(10L)
            .build())
        .testCaseIds(testCaseIds)
        .build();

    String jsonContent = mapper.writeValueAsString(duplicateRequest);

    // When
    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/duplicate")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].manualScenario").exists())
        .andExpect(jsonPath("$[0].manualScenario.manualScenarioType").value("STEPS"))
        .andExpect(jsonPath("$[0].testFolder").exists())
        .andExpect(jsonPath("$[0].testFolder.id").exists())
        .andReturn();

    var responseBody = result.getResponse().getContentAsString();

    var duplicatesTestCasesResponse = mapper.readValue(responseBody,
        new TypeReference<List<TmsTestCaseRS>>() {
        });

    // Then - Verify duplicated test case has manual scenario
    var duplicatedTestCases = testCaseRepository.findAllById(
        duplicatesTestCasesResponse.stream().map(TmsTestCaseRS::getId).toList()
    );

    assertEquals(1, duplicatedTestCases.size());

    var testFolder = tmsTestFolderRepository.findById(
        duplicatedTestCases.getFirst().getTestFolder().getId());
    assertTrue(testFolder.isPresent());
    assertEquals("new parent folder from test case duplication", testFolder.get().getName());
    assertEquals(10L, testFolder.get().getParentTestFolder().getId());

    var defaultVersion = tmsTestCaseVersionRepository
        .findDefaultVersionByTestCaseId(duplicatedTestCases.getFirst().getId());

    assertTrue(defaultVersion.isPresent());
    assertNotNull(defaultVersion.get().getManualScenario());
    assertNotNull(defaultVersion.get().getManualScenario().getStepsScenario());
    assertThat(defaultVersion.get().getManualScenario().getStepsScenario().getSteps())
        .isNotNull()
        .isNotEmpty();
  }

  @Test
  void duplicateTestCasesWithEmptyIdsIntegrationTest() throws Exception {
    // Given
    BatchDuplicateTestCasesRQ duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testCaseIds(Collections.emptyList())
        .build();

    String jsonContent = mapper.writeValueAsString(duplicateRequest);

    // When/Then - should return bad request due to @NotEmpty validation
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/duplicate")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void duplicateTestCasesWithNullIdsIntegrationTest() throws Exception {
    // Given - create request with null testCaseIds
    String jsonContent = "{\"testCaseIds\": null}";

    // When/Then - should return bad request due to @NotNull validation
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/duplicate")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void duplicateTestCasesWithNonExistentIdsIntegrationTest() throws Exception {
    // Given
    List<Long> nonExistentIds = List.of(999L, 1000L);
    BatchDuplicateTestCasesRQ duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testCaseIds(nonExistentIds)
        .build();

    String jsonContent = mapper.writeValueAsString(duplicateRequest);

    // When/Then - should return not found for non-existent test cases
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/duplicate")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(content().string(
            containsString("Test Cases with ids: [999, 1000]")));
  }

  @Test
  void duplicateTestCasesWithMixedExistingAndNonExistentIdsIntegrationTest() throws Exception {
    // Given
    List<Long> mixedIds = List.of(4L, 999L); // 4L exists, 999L doesn't
    BatchDuplicateTestCasesRQ duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testCaseIds(mixedIds)
        .build();

    String jsonContent = mapper.writeValueAsString(duplicateRequest);

    // When/Then - should return not found for non-existent test cases
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/duplicate")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("Test Cases with ids: [999]")));
  }

  @Test
  void duplicateTestCasesWithMalformedJsonIntegrationTest() throws Exception {
    // Given - malformed JSON
    String malformedJson = "{\"testCaseIds\": [4, 5";

    // When/Then - should return bad request due to malformed JSON
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/duplicate")
            .contentType("application/json")
            .content(malformedJson)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void duplicateTestCasesWithTagsInDefaultProjectIntegrationTest() throws Exception {
    // Given - test case 36 has tags
    List<Long> testCaseIds = List.of(36L);
    BatchDuplicateTestCasesRQ duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testFolderId(11L)
        .testCaseIds(testCaseIds)
        .build();

    String jsonContent = mapper.writeValueAsString(duplicateRequest);

    // Get original test case tags
    var originalTags = testCaseAttributeRepository.findAllById_TestCaseId(4L);

    // When
    var result = mockMvc.perform(
            post("/v1/project/" + DEFAULT_PROJECT_KEY + "/tms/test-case/batch/duplicate")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andReturn();

    entityManager.clear();

    // Then - Verify duplicated test case has same tags
    var responseBody = result.getResponse().getContentAsString();

    var duplicatesTestCasesResponse = mapper.readValue(responseBody,
        new TypeReference<List<TmsTestCaseRS>>() {
        });

    var duplicatedTestCases = testCaseRepository.findAllById(
        duplicatesTestCasesResponse.stream().map(TmsTestCaseRS::getId).toList()
    );

    assertEquals(1, duplicatedTestCases.size());

    var duplicatedAttributes = duplicatesTestCasesResponse.getFirst().getAttributes();

    assertEquals(originalTags.size(), duplicatedAttributes.size());
  }

  @Test
  void duplicateTestCasesInDefaultProjectIntegrationTest() throws Exception {
    // Given - use test case from default project (ids starting from higher numbers based on SQL)
    List<Long> testCaseIds = List.of(35L);
    BatchDuplicateTestCasesRQ duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testFolderId(11L)
        .testCaseIds(testCaseIds)
        .build();

    String jsonContent = mapper.writeValueAsString(duplicateRequest);

    // When
    mockMvc.perform(post("/v1/project/" + DEFAULT_PROJECT_KEY + "/tms/test-case/batch/duplicate")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].testFolder").exists())
        .andExpect(jsonPath("$[0].testFolder.id").exists())
        .andExpect(jsonPath("$[0].testFolder.id").value(duplicateRequest.getTestFolderId()));
  }

  @Test
  void createTestCaseWithTextScenarioAttachments_ShouldMakeAttachmentsPermanent() throws Exception {
    // Given uploaded attachments
    var attachment1 = uploadTestAttachment("scenario-attachment-1.pdf", "application/pdf");
    var attachment2 = uploadTestAttachment("scenario-attachment-2.jpg", "image/jpeg");

    var attachmentRQ1 = new TmsManualScenarioAttachmentRQ();
    attachmentRQ1.setId(String.valueOf(attachment1.getId()));

    var attachmentRQ2 = new TmsManualScenarioAttachmentRQ();
    attachmentRQ2.setId(String.valueOf(attachment2.getId()));

    var manualScenarioRQ = TmsTextManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.TEXT)
        .instructions("Test instructions with attachments")
        .expectedResult("Expected result")
        .attachments(List.of(attachmentRQ1, attachmentRQ2))
        .build();

    var testCaseRQ = TmsTestCaseRQ.builder()
        .name("Test Case With Text Scenario and Attachments")
        .description("Description for test case with attachments")
        .testFolderId(3L)
        .manualScenario(manualScenarioRQ)
        .build();

    // When creating test case
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(testCaseRQ))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Case With Text Scenario and Attachments"))
        .andExpect(jsonPath("$.manualScenario.attachments").isArray())
        .andExpect(jsonPath("$.manualScenario.attachments.length()").value(2));

    // Then verify attachments TTL was removed (made permanent)
    var persistedAttachment1 = tmsAttachmentRepository.findById(attachment1.getId());
    var persistedAttachment2 = tmsAttachmentRepository.findById(attachment2.getId());

    assertTrue(persistedAttachment1.isPresent());
    assertTrue(persistedAttachment2.isPresent());
    assertNull(persistedAttachment1.get().getExpiresAt()); // TTL should be removed
    assertNull(persistedAttachment2.get().getExpiresAt()); // TTL should be removed
  }

  @Test
  void createTestCaseWithStepsScenarioAttachments_ShouldMakeAttachmentsPermanent()
      throws Exception {
    // Given uploaded attachments for steps
    var stepAttachment1 = uploadTestAttachment("step1-attachment.docx",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    var stepAttachment2 = uploadTestAttachment("step2-attachment.png", "image/png");

    var stepAttachmentRQ1 = new TmsManualScenarioAttachmentRQ();
    stepAttachmentRQ1.setId(String.valueOf(stepAttachment1.getId()));

    var stepAttachmentRQ2 = new TmsManualScenarioAttachmentRQ();
    stepAttachmentRQ2.setId(String.valueOf(stepAttachment2.getId()));

    var step1 = TmsStepRQ.builder()
        .instructions("Step 1 instructions")
        .expectedResult("Step 1 expected result")
        .attachments(List.of(stepAttachmentRQ1))
        .build();

    var step2 = TmsStepRQ.builder()
        .instructions("Step 2 instructions")
        .expectedResult("Step 2 expected result")
        .attachments(List.of(stepAttachmentRQ2))
        .build();

    var manualScenarioRQ = TmsStepsManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.STEPS)
        .steps(List.of(step1, step2))
        .build();

    var testCaseRQ = TmsTestCaseRQ.builder()
        .name("Test Case With Steps and Attachments")
        .description("Description for test case with step attachments")
        .testFolderId(3L)
        .manualScenario(manualScenarioRQ)
        .build();

    // When creating a test case
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(testCaseRQ))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Case With Steps and Attachments"))
        .andExpect(jsonPath("$.manualScenario.steps").isArray())
        .andExpect(jsonPath("$.manualScenario.steps.length()").value(2));
  }

  @Test
  void updateTestCaseWithAttachments_ShouldMakeNewAttachmentsPermanent() throws Exception {
    // Given new attachment for update
    var newAttachment = uploadTestAttachment("updated-attachment.pdf", "application/pdf");

    var attachmentRQ = new TmsManualScenarioAttachmentRQ();
    attachmentRQ.setId(String.valueOf(newAttachment.getId()));

    var manualScenarioRQ = TmsTextManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.TEXT)
        .instructions("Updated instructions with new attachment")
        .expectedResult("Updated expected result")
        .attachments(List.of(attachmentRQ))
        .build();

    var testCaseRQ = TmsTestCaseRQ.builder()
        .name("Updated Test Case 17 with Attachments")
        .description("Updated description")
        .testFolderId(7L)
        .manualScenario(manualScenarioRQ)
        .build();

    // When updating existing test case
    mockMvc.perform(put("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/17")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(testCaseRQ))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated Test Case 17 with Attachments"));

    // Then verify attachment TTL was removed
    var persistedAttachment = tmsAttachmentRepository.findById(newAttachment.getId());
    assertTrue(persistedAttachment.isPresent());
    assertNull(persistedAttachment.get().getExpiresAt());
  }

  @Test
  void duplicateTestCaseWithTextManualScenarioAttachments_ShouldDuplicateAttachments()
      throws Exception {
    // Given test case with attachments created first
    var originalAttachment = uploadTestAttachment("original-attachment.txt", "text/plain");

    var attachmentRQ = new TmsManualScenarioAttachmentRQ();
    attachmentRQ.setId(String.valueOf(originalAttachment.getId()));

    var manualScenarioRQ = TmsTextManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.TEXT)
        .instructions("Original instructions with attachment")
        .expectedResult("Original expected result")
        .attachments(List.of(attachmentRQ))
        .build();

    var testCaseRQ = TmsTestCaseRQ.builder()
        .name("Original Test Case with Attachment")
        .description("Original description")
        .testFolderId(3L)
        .manualScenario(manualScenarioRQ)
        .build();

    var createResult = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(testCaseRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();

    var createdTestCase = mapper.readValue(createResult.getResponse().getContentAsString(),
        TmsTestCaseRS.class);

    // When duplicating test case
    var duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testFolderId(10L)
        .testCaseIds(List.of(createdTestCase.getId()))
        .build();

    var duplicateResult = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/duplicate")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(duplicateRequest))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].manualScenario.attachments").isArray())
        .andExpect(jsonPath("$[0].manualScenario.attachments.length()").value(1))
        .andReturn();

    entityManager.clear();

    // Then verify attachment was duplicated
    var duplicateResponse = mapper.readValue(duplicateResult.getResponse().getContentAsString(),
        new TypeReference<List<TmsTestCaseRS>>() {
        });

    var textManualScenarioRS = (TmsTextManualScenarioRS) duplicateResponse.getFirst()
        .getManualScenario();

    var duplicatedAttachmentId = Long.valueOf(
        textManualScenarioRS.getAttachments().getFirst().getId());

    var duplicatedAttachment = tmsAttachmentRepository.findById(duplicatedAttachmentId);

    assertTrue(duplicatedAttachment.isPresent());
    assertNotEquals(originalAttachment.getId(), duplicatedAttachment.get().getId());
    assertNull(duplicatedAttachment.get().getExpiresAt()); // Should be permanent
  }

  @Test
  void createTestCaseWithInvalidAttachmentId_ShouldIgnoreInvalidAttachments() throws Exception {
    // Given non-existent attachment ID
    var invalidAttachmentRQ = new TmsManualScenarioAttachmentRQ();
    invalidAttachmentRQ.setId("999999");

    var manualScenarioRQ = TmsTextManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.TEXT)
        .instructions("Test instructions")
        .expectedResult("Expected result")
        .attachments(List.of(invalidAttachmentRQ))
        .build();

    var testCaseRQ = TmsTestCaseRQ.builder()
        .name("Test Case With Invalid Attachment")
        .description("Description")
        .testFolderId(3L)
        .manualScenario(manualScenarioRQ)
        .build();

    // When creating test case with invalid attachment ID
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(testCaseRQ))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Case With Invalid Attachment"));
    // Invalid attachments should be silently ignored, test case should still be created
  }

  // Helper method to upload test attachments
  private UploadAttachmentRS uploadTestAttachment(String fileName, String contentType)
      throws Exception {
    var file = new MockMultipartFile(
        "file",
        fileName,
        contentType,
        ("test content for " + fileName).getBytes()
    );

    var result = mockMvc.perform(
            multipart("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/attachment/upload")
                .file(file)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();

    return mapper.readValue(result.getResponse().getContentAsString(), UploadAttachmentRS.class);
  }

  @Test
  void createTestCaseWithPreconditionsAttachments_ShouldMakeAttachmentsPermanent()
      throws Exception {
    // Given uploaded preconditions attachments
    var preconditionsAttachment1 = uploadTestAttachment("preconditions1.xlsx",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    var preconditionsAttachment2 = uploadTestAttachment("preconditions2.pdf", "application/pdf");

    var preconditionsAttachmentRQ1 = new TmsManualScenarioAttachmentRQ();
    preconditionsAttachmentRQ1.setId(String.valueOf(preconditionsAttachment1.getId()));

    var preconditionsAttachmentRQ2 = new TmsManualScenarioAttachmentRQ();
    preconditionsAttachmentRQ2.setId(String.valueOf(preconditionsAttachment2.getId()));

    var preconditionsRQ = TmsManualScenarioPreconditionsRQ.builder()
        .value("Test preconditions with multiple attachments")
        .attachments(List.of(preconditionsAttachmentRQ1, preconditionsAttachmentRQ2))
        .build();

    var manualScenarioRQ = TmsTextManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.TEXT)
        .instructions("Test instructions")
        .expectedResult("Expected result")
        .preconditions(preconditionsRQ)
        .build();

    var testCaseRQ = TmsTestCaseRQ.builder()
        .name("Test Case With Multiple Preconditions Attachments")
        .description("Description for test case with multiple preconditions attachments")
        .testFolderId(3L)
        .manualScenario(manualScenarioRQ)
        .build();

    // When creating test case
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(testCaseRQ))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Case With Multiple Preconditions Attachments"))
        .andExpect(jsonPath("$.manualScenario.preconditions.attachments").isArray())
        .andExpect(jsonPath("$.manualScenario.preconditions.attachments.length()").value(2));

    // Then verify preconditions attachments TTL was removed
    var persistedAttachment1 = tmsAttachmentRepository.findById(preconditionsAttachment1.getId());
    var persistedAttachment2 = tmsAttachmentRepository.findById(preconditionsAttachment2.getId());

    assertTrue(persistedAttachment1.isPresent());
    assertTrue(persistedAttachment2.isPresent());
    assertNull(persistedAttachment1.get().getExpiresAt()); // TTL should be removed
    assertNull(persistedAttachment2.get().getExpiresAt()); // TTL should be removed
  }

  @Test
  void createTestCaseWithStepsScenarioAndPreconditionsAttachments_ShouldMakeAllAttachmentsPermanent()
      throws Exception {
    // Given uploaded attachments for both steps and preconditions
    var stepAttachment = uploadTestAttachment("step-attachment.png", "image/png");
    var preconditionsAttachment = uploadTestAttachment("preconditions-doc.docx",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    var stepAttachmentRQ = new TmsManualScenarioAttachmentRQ();
    stepAttachmentRQ.setId(String.valueOf(stepAttachment.getId()));

    var preconditionsAttachmentRQ = new TmsManualScenarioAttachmentRQ();
    preconditionsAttachmentRQ.setId(String.valueOf(preconditionsAttachment.getId()));

    var preconditionsRQ = TmsManualScenarioPreconditionsRQ.builder()
        .value("Prerequisites with attachment")
        .attachments(List.of(preconditionsAttachmentRQ))
        .build();

    var step1 = TmsStepRQ.builder()
        .instructions("Step with attachment")
        .expectedResult("Step should complete successfully")
        .attachments(List.of(stepAttachmentRQ))
        .build();

    var manualScenarioRQ = TmsStepsManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.STEPS)
        .preconditions(preconditionsRQ)
        .steps(List.of(step1))
        .build();

    var testCaseRQ = TmsTestCaseRQ.builder()
        .name("Test Case With Steps and Preconditions Attachments")
        .description("Test case with attachments in both steps and preconditions")
        .testFolderId(3L)
        .manualScenario(manualScenarioRQ)
        .build();

    // When creating a test case
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(testCaseRQ))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Case With Steps and Preconditions Attachments"))
        .andExpect(jsonPath("$.manualScenario.preconditions.attachments").isArray())
        .andExpect(jsonPath("$.manualScenario.preconditions.attachments.length()").value(1))
        .andExpect(jsonPath("$.manualScenario.steps").isArray())
        .andExpect(jsonPath("$.manualScenario.steps[0].attachments").isArray())
        .andExpect(jsonPath("$.manualScenario.steps[0].attachments.length()").value(1));

    // Then verify all attachments TTL was removed
    var persistedStepAttachment = tmsAttachmentRepository.findById(stepAttachment.getId());
    var persistedPreconditionsAttachment = tmsAttachmentRepository.findById(
        preconditionsAttachment.getId());

    assertTrue(persistedStepAttachment.isPresent());
    assertTrue(persistedPreconditionsAttachment.isPresent());
    assertNull(persistedStepAttachment.get().getExpiresAt()); // TTL should be removed
    assertNull(persistedPreconditionsAttachment.get().getExpiresAt()); // TTL should be removed
  }

  @Test
  void updateTestCaseWithPreconditionsAttachments_ShouldMakeNewAttachmentsPermanent()
      throws Exception {
    // Given new preconditions attachment for update
    var newPreconditionsAttachment = uploadTestAttachment("updated-preconditions.txt",
        "text/plain");

    var preconditionsAttachmentRQ = new TmsManualScenarioAttachmentRQ();
    preconditionsAttachmentRQ.setId(String.valueOf(newPreconditionsAttachment.getId()));

    var preconditionsRQ = TmsManualScenarioPreconditionsRQ.builder()
        .value("Updated preconditions with new attachment")
        .attachments(List.of(preconditionsAttachmentRQ))
        .build();

    var manualScenarioRQ = TmsTextManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.TEXT)
        .instructions("Updated instructions")
        .expectedResult("Updated expected result")
        .preconditions(preconditionsRQ)
        .build();

    var testCaseRQ = TmsTestCaseRQ.builder()
        .name("Updated Test Case 18 with Preconditions Attachments")
        .description("Updated description")
        .testFolderId(6L)
        .manualScenario(manualScenarioRQ)
        .build();

    // When updating existing test case
    mockMvc.perform(put("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/18")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(testCaseRQ))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated Test Case 18 with Preconditions Attachments"))
        .andExpect(jsonPath("$.manualScenario.preconditions.attachments").isArray())
        .andExpect(jsonPath("$.manualScenario.preconditions.attachments.length()").value(1));

    // Then verify preconditions attachment TTL was removed
    var persistedAttachment = tmsAttachmentRepository.findById(newPreconditionsAttachment.getId());
    assertTrue(persistedAttachment.isPresent());
    assertNull(persistedAttachment.get().getExpiresAt());
  }

  @Test
  void duplicateTestCaseWithPreconditionsAttachments_ShouldDuplicatePreconditionsAttachments()
      throws Exception {
    // Given test case with preconditions attachments created first
    var originalPreconditionsAttachment = uploadTestAttachment("original-preconditions.json",
        "application/json");

    var preconditionsAttachmentRQ = new TmsManualScenarioAttachmentRQ();
    preconditionsAttachmentRQ.setId(String.valueOf(originalPreconditionsAttachment.getId()));

    var preconditionsRQ = TmsManualScenarioPreconditionsRQ.builder()
        .value("Original preconditions with attachment for duplication")
        .attachments(List.of(preconditionsAttachmentRQ))
        .build();

    var manualScenarioRQ = TmsTextManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.TEXT)
        .instructions("Original instructions")
        .expectedResult("Original expected result")
        .preconditions(preconditionsRQ)
        .build();

    var testCaseRQ = TmsTestCaseRQ.builder()
        .name("Original Test Case with Preconditions Attachment")
        .description("Original description")
        .testFolderId(3L)
        .manualScenario(manualScenarioRQ)
        .build();

    var createResult = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(testCaseRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();

    var createdTestCase = mapper.readValue(createResult.getResponse().getContentAsString(),
        TmsTestCaseRS.class);

    // When duplicating a test case
    var duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testFolderId(10L)
        .testCaseIds(List.of(createdTestCase.getId()))
        .build();

    var duplicateResult = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/batch/duplicate")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(duplicateRequest))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].manualScenario.preconditions.attachments").isArray())
        .andExpect(jsonPath("$[0].manualScenario.preconditions.attachments.length()").value(1))
        .andReturn();

    // Then verify preconditions attachment was duplicated
    var duplicateResponse = mapper.readValue(duplicateResult.getResponse().getContentAsString(),
        new TypeReference<List<TmsTestCaseRS>>() {
        });

    var duplicatedAttachmentId = Long.valueOf(duplicateResponse.getFirst()
        .getManualScenario().getPreconditions().getAttachments().getFirst().getId());

    entityManager.clear();

    var duplicatedAttachment = tmsAttachmentRepository.findById(duplicatedAttachmentId);
    assertTrue(duplicatedAttachment.isPresent());
    assertNotEquals(originalPreconditionsAttachment.getId(), duplicatedAttachment.get().getId());
    assertNull(duplicatedAttachment.get().getExpiresAt()); // Should be permanent
  }

  @Test
  void patchTestCaseWithPreconditionsAttachments_ShouldAddNewAttachments() throws Exception {
    // Given existing test case and new preconditions attachment
    var newPreconditionsAttachment = uploadTestAttachment("patched-preconditions.zip",
        "application/zip");

    var preconditionsAttachmentRQ = new TmsManualScenarioAttachmentRQ();
    preconditionsAttachmentRQ.setId(String.valueOf(newPreconditionsAttachment.getId()));

    var preconditionsRQ = TmsManualScenarioPreconditionsRQ.builder()
        .value("Patched preconditions with attachment")
        .attachments(List.of(preconditionsAttachmentRQ))
        .build();

    var patchRequest = TmsTestCaseRQ.builder()
        .manualScenario(TmsTextManualScenarioRQ
            .builder()
            .preconditions(preconditionsRQ)
            .build())
        .build();

    // When patching existing test case
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/17")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(patchRequest))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.manualScenario.preconditions.attachments").isArray())
        .andExpect(jsonPath("$.manualScenario.preconditions.attachments.length()").value(1));

    // Then verify preconditions attachment TTL was removed
    var persistedAttachment = tmsAttachmentRepository.findById(newPreconditionsAttachment.getId());
    assertTrue(persistedAttachment.isPresent());
    assertNull(persistedAttachment.get().getExpiresAt());
  }

  @Test
  void createTestCaseWithInvalidPreconditionsAttachmentId_ShouldIgnoreInvalidAttachments()
      throws Exception {
    // Given non-existent preconditions attachment ID
    var invalidPreconditionsAttachmentRQ = new TmsManualScenarioAttachmentRQ();
    invalidPreconditionsAttachmentRQ.setId("999888");

    var preconditionsRQ = TmsManualScenarioPreconditionsRQ.builder()
        .value("Preconditions with invalid attachment")
        .attachments(List.of(invalidPreconditionsAttachmentRQ))
        .build();

    var manualScenarioRQ = TmsTextManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.TEXT)
        .instructions("Test instructions")
        .expectedResult("Expected result")
        .preconditions(preconditionsRQ)
        .build();

    var testCaseRQ = TmsTestCaseRQ.builder()
        .name("Test Case With Invalid Preconditions Attachment")
        .description("Description")
        .testFolderId(3L)
        .manualScenario(manualScenarioRQ)
        .build();

    // When creating test case with invalid preconditions attachment ID
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(testCaseRQ))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Case With Invalid Preconditions Attachment"))
        .andExpect(jsonPath("$.manualScenario.preconditions.value").value(
            "Preconditions with invalid attachment"));
    // Invalid preconditions attachments should be silently ignored, test case should still be created
  }

  @Test
  void getTestCaseByIdWithLastExecutionIntegrationTest() throws Exception {
    // Given - test case 100 has last execution with start_time = 1696579200000 (timestamp)

    // When/Then
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/100")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(100L))
        .andExpect(jsonPath("$.name").value("Test Case with Last Execution"))
        .andExpect(jsonPath("$.lastExecutionAt").isNotEmpty()); // Latest execution
  }

  @Test
  void getTestCasesByCriteriaWithLastExecutionIntegrationTest() throws Exception {
    // When/Then - Get test cases with last executions
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case")
            .param("filter.in.id", "100,101,102,103")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(4))
        .andExpect(jsonPath("$.content[?(@.id == 100)].lastExecutionAt").isNotEmpty())
        .andExpect(jsonPath("$.content[?(@.id == 101)].lastExecutionAt").isNotEmpty())
        .andExpect(jsonPath("$.content[?(@.id == 102)].lastExecutionAt").doesNotExist())
        .andExpect(jsonPath("$.content[?(@.id == 103)].lastExecutionAt").isNotEmpty());
  }

  @Test
  void getTestCaseByIdWithoutExecutionIntegrationTest() throws Exception {
    // Given - test case 102 has no executions

    // When/Then
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/102")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(102L))
        .andExpect(jsonPath("$.name").value("Test Case without Execution"))
        .andExpect(jsonPath("$.lastExecutionAt").doesNotExist()); // No execution
  }

  @Test
  void patchTestCaseWithLastExecutionIntegrationTest() throws Exception {
    // Given
    var testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setDescription("Patched description for test case with execution");

    String jsonContent = mapper.writeValueAsString(testCaseRQ);

    // When
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/101")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(101L))
        .andExpect(
            jsonPath("$.description").value("Patched description for test case with execution"))
        .andExpect(
            jsonPath("$.lastExecutionAt").isNotEmpty()); // Should have the latest execution
  }
}
