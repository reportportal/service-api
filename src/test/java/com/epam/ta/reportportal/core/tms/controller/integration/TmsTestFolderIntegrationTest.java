package com.epam.ta.reportportal.core.tms.controller.integration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolder;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestFolderRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderExportFileType;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRQ;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql("/db/tms/tms-product-version/tms-test-folder-fill.sql")
@ExtendWith(MockitoExtension.class)
class TmsTestFolderIntegrationTest extends BaseMvcTest {

  private static final String SUPERADMIN_PROJECT_KEY = "superadmin_personal";

  @Autowired
  private TmsTestFolderRepository tmsTestFolderRepository;

  @Test
  void createTestFolderIntegrationTest() throws Exception {
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
        .andExpect(status().isOk());

    Optional<TmsTestFolder> folder = tmsTestFolderRepository.findById(1L);
    assertTrue(folder.isPresent());
    assertEquals(request.getName(), folder.get().getName());
    assertEquals(request.getDescription(), folder.get().getDescription());
    assertEquals(1L, folder.get().getProject().getId());
  }

  @Test
  void updateTestFolderIntegrationTest() throws Exception {
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("description_create")
        .name("name_create")
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
  void getTestFolderByIdIntegrationTest() throws Exception {
    Optional<TmsTestFolder> folder = tmsTestFolderRepository.findById(4L);
    assertTrue(folder.isPresent());

    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/4")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(folder.get().getId()))
        .andExpect(jsonPath("$.name").value(folder.get().getName()))
        .andExpect(jsonPath("$.description").value(folder.get().getDescription()))
        .andExpect(jsonPath("$.countOfTestCases").exists())
        .andExpect(jsonPath("$.subFolders").exists());
  }

  @Test
  void testGetTestFolderByProjectId() throws Exception {
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
  void testGetSubfolders() throws Exception {
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/3/sub-folder")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[*].countOfTestCases").exists())
        .andExpect(jsonPath("$.content[*].parentFolderId").exists())
        .andExpect(jsonPath("$.content[*].subFolders").exists());
  }

  @Test
  void testDeleteTestFolder() throws Exception {
    Optional<TmsTestFolder> folder = tmsTestFolderRepository.findById(5L);
    assertTrue(folder.isPresent());

    mockMvc
        .perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/5")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    boolean exists = tmsTestFolderRepository.existsById(5L);
    assertFalse(exists);
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
    var result = assertThrows(jakarta.servlet.ServletException.class, () -> mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/folder/{folderId}/export/{fileType}",
                nonExistentFolderId, fileType)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andReturn());

    assertThat(result.getMessage()).contains("Test Folder cannot be found by id");
  }
}
