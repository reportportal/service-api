package com.epam.reportportal.base.core.tms.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.auth.OAuthHelper;
import com.epam.reportportal.base.core.tms.dto.UploadAttachmentRS;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsAttachmentRepository;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

class TmsAttachmentIntegrationTest extends BaseMvcTest {

  private static final String SUPERADMIN_PROJECT_KEY = "superadmin_personal";

  private final ObjectMapper mapper = new ObjectMapper();

  @Autowired
  private OAuthHelper oAuthHelper;

  @Autowired
  private TmsAttachmentRepository tmsAttachmentRepository;

  @Test
  void uploadTmsAttachment_ShouldUploadSuccessfully_WhenValidFile() throws Exception {
    // Given valid file to upload
    var file = new MockMultipartFile(
        "file",
        "test-attachment.pdf",
        "application/pdf",
        "test attachment content".getBytes()
    );

    // When uploading attachment
    var result = mockMvc.perform(
            multipart("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/attachment/upload")
                .file(file)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.fileName").value("test-attachment.pdf"))
        .andReturn();

    // Then verify response and database state
    var responseBody = result.getResponse().getContentAsString();
    var uploadResponse = mapper.readValue(responseBody, UploadAttachmentRS.class);

    assertNotNull(uploadResponse.getId());
    assertEquals("test-attachment.pdf", uploadResponse.getFileName());

    var attachment = tmsAttachmentRepository.findById(uploadResponse.getId());
    assertTrue(attachment.isPresent());
    assertEquals("test-attachment.pdf", attachment.get().getFileName());
    assertEquals("application/pdf", attachment.get().getFileType());
    assertNotNull(attachment.get().getExpiresAt()); // Should have TTL set
  }

  @Test
  void uploadTmsAttachment_ShouldReturnBadRequest_WhenEmptyFile() throws Exception {
    // Given empty file
    var emptyFile = new MockMultipartFile(
        "file",
        "empty.txt",
        "text/plain",
        new byte[0]
    );

    // When uploading empty file
    mockMvc.perform(
            multipart("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/attachment/upload")
                .file(emptyFile)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void downloadTmsAttachment_ShouldDownloadSuccessfully_WhenAttachmentExists() throws Exception {
    // Given uploaded attachment
    var uploadResponse = uploadTestAttachment("download-test.txt", "text/plain");

    // When downloading the attachment
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/attachment/" + uploadResponse.getId())
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Disposition",
            "attachment; filename=\"download-test.txt\""))
        .andExpect(header().string("Content-Type", "text/plain"));
  }

  @Test
  void downloadTmsAttachment_ShouldReturnNotFound_WhenAttachmentDoesNotExist() throws Exception {
    // When downloading non-existent attachment
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/attachment/999999")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void deleteTmsAttachment_ShouldDeleteSuccessfully_WhenAttachmentExists() throws Exception {
    // Given uploaded attachment
    var uploadResponse = uploadTestAttachment("delete-test.txt", "text/plain");

    // When deleting the attachment
    mockMvc.perform(
            delete(
                "/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/attachment/" + uploadResponse.getId())
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value(
            "Attachment with ID = '" + uploadResponse.getId() + "' successfully deleted."));

    // Then attachment should be deleted from database
    var deletedAttachment = tmsAttachmentRepository.findById(uploadResponse.getId());
    assertFalse(deletedAttachment.isPresent());
  }

  @Test
  void deleteTmsAttachment_ShouldReturnNotFound_WhenAttachmentDoesNotExist() throws Exception {
    // When deleting non-existent attachment
    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/attachment/999999")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void uploadedAttachment_ShouldHaveTtlSet_WhenNotUsedInTestCase() throws Exception {
    // Given uploaded attachment not used in any test case
    var uploadResponse = uploadTestAttachment("ttl-test.txt", "text/plain");

    // Then verify attachment has a TTL set
    var attachment = tmsAttachmentRepository.findById(uploadResponse.getId());
    assertTrue(attachment.isPresent());
    assertNotNull(attachment.get().getExpiresAt());
    assertTrue(attachment.get().getExpiresAt().isAfter(Instant.now()));
  }

  @Test
  void uploadMultipleAttachmentTypes_ShouldSupportDifferentFileTypes() throws Exception {
    // Given different file types
    var testFiles = List.of(
        new Object[]{"image.png", "image/png"},
        new Object[]{"document.pdf", "application/pdf"},
        new Object[]{"spreadsheet.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
        new Object[]{"text.txt", "text/plain"},
        new Object[]{"archive.zip", "application/zip"}
    );

    for (var fileData : testFiles) {
      var fileName = (String) fileData[0];
      var contentType = (String) fileData[1];

      // When uploading different file types
      var uploadResponse = uploadTestAttachment(fileName, contentType);

      // Then verify each file is uploaded correctly
      assertNotNull(uploadResponse.getId());
      assertEquals(fileName, uploadResponse.getFileName());

      var attachment = tmsAttachmentRepository.findById(uploadResponse.getId());
      assertTrue(attachment.isPresent());
      assertEquals(contentType, attachment.get().getFileType());
    }
  }

  @Test
  void uploadLargeAttachment_ShouldHandleLargeFiles() throws Exception {
    // Given large file (1MB)
    var largeContent = new byte[1024 * 1024];
    var largeFile = new MockMultipartFile(
        "file",
        "large-document.pdf",
        "application/pdf",
        largeContent
    );

    // When uploading large file
    var result = mockMvc.perform(
            multipart("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/attachment/upload")
                .file(largeFile)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fileName").value("large-document.pdf"))
        .andReturn();

    // Then verify large file is stored correctly
    var uploadResponse = mapper.readValue(result.getResponse().getContentAsString(),
        UploadAttachmentRS.class);

    var attachment = tmsAttachmentRepository.findById(uploadResponse.getId());
    assertTrue(attachment.isPresent());
    assertEquals(1024 * 1024, attachment.get().getFileSize());
  }

  @Test
  void uploadAttachmentWithSpecialCharacters_ShouldPreserveFileName() throws Exception {
    // Given file with special characters in name
    var specialFileName = "test file with spaces & symbols (1).txt";
    var file = new MockMultipartFile(
        "file",
        specialFileName,
        "text/plain",
        "content with special chars".getBytes()
    );

    // When uploading file with special characters
    var result = mockMvc.perform(
            multipart("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/attachment/upload")
                .file(file)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fileName").value(specialFileName))
        .andReturn();

    // Then verify special characters are preserved
    var uploadResponse = mapper.readValue(result.getResponse().getContentAsString(),
        UploadAttachmentRS.class);

    var attachment = tmsAttachmentRepository.findById(uploadResponse.getId());
    assertTrue(attachment.isPresent());
    assertEquals(specialFileName, attachment.get().getFileName());
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
}
