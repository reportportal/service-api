package com.epam.reportportal.base.core.tms.controller.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.core.tms.dto.DuplicateTmsTestFolderRS;
import com.epam.reportportal.base.core.tms.dto.NewTestFolderRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestFolderExportFileType;
import com.epam.reportportal.base.core.tms.dto.TmsTestFolderRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestFolderRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * Integration tests for TmsTestFolderController. Tests the full flow of test folder operations including database
 * interactions.
 */
@Sql("/db/tms/tms-test-folder/tms-test-folder-fill.sql")
@ExtendWith(MockitoExtension.class)
@Disabled
class TmsTestFolderIntegrationTest extends BaseMvcTest {

  private static final String SUPERADMIN_PROJECT_KEY = "superadmin_personal";

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private TmsTestFolderRepository tmsTestFolderRepository;

  @Autowired
  private TmsTestCaseRepository tmsTestCaseRepository;

  @Test
  void createRootTestFolderIntegrationTest() throws Exception {
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("description_create")
        .name("name_create")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value(request.getName()))
        .andExpect(jsonPath("$.description").value(request.getDescription()));

    // Find the created folder (assuming it gets ID based on sequence or max+1)
    var createdFolder = tmsTestFolderRepository.findAll().stream()
        .filter(f -> f.getName().equals("name_create"))
        .findFirst();

    assertTrue(createdFolder.isPresent());
    assertEquals(request.getName(), createdFolder.get().getName());
    assertEquals(request.getDescription(), createdFolder.get().getDescription());
    assertEquals(1L, createdFolder.get().getProject().getId());
    assertNull(createdFolder.get().getParentTestFolder());
  }

  @Test
  void createTestFolderWithExistingParentIdIntegrationTest() throws Exception {
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("Child folder description")
        .name("Child folder")
        .parentTestFolderId(3L) // Use existing folder by ID
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value(request.getName()))
        .andExpect(jsonPath("$.description").value(request.getDescription()))
        .andExpect(jsonPath("$.parentFolderId").value(3L));

    // Verify in database
    var createdFolder = tmsTestFolderRepository.findAll().stream()
        .filter(f -> f.getName().equals("Child folder"))
        .findFirst();

    assertTrue(createdFolder.isPresent());
    assertNotNull(createdFolder.get().getParentTestFolder());
    assertEquals(3L, createdFolder.get().getParentTestFolder().getId());
  }

  @Test
  void createTestFolderWithNewRootParentFolderIntegrationTest() throws Exception {
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("Child folder with new parent")
        .name("Child folder with new parent")
        .parentTestFolder(NewTestFolderRQ.builder()
            .name("New Root Parent Folder")
            .build())
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value(request.getName()))
        .andExpect(jsonPath("$.description").value(request.getDescription()))
        .andExpect(jsonPath("$.parentFolderId").exists());

    entityManager.clear();

    // Verify both folders are created
    var createdChildFolder = tmsTestFolderRepository.findAll().stream()
        .filter(f -> f.getName().equals("Child folder with new parent"))
        .findFirst();

    assertTrue(createdChildFolder.isPresent());
    assertNotNull(createdChildFolder.get().getParentTestFolder());
    assertEquals("New Root Parent Folder",
        createdChildFolder.get().getParentTestFolder().getName());
    assertNull(createdChildFolder.get().getParentTestFolder()
        .getParentTestFolder()); // New parent has no parent
  }

  @Test
  void createTestFolderWithNestedNewParentFolderIntegrationTest() throws Exception {
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("Child folder with nested new parent")
        .name("Child folder with nested parent")
        .parentTestFolder(NewTestFolderRQ.builder()
            .name("New Parent Folder with Grandparent")
            .parentTestFolderId(3L) // New parent will have existing folder 3 as parent
            .build())
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value(request.getName()))
        .andExpect(jsonPath("$.description").value(request.getDescription()))
        .andExpect(jsonPath("$.parentFolderId").exists());

    // Verify hierarchy is created correctly
    var createdChildFolder = tmsTestFolderRepository.findAll().stream()
        .filter(f -> f.getName().equals("Child folder with nested parent"))
        .findFirst();

    assertTrue(createdChildFolder.isPresent());
    assertNotNull(createdChildFolder.get().getParentTestFolder());
    assertEquals("New Parent Folder with Grandparent",
        createdChildFolder.get().getParentTestFolder().getName());
    assertNotNull(createdChildFolder.get().getParentTestFolder().getParentTestFolder());
    assertEquals(3L, createdChildFolder.get().getParentTestFolder().getParentTestFolder().getId());
  }

  @Test
  void createTestFolderWithNonExistentParentIdIntegrationTest() throws Exception {
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("Child folder with invalid parent")
        .name("Child folder invalid parent")
        .parentTestFolderId(999L) // Non-existent folder ID
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("Test Folder with id: 999")));
  }

  @Test
  void createTestFolderWithNewParentHavingNonExistentGrandparentIntegrationTest() throws Exception {
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("Child folder with invalid grandparent")
        .name("Child folder invalid grandparent")
        .parentTestFolder(NewTestFolderRQ.builder()
            .name("New Parent with Invalid Grandparent")
            .parentTestFolderId(999L) // Non-existent grandparent folder ID
            .build())
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("Test Folder with id: 999")));
  }

  @Test
  void createTestFolderWithBothParentOptionsValidationIntegrationTest() throws Exception {
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("Child folder with both parent options")
        .name("Child folder both options")
        .parentTestFolderId(3L)
        .parentTestFolder(NewTestFolderRQ.builder()
            .name("New Parent Folder")
            .build())
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(
            containsString("Either parent folder id or parent folder name should be set")));
  }

  @Test
  void updateTestFolderIntegrationTest() throws Exception {
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("description_updated")
        .name("name_updated")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    mockMvc.perform(put("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/3")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value(request.getName()))
        .andExpect(jsonPath("$.description").value(request.getDescription()));

    Optional<TmsTestFolder> folder = tmsTestFolderRepository.findById(3L);
    assertTrue(folder.isPresent());
    assertEquals(request.getName(), folder.get().getName());
    assertEquals(request.getDescription(), folder.get().getDescription());
    assertEquals(1L, folder.get().getProject().getId());
  }

  @Test
  void updateTestFolderWithNewParentIdIntegrationTest() throws Exception {
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("Updated description with new parent")
        .name("Updated name with new parent")
        .parentTestFolderId(4L)
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    mockMvc.perform(put("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/3")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value(request.getName()))
        .andExpect(jsonPath("$.description").value(request.getDescription()))
        .andExpect(jsonPath("$.parentFolderId").value(4L));

    Optional<TmsTestFolder> folder = tmsTestFolderRepository.findById(3L);
    assertTrue(folder.isPresent());
    assertEquals(request.getName(), folder.get().getName());
    assertEquals(request.getDescription(), folder.get().getDescription());
    assertNotNull(folder.get().getParentTestFolder());
    assertEquals(4L, folder.get().getParentTestFolder().getId());
  }

  @Test
  void updateTestFolderWithNewParentFolderIntegrationTest() throws Exception {
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("Updated description with brand new parent")
        .name("Updated name with brand new parent")
        .parentTestFolder(NewTestFolderRQ.builder()
            .name("Brand New Parent for Update")
            .build())
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    mockMvc.perform(put("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/3")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value(request.getName()))
        .andExpect(jsonPath("$.description").value(request.getDescription()))
        .andExpect(jsonPath("$.parentFolderId").exists());

    Optional<TmsTestFolder> folder = tmsTestFolderRepository.findById(3L);
    assertTrue(folder.isPresent());
    assertEquals(request.getName(), folder.get().getName());
    assertEquals(request.getDescription(), folder.get().getDescription());
    assertNotNull(folder.get().getParentTestFolder());
    assertEquals("Brand New Parent for Update", folder.get().getParentTestFolder().getName());
  }

  @Test
  void patchTestFolderIntegrationTest() throws Exception {
    Optional<TmsTestFolder> originalFolder = tmsTestFolderRepository.findById(4L);
    assertTrue(originalFolder.isPresent());

    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .name("patched_name")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/4")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("patched_name"));

    Optional<TmsTestFolder> updatedFolder = tmsTestFolderRepository.findById(4L);
    assertTrue(updatedFolder.isPresent());
    assertEquals("patched_name", updatedFolder.get().getName());
    assertEquals(originalFolder.get().getDescription(), updatedFolder.get().getDescription());
  }

  @Test
  void patchTestFolderWithNewParentIdIntegrationTest() throws Exception {
    Optional<TmsTestFolder> originalFolder = tmsTestFolderRepository.findById(4L);
    assertTrue(originalFolder.isPresent());

    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .parentTestFolderId(5L)
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/4")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.parentFolderId").value(5L));

    Optional<TmsTestFolder> updatedFolder = tmsTestFolderRepository.findById(4L);
    assertTrue(updatedFolder.isPresent());
    assertEquals(originalFolder.get().getName(), updatedFolder.get().getName());
    assertEquals(originalFolder.get().getDescription(), updatedFolder.get().getDescription());
    assertNotNull(updatedFolder.get().getParentTestFolder());
    assertEquals(5L, updatedFolder.get().getParentTestFolder().getId());
  }

  @Test
  void patchTestFolderNotFoundIntegrationTest() throws Exception {
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .name("patched_name")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/999")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void getTestFolderByIdIntegrationTest() throws Exception {
    Optional<TmsTestFolder> folder = tmsTestFolderRepository.findById(4L);
    assertTrue(folder.isPresent());

    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/4")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(folder.get().getId()))
        .andExpect(jsonPath("$.name").value(folder.get().getName()))
        .andExpect(jsonPath("$.description").value(folder.get().getDescription()))
        .andExpect(jsonPath("$.countOfTestCases").exists());
  }

  @Test
  void getTestFolderByIdNotFoundIntegrationTest() throws Exception {
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/999")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void getFoldersByCriteriaWithoutTestPlanIdIntegrationTest() throws Exception {
    Optional<TmsTestFolder> folder = tmsTestFolderRepository.findById(5L);
    assertTrue(folder.isPresent());

    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[?(@.id == %d)]", folder.get().getId()).exists())
        .andExpect(jsonPath("$.content[?(@.id == %d)].name", folder.get().getId()).value(
            folder.get().getName()))
        .andExpect(jsonPath("$.content[?(@.id == %d)].description", folder.get().getId()).value(
            folder.get().getDescription()));
  }

  @Test
  void getFoldersByCriteriaWithTestPlanIdIntegrationTest() throws Exception {
    // Assuming test plan 4 has some test cases linked to folders
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder")
            .param("testPlanId", "4")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  void getFoldersByCriteriaWithNonExistentTestPlanIdIntegrationTest() throws Exception {
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder")
            .param("filter.eq.testPlanId", "999")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0));
  }

  @Test
  void getFoldersByCriteriaWithInvalidTestPlanIdIntegrationTest() throws Exception {
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder")
            .param("filter.eq.testPlanId", "invalid")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testDeleteTestFolder() throws Exception {
    Optional<TmsTestFolder> folder = tmsTestFolderRepository.findById(5L);
    assertTrue(folder.isPresent());

    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/5")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    boolean exists = tmsTestFolderRepository.existsById(5L);
    assertFalse(exists);
  }

  @Test
  void deleteNonExistentTestFolderIntegrationTest() throws Exception {
    mockMvc
        .perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/999")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk()); // Delete is idempotent in this implementation
  }

  @Test
  void testExportFolderToCsv() throws Exception {
    // Arrange
    long folderId = 3L;
    var fileType = TmsTestFolderExportFileType.CSV;

    // Verify folder exists before export
    var folder = tmsTestFolderRepository.findById(folderId);
    assertTrue(folder.isPresent());

    // Act & Assert
    var result = mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/export/{fileType}",
                folderId, fileType)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
        .andExpect(header().string("Content-Disposition",
            String.format("attachment; filename=\"test_folder_%d_export.csv\"", folderId)))
        .andReturn();

    // Additional verification for CSV content
    var content = result.getResponse().getContentAsString();
    assertTrue(content.contains("Id,Name,Description,Level,Path,Parent Id"));
    assertTrue(content.contains(String.valueOf(folderId)));
    assertTrue(content.contains(folder.get().getName()));
  }

  @Test
  void testExportFolderToCsvWithHierarchy() throws Exception {
    // This test assumes folder 3 has subfolders
    long folderId = 3L;
    var fileType = TmsTestFolderExportFileType.CSV;

    // Verify folder exists before export
    var folder = tmsTestFolderRepository.findById(folderId);
    assertTrue(folder.isPresent());

    // Act & Assert
    var result = mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/export/{fileType}",
                folderId, fileType)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();

    // Verify that the CSV contains multiple lines (header + at least one data row)
    var content = result.getResponse().getContentAsString();
    var lines = content.split("\n");
    assertTrue(lines.length > 1, "Export should contain at least header and one data row");
  }

  @Test
  void testExportNonExistingFolderToCsv() throws Exception {
    // Arrange
    long nonExistentFolderId = 999L;
    var fileType = TmsTestFolderExportFileType.CSV;

    // Verify folder doesn't exist
    var folder = tmsTestFolderRepository.findById(nonExistentFolderId);
    assertFalse(folder.isPresent());

    // Act & Assert - should return 404 Not Found
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/export/{fileType}",
                nonExistentFolderId, fileType)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }


  @Test
  void duplicateTestFolder_RootFolder_Success() throws Exception {
    // Arrange
    Long sourceFolderId = 3L;
    Optional<TmsTestFolder> sourceFolder = tmsTestFolderRepository.findById(sourceFolderId);
    assertTrue(sourceFolder.isPresent());

    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .name("Duplicated Folder")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    // Act
    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/duplicate",
                sourceFolderId)
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Duplicated Folder"))
        .andExpect(jsonPath("$.folderDuplicationStatistic").exists())
        .andExpect(jsonPath("$.folderDuplicationStatistic.totalCount").exists())
        .andExpect(jsonPath("$.folderDuplicationStatistic.successCount").exists())
        .andExpect(jsonPath("$.testCaseDuplicationStatistic").exists())
        .andExpect(jsonPath("$.testCaseDuplicationStatistic.totalCount").exists())
        .andExpect(jsonPath("$.subFolders").doesNotExist())
        .andReturn();

    // Parse response
    var responseContent = result.getResponse().getContentAsString();
    DuplicateTmsTestFolderRS response = mapper.readValue(responseContent,
        DuplicateTmsTestFolderRS.class);

    // Assert - verify duplicated folder exists in database
    Optional<TmsTestFolder> duplicatedFolder = tmsTestFolderRepository.findById(response.getId());
    assertTrue(duplicatedFolder.isPresent());
    assertEquals("Duplicated Folder", duplicatedFolder.get().getName());
    assertNotEquals(sourceFolderId, duplicatedFolder.get().getId());

    // Assert - verify statistics
    assertNotNull(response.getFolderDuplicationStatistic());
    assertTrue(response.getFolderDuplicationStatistic().getSuccessCount() > 0);
    assertEquals(0, response.getFolderDuplicationStatistic().getFailureCount());
  }

  @Test
  void duplicateTestFolder_WithExistingParentId_Success() throws Exception {
    // Arrange
    Long sourceFolderId = 4L;
    Long targetParentId = 3L;

    Optional<TmsTestFolder> sourceFolder = tmsTestFolderRepository.findById(sourceFolderId);
    assertTrue(sourceFolder.isPresent());

    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .name("Duplicated Into Existing Parent")
        .parentTestFolderId(targetParentId)
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    // Act
    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/duplicate",
                sourceFolderId)
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Duplicated Into Existing Parent"))
        .andExpect(jsonPath("$.parentFolderId").value(targetParentId))
        .andExpect(jsonPath("$.subFolders").doesNotExist())
        .andReturn();

    // Parse response
    var responseContent = result.getResponse().getContentAsString();
    DuplicateTmsTestFolderRS response = mapper.readValue(responseContent,
        DuplicateTmsTestFolderRS.class);

    // Assert - verify duplicated folder has correct parent
    Optional<TmsTestFolder> duplicatedFolder = tmsTestFolderRepository.findById(response.getId());
    assertTrue(duplicatedFolder.isPresent());
    assertNotNull(duplicatedFolder.get().getParentTestFolder());
    assertEquals(targetParentId, duplicatedFolder.get().getParentTestFolder().getId());
  }

  @Test
  void duplicateTestFolder_WithNewParentFolder_Success() throws Exception {
    // Arrange
    var sourceFolderId = 4L;

    var request = TmsTestFolderRQ
        .builder()
        .name("Duplicated With New Parent")
        .parentTestFolder(NewTestFolderRQ.builder()
            .name("Brand New Parent Folder")
            .build())
        .build();
    var mapper = new ObjectMapper();
    var jsonContent = mapper.writeValueAsString(request);

    // Act
    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/duplicate",
                sourceFolderId)
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Duplicated With New Parent"))
        .andExpect(jsonPath("$.parentFolderId").exists())
        .andReturn();

    // Parse response
    var responseContent = result.getResponse().getContentAsString();
    DuplicateTmsTestFolderRS response = mapper.readValue(responseContent,
        DuplicateTmsTestFolderRS.class);

    // Assert - verify duplicated folder has correct parent
    Optional<TmsTestFolder> duplicatedFolder = tmsTestFolderRepository.findById(response.getId());
    assertTrue(duplicatedFolder.isPresent());
    assertNotNull(duplicatedFolder.get().getParentTestFolder());
    assertEquals("Brand New Parent Folder", duplicatedFolder.get().getParentTestFolder().getName());
  }

  @Test
  void duplicateTestFolder_WithNewNestedParentFolder_Success() throws Exception {
    // Arrange
    Long sourceFolderId = 4L;
    Long grandparentId = 3L;

    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .name("Duplicated With Nested Parent")
        .parentTestFolder(NewTestFolderRQ.builder()
            .name("New Nested Parent")
            .parentTestFolderId(grandparentId)
            .build())
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    // Act
    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/duplicate",
                sourceFolderId)
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Duplicated With Nested Parent"))
        .andExpect(jsonPath("$.parentFolderId").exists())
        .andExpect(jsonPath("$.subFolders").doesNotExist())
        .andReturn();

    // Parse response
    var responseContent = result.getResponse().getContentAsString();
    DuplicateTmsTestFolderRS response = mapper.readValue(responseContent,
        DuplicateTmsTestFolderRS.class);

    // Assert - verify hierarchy
    Optional<TmsTestFolder> duplicatedFolder = tmsTestFolderRepository.findById(response.getId());
    assertTrue(duplicatedFolder.isPresent());
    assertNotNull(duplicatedFolder.get().getParentTestFolder());
    assertEquals("New Nested Parent", duplicatedFolder.get().getParentTestFolder().getName());
    assertNotNull(duplicatedFolder.get().getParentTestFolder().getParentTestFolder());
    assertEquals(grandparentId,
        duplicatedFolder.get().getParentTestFolder().getParentTestFolder().getId());
  }

  @Test
  void duplicateTestFolder_WithSubfolders_DuplicatesHierarchy() throws Exception {
    // Arrange - folder 3 should have subfolders in test data
    Long sourceFolderId = 3L;
    Optional<TmsTestFolder> sourceFolder = tmsTestFolderRepository.findById(sourceFolderId);
    assertTrue(sourceFolder.isPresent());

    // Get count of subfolders before duplication
    List<Long> allFolderIds = tmsTestFolderRepository.findAllFolderIdsInHierarchy(1L,
        sourceFolderId);
    int expectedFolderCount = allFolderIds.size();

    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .name("Duplicated Hierarchy")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    // Act
    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/duplicate",
                sourceFolderId)
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Duplicated Hierarchy"))
        .andExpect(jsonPath("$.folderDuplicationStatistic.totalCount").value(expectedFolderCount))
        .andExpect(jsonPath("$.folderDuplicationStatistic.successCount").value(expectedFolderCount))
        .andExpect(jsonPath("$.folderDuplicationStatistic.failureCount").value(0))
        .andExpect(jsonPath("$.subFolders").doesNotExist())
        .andReturn();

    // Parse response
    var responseContent = result.getResponse().getContentAsString();
    DuplicateTmsTestFolderRS response = mapper.readValue(responseContent,
        DuplicateTmsTestFolderRS.class);

    // Assert - verify all folders in hierarchy are duplicated
    List<Long> duplicatedHierarchyIds = tmsTestFolderRepository.findAllFolderIdsInHierarchy(1L,
        response.getId());
    assertEquals(expectedFolderCount, duplicatedHierarchyIds.size());

    // Assert - verify duplicated root folder
    Optional<TmsTestFolder> duplicatedRoot = tmsTestFolderRepository.findById(response.getId());
    assertTrue(duplicatedRoot.isPresent());
    assertEquals("Duplicated Hierarchy", duplicatedRoot.get().getName());
  }

  @Test
  void duplicateTestFolder_WithTestCases_DuplicatesTestCases() throws Exception {
    // Arrange - use folder that has test cases
    Long sourceFolderId = 3L;
    Optional<TmsTestFolder> sourceFolder = tmsTestFolderRepository.findById(sourceFolderId);
    assertTrue(sourceFolder.isPresent());

    // Get test cases count in source folder hierarchy
    List<Long> sourceFolderIds = tmsTestFolderRepository.findAllFolderIdsInHierarchy(1L,
        sourceFolderId);
    long originalTestCaseCount = tmsTestCaseRepository.findAll().stream()
        .filter(tc -> sourceFolderIds.contains(tc.getTestFolder().getId()))
        .count();

    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .name("Duplicated With Test Cases")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    // Act
    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/duplicate",
                sourceFolderId)
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Duplicated With Test Cases"))
        .andExpect(jsonPath("$.testCaseDuplicationStatistic").exists())
        .andExpect(jsonPath("$.testCaseDuplicationStatistic.totalCount").value(
            greaterThanOrEqualTo(0)))
        .andExpect(jsonPath("$.subFolders").doesNotExist())
        .andReturn();

    // Parse response
    var responseContent = result.getResponse().getContentAsString();
    DuplicateTmsTestFolderRS response = mapper.readValue(responseContent,
        DuplicateTmsTestFolderRS.class);

    // Assert - verify test cases are duplicated
    if (originalTestCaseCount > 0) {
      assertTrue(response.getTestCaseDuplicationStatistic().getSuccessCount() > 0);
      assertEquals(originalTestCaseCount,
          response.getTestCaseDuplicationStatistic().getTotalCount().longValue());
    }
  }

  @Test
  void duplicateTestFolder_WithNameConflict_GeneratesUniqueName() throws Exception {
    // Arrange
    Long sourceFolderId = 3L;

    // First duplication
    TmsTestFolderRQ request1 = TmsTestFolderRQ.builder()
        .name("Duplicate Name Test")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent1 = mapper.writeValueAsString(request1);

    var result1 = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/duplicate",
                sourceFolderId)
                .contentType("application/json")
                .content(jsonContent1)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Duplicate Name Test"))
        .andReturn();

    // Second duplication with same name
    TmsTestFolderRQ request2 = TmsTestFolderRQ.builder()
        .name("Duplicate Name Test")
        .build();
    String jsonContent2 = mapper.writeValueAsString(request2);

    var result2 = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/duplicate",
                sourceFolderId)
                .contentType("application/json")
                .content(jsonContent2)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Duplicate Name Test-1"))
        .andExpect(jsonPath("$.subFolders").doesNotExist())
        .andReturn();

    // Parse responses
    var responseContent1 = result1.getResponse().getContentAsString();
    var responseContent2 = result2.getResponse().getContentAsString();
    DuplicateTmsTestFolderRS response1 = mapper.readValue(responseContent1,
        DuplicateTmsTestFolderRS.class);
    DuplicateTmsTestFolderRS response2 = mapper.readValue(responseContent2,
        DuplicateTmsTestFolderRS.class);

    // Assert - verify both folders exist with different IDs and names
    assertNotEquals(response1.getId(), response2.getId());
    assertEquals("Duplicate Name Test", response1.getName());
    assertEquals("Duplicate Name Test-1", response2.getName());
  }

  @Test
  void duplicateTestFolder_NonExistentSourceFolder_ReturnsNotFound() throws Exception {
    // Arrange
    Long nonExistentFolderId = 999L;

    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .name("Should Not Be Created")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    // Act & Assert
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/duplicate",
                nonExistentFolderId)
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("Test Folder with id: 999")));
  }

  @Test
  void duplicateTestFolder_WithNonExistentParentId_ReturnsNotFound() throws Exception {
    // Arrange
    Long sourceFolderId = 3L;
    Long nonExistentParentId = 999L;

    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .name("Duplicated Folder")
        .parentTestFolderId(nonExistentParentId)
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    // Act & Assert
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/duplicate",
                sourceFolderId)
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("Test Folder with id: 999")));
  }

  @Test
  void duplicateTestFolder_WithBothParentOptions_ReturnsBadRequest() throws Exception {
    // Arrange
    Long sourceFolderId = 3L;

    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .name("Duplicated Folder")
        .parentTestFolderId(4L)
        .parentTestFolder(NewTestFolderRQ.builder()
            .name("New Parent")
            .build())
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    // Act & Assert
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/duplicate",
                sourceFolderId)
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString(
            "Either parent folder id or parent folder name should be set")));
  }

  @Test
  void duplicateTestFolder_MultipleTimesWithIncrement_Success() throws Exception {
    // Arrange
    Long sourceFolderId = 3L;

    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .name("Multi Duplicate Test")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    // Act - First duplication
    var result1 = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/duplicate",
                sourceFolderId)
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Multi Duplicate Test"))
        .andReturn();

    // Act - Second duplication
    var result2 = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/duplicate",
                sourceFolderId)
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Multi Duplicate Test-1"))
        .andReturn();

    // Act - Third duplication
    var result3 = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/duplicate",
                sourceFolderId)
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Multi Duplicate Test-2"))
        .andReturn();

    // Parse responses
    var responseContent1 = result1.getResponse().getContentAsString();
    var responseContent2 = result2.getResponse().getContentAsString();
    var responseContent3 = result3.getResponse().getContentAsString();
    DuplicateTmsTestFolderRS response1 = mapper.readValue(responseContent1,
        DuplicateTmsTestFolderRS.class);
    DuplicateTmsTestFolderRS response2 = mapper.readValue(responseContent2,
        DuplicateTmsTestFolderRS.class);
    DuplicateTmsTestFolderRS response3 = mapper.readValue(responseContent3,
        DuplicateTmsTestFolderRS.class);

    // Assert - all three folders have unique IDs and names
    assertNotEquals(response1.getId(), response2.getId());
    assertNotEquals(response2.getId(), response3.getId());
    assertNotEquals(response1.getId(), response3.getId());

    assertEquals("Multi Duplicate Test", response1.getName());
    assertEquals("Multi Duplicate Test-1", response2.getName());
    assertEquals("Multi Duplicate Test-2", response3.getName());
  }

  @Test
  void duplicateTestFolder_ComplexHierarchyWithTestCases_Success() throws Exception {
    // Arrange - folder 3 should have complex hierarchy with subfolders and test cases
    Long sourceFolderId = 3L;

    // Get source folder structure
    List<Long> sourceFolderIds = tmsTestFolderRepository.findAllFolderIdsInHierarchy(1L,
        sourceFolderId);
    int expectedFolderCount = sourceFolderIds.size();

    // Get test cases count
    long originalTestCaseCount = tmsTestCaseRepository.findAll().stream()
        .filter(tc -> sourceFolderIds.contains(tc.getTestFolder().getId()))
        .count();

    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .name("Complex Hierarchy Duplicate")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    // Act
    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/duplicate",
                sourceFolderId)
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Complex Hierarchy Duplicate"))
        .andExpect(jsonPath("$.folderDuplicationStatistic").exists())
        .andExpect(jsonPath("$.folderDuplicationStatistic.totalCount").value(expectedFolderCount))
        .andExpect(jsonPath("$.testCaseDuplicationStatistic").exists())
        .andReturn();

    // Parse response
    var responseContent = result.getResponse().getContentAsString();
    DuplicateTmsTestFolderRS response = mapper.readValue(responseContent,
        DuplicateTmsTestFolderRS.class);

    // Assert - verify folder hierarchy
    List<Long> duplicatedFolderIds = tmsTestFolderRepository.findAllFolderIdsInHierarchy(1L,
        response.getId());
    assertEquals(expectedFolderCount, duplicatedFolderIds.size());

    // Assert - verify test cases are duplicated and in correct folders
    if (originalTestCaseCount > 0) {

      // Verify total test case count increased
      long totalTestCaseCount = tmsTestCaseRepository.count();
      assertTrue(totalTestCaseCount >= originalTestCaseCount * 2);
    }

    // Assert - verify statistics
    assertEquals(expectedFolderCount,
        response.getFolderDuplicationStatistic().getTotalCount().intValue());
    assertEquals(0, response.getFolderDuplicationStatistic().getFailureCount());
    assertTrue(response.getFolderDuplicationStatistic().getSuccessCount() > 0);
  }

  @Test
  void duplicateTestFolder_EmptyFolder_Success() throws Exception {
    // Arrange - create empty folder first
    TmsTestFolderRQ createRequest = TmsTestFolderRQ.builder()
        .name("Empty Folder For Duplication")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String createJsonContent = mapper.writeValueAsString(createRequest);

    var createResult = mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder")
            .contentType("application/json")
            .content(createJsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();

    var createResponseContent = createResult.getResponse().getContentAsString();
    TmsTestFolderRS createdFolder = mapper.readValue(createResponseContent, TmsTestFolderRS.class);
    Long createdFolderId = createdFolder.getId();

    // Act - Duplicate empty folder
    TmsTestFolderRQ duplicateRequest = TmsTestFolderRQ.builder()
        .name("Duplicated Empty Folder")
        .build();
    String duplicateJsonContent = mapper.writeValueAsString(duplicateRequest);

    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/duplicate",
                createdFolderId)
                .contentType("application/json")
                .content(duplicateJsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Duplicated Empty Folder"))
        .andExpect(jsonPath("$.countOfTestCases").value(0))
        .andExpect(jsonPath("$.folderDuplicationStatistic.totalCount").value(1))
        .andExpect(jsonPath("$.folderDuplicationStatistic.successCount").value(1))
        .andExpect(jsonPath("$.testCaseDuplicationStatistic.totalCount").value(0))
        .andExpect(jsonPath("$.testCaseDuplicationStatistic.successCount").value(0))
        .andReturn();

    // Parse response
    var responseContent = result.getResponse().getContentAsString();
    DuplicateTmsTestFolderRS response = mapper.readValue(responseContent,
        DuplicateTmsTestFolderRS.class);

    // Assert
    Optional<TmsTestFolder> duplicatedFolder = tmsTestFolderRepository.findById(response.getId());
    assertTrue(duplicatedFolder.isPresent());
    assertEquals("Duplicated Empty Folder", duplicatedFolder.get().getName());
    assertEquals(0L, response.getCountOfTestCases());
  }

  @Test
  void duplicateTestFolder_SubfolderIntoParent_Success() throws Exception {
    // Arrange - duplicate subfolder and place it into a different parent
    Long sourceFolderId = 4L; // Assuming this is a subfolder
    Long newParentId = 5L;

    Optional<TmsTestFolder> sourceFolder = tmsTestFolderRepository.findById(sourceFolderId);
    assertTrue(sourceFolder.isPresent());

    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .name("Moved Subfolder Duplicate")
        .parentTestFolderId(newParentId)
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    // Act
    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/duplicate",
                sourceFolderId)
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Moved Subfolder Duplicate"))
        .andExpect(jsonPath("$.parentFolderId").value(newParentId))
        .andReturn();

    // Parse response
    var responseContent = result.getResponse().getContentAsString();
    DuplicateTmsTestFolderRS response = mapper.readValue(responseContent,
        DuplicateTmsTestFolderRS.class);

    // Assert - verify folder is in new parent
    Optional<TmsTestFolder> duplicatedFolder = tmsTestFolderRepository.findById(response.getId());
    assertTrue(duplicatedFolder.isPresent());
    assertNotNull(duplicatedFolder.get().getParentTestFolder());
    assertEquals(newParentId, duplicatedFolder.get().getParentTestFolder().getId());
  }

  @Test
  void duplicateTestFolder_WithNewParentFolderHavingInvalidGrandparent_ReturnsNotFound()
      throws Exception {
    // Arrange
    Long sourceFolderId = 3L;

    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .name("Duplicated Folder")
        .parentTestFolder(NewTestFolderRQ.builder()
            .name("New Parent")
            .parentTestFolderId(999L) // Non-existent grandparent
            .build())
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    // Act & Assert
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/duplicate",
                sourceFolderId)
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("Test Folder with id: 999")));
  }

  @Test
  void duplicateTestFolder_VerifySubfoldersHaveCorrectNames() throws Exception {
    // Arrange - folder 3 should have subfolders
    Long sourceFolderId = 3L;

    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .name("Root Duplicate For Name Test")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    // Act
    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/duplicate",
                sourceFolderId)
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Root Duplicate For Name Test"))
        .andReturn();

    // Parse response
    var responseContent = result.getResponse().getContentAsString();
    DuplicateTmsTestFolderRS response = mapper.readValue(responseContent,
        DuplicateTmsTestFolderRS.class);

    // Assert - verify subfolders have "-copy" suffix
    List<Long> duplicatedFolderIds = tmsTestFolderRepository.findAllFolderIdsInHierarchy(1L,
        response.getId());
    List<TmsTestFolder> duplicatedFolders = tmsTestFolderRepository.findAllById(
        duplicatedFolderIds);

    // Get subfolders (all except root)
    var subfolders = duplicatedFolders.stream()
        .filter(f -> !f.getId().equals(response.getId()))
        .toList();

    // All subfolders should have names ending with "-copy" or "-copy-N"
    for (TmsTestFolder subfolder : subfolders) {
      assertTrue(subfolder.getName().contains("-copy"),
          "Subfolder name should contain '-copy': " + subfolder.getName());
    }
  }
}
