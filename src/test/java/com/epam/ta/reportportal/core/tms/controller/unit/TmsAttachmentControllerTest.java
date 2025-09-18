package com.epam.ta.reportportal.core.tms.controller.unit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.binary.impl.AttachmentDataStoreService;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.tms.controller.TmsAttachmentController;
import com.epam.ta.reportportal.core.tms.db.entity.TmsAttachment;
import com.epam.ta.reportportal.core.tms.dto.UploadAttachmentRS;
import com.epam.ta.reportportal.core.tms.service.TmsAttachmentService;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class TmsAttachmentControllerTest {

  private final long projectId = 1L;
  private final String projectKey = "test_project";

  @Mock
  private TmsAttachmentService tmsAttachmentService;

  @Mock
  private AttachmentDataStoreService attachmentDataStoreService;

  @Mock
  private ProjectExtractor projectExtractor;

  @InjectMocks
  private TmsAttachmentController tmsAttachmentController;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private ReportPortalUser testUser;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    objectMapper = new ObjectMapper();

    // Create a test user
    testUser = ReportPortalUser.userBuilder()
        .withUserName("testUser")
        .withPassword("password")
        .withUserId(1L)
        .withActive(true)
        .withAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
        .build();

    // Configure MockMvc with a custom argument resolver for @AuthenticationPrincipal
    mockMvc = standaloneSetup(tmsAttachmentController)
        .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
          @Override
          public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterAnnotation(AuthenticationPrincipal.class) != null;
          }

          @Override
          public Object resolveArgument(MethodParameter parameter,
              ModelAndViewContainer mavContainer,
              NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return testUser;
          }
        })
        .build();

    // Setup the project extractor mock to return a MembershipDetails with the projectId
    var membershipDetails = MembershipDetails.builder()
        .withProjectId(projectId)
        .withProjectKey(projectKey)
        .build();
    given(projectExtractor.extractMembershipDetails(eq(testUser), anyString()))
        .willReturn(membershipDetails);
  }

  @Test
  void uploadAttachmentTest() throws Exception {
    // Given
    var fileContent = "test attachment content";
    var file = new MockMultipartFile("file", "test.txt", "text/plain", fileContent.getBytes());

    var uploadResponse = UploadAttachmentRS.builder()
        .id(1L)
        .fileName("test.txt")
        .fileSize(22L)
        .fileType("text/plain")
        .build();

    given(tmsAttachmentService.uploadAttachment(any())).willReturn(uploadResponse);

    // When/Then
    mockMvc.perform(
            multipart("/v1/project/{projectKey}/tms/attachment/upload", projectKey)
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.fileName").value("test.txt"))
        .andExpect(jsonPath("$.fileSize").value(22L))
        .andExpect(jsonPath("$.fileType").value("text/plain"));

    verify(tmsAttachmentService).uploadAttachment(any());
  }

  @Test
  void uploadAttachmentWithImageTest() throws Exception {
    // Given
    var imageContent = new byte[]{1, 2, 3, 4, 5}; // Fake image content
    var file = new MockMultipartFile("file", "image.png", "image/png", imageContent);

    var uploadResponse = UploadAttachmentRS.builder()
        .id(2L)
        .fileName("image.png")
        .fileSize(5L)
        .fileType("image/png")
        .build();

    given(tmsAttachmentService.uploadAttachment(any())).willReturn(uploadResponse);

    // When/Then
    mockMvc.perform(
            multipart("/v1/project/{projectKey}/tms/attachment/upload", projectKey)
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(2L))
        .andExpect(jsonPath("$.fileName").value("image.png"))
        .andExpect(jsonPath("$.fileSize").value(5L))
        .andExpect(jsonPath("$.fileType").value("image/png"));

    verify(tmsAttachmentService).uploadAttachment(any());
  }

  @Test
  void uploadAttachmentWithPdfTest() throws Exception {
    // Given
    var pdfContent = "%PDF-1.4 fake pdf content".getBytes();
    var file = new MockMultipartFile("file", "document.pdf", "application/pdf", pdfContent);

    var uploadResponse = UploadAttachmentRS.builder()
        .id(3L)
        .fileName("document.pdf")
        .fileSize((long) pdfContent.length)
        .fileType("application/pdf")
        .build();

    given(tmsAttachmentService.uploadAttachment(any())).willReturn(uploadResponse);

    // When/Then
    mockMvc.perform(
            multipart("/v1/project/{projectKey}/tms/attachment/upload", projectKey)
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(3L))
        .andExpect(jsonPath("$.fileName").value("document.pdf"))
        .andExpect(jsonPath("$.fileSize").value(pdfContent.length))
        .andExpect(jsonPath("$.fileType").value("application/pdf"));

    verify(tmsAttachmentService).uploadAttachment(any());
  }

  @Test
  void downloadAttachmentTest() throws Exception {
    // Given
    var attachmentId = 1L;
    var attachment = new TmsAttachment();
    attachment.setId(attachmentId);
    attachment.setFileName("test.txt");
    attachment.setFileType("text/plain");
    attachment.setFileSize(22L);
    attachment.setPathToFile("/path/to/file");

    var fileContent = "test attachment content";
    InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());

    given(tmsAttachmentService.getTmsAttachment(attachmentId)).willReturn(Optional.of(attachment));
    given(attachmentDataStoreService.load("/path/to/file")).willReturn(Optional.of(inputStream));

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/attachment/{attachmentId}", projectKey, attachmentId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"test.txt\""))
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/plain"))
        .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, "22"));

    verify(tmsAttachmentService).getTmsAttachment(attachmentId);
    verify(attachmentDataStoreService).load("/path/to/file");
  }

  @Test
  void downloadAttachmentWithImageTest() throws Exception {
    // Given
    var attachmentId = 2L;
    var attachment = new TmsAttachment();
    attachment.setId(attachmentId);
    attachment.setFileName("image.png");
    attachment.setFileType("image/png");
    attachment.setFileSize(1024L);
    attachment.setPathToFile("/path/to/image.png");

    var imageContent = new byte[1024];
    InputStream inputStream = new ByteArrayInputStream(imageContent);

    given(tmsAttachmentService.getTmsAttachment(attachmentId)).willReturn(Optional.of(attachment));
    given(attachmentDataStoreService.load("/path/to/image.png")).willReturn(
        Optional.of(inputStream));

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/attachment/{attachmentId}", projectKey, attachmentId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"image.png\""))
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png"))
        .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, "1024"));

    verify(tmsAttachmentService).getTmsAttachment(attachmentId);
    verify(attachmentDataStoreService).load("/path/to/image.png");
  }

  @Test
  void downloadAttachmentNotFoundTest() throws Exception {
    // Given
    var attachmentId = 999L;

    given(tmsAttachmentService.getTmsAttachment(attachmentId)).willReturn(Optional.empty());

    // When/Then
    assertThrows(ServletException.class, () -> mockMvc.perform(
        get("/v1/project/{projectKey}/tms/attachment/{attachmentId}", projectKey, attachmentId)
            .contentType(MediaType.APPLICATION_JSON)));

    verify(tmsAttachmentService).getTmsAttachment(attachmentId);
  }

  @Test
  void downloadAttachmentFileNotFoundTest() throws Exception {
    // Given
    var attachmentId = 1L;
    var attachment = new TmsAttachment();
    attachment.setId(attachmentId);
    attachment.setFileName("test.txt");
    attachment.setFileType("text/plain");
    attachment.setFileSize(22L);
    attachment.setPathToFile("/path/to/missing/file");

    given(tmsAttachmentService.getTmsAttachment(attachmentId)).willReturn(Optional.of(attachment));
    given(attachmentDataStoreService.load("/path/to/missing/file")).willReturn(Optional.empty());

    // When/Then
    assertThrows(ServletException.class, () -> mockMvc.perform(
        get("/v1/project/{projectKey}/tms/attachment/{attachmentId}", projectKey, attachmentId)
            .contentType(MediaType.APPLICATION_JSON)));

    verify(tmsAttachmentService).getTmsAttachment(attachmentId);
    verify(attachmentDataStoreService).load("/path/to/missing/file");
  }

  @Test
  void deleteAttachmentTest() throws Exception {
    // Given
    var attachmentId = 1L;

    doNothing().when(tmsAttachmentService).deleteAttachment(attachmentId);

    // When/Then
    mockMvc.perform(
            delete("/v1/project/{projectKey}/tms/attachment/{attachmentId}", projectKey, attachmentId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value(
            "Attachment with ID = '" + attachmentId + "' successfully deleted."));

    verify(tmsAttachmentService).deleteAttachment(attachmentId);
  }

  @Test
  void deleteAttachmentWithDifferentIdTest() throws Exception {
    // Given
    var attachmentId = 123L;

    doNothing().when(tmsAttachmentService).deleteAttachment(attachmentId);

    // When/Then
    mockMvc.perform(
            delete("/v1/project/{projectKey}/tms/attachment/{attachmentId}", projectKey, attachmentId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value(
            "Attachment with ID = '" + attachmentId + "' successfully deleted."));

    verify(tmsAttachmentService).deleteAttachment(attachmentId);
  }

  @Test
  void deleteNonExistentAttachmentTest() throws Exception {
    // Given
    var attachmentId = 999L;

    doThrow(new ReportPortalException(ErrorType.NOT_FOUND,
        "Attachment not found: " + attachmentId))
        .when(tmsAttachmentService)
        .deleteAttachment(attachmentId);

    // When/Then
    assertThrows(ServletException.class, () -> mockMvc.perform(
        delete("/v1/project/{projectKey}/tms/attachment/{attachmentId}", projectKey, attachmentId)
            .contentType(MediaType.APPLICATION_JSON)));

    verify(tmsAttachmentService).deleteAttachment(attachmentId);
  }

  @Test
  void uploadEmptyFileTest() throws Exception {
    // Given
    var file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

    var uploadResponse = UploadAttachmentRS.builder()
        .id(1L)
        .fileName("empty.txt")
        .fileSize(0L)
        .fileType("text/plain")
        .build();

    given(tmsAttachmentService.uploadAttachment(any())).willReturn(uploadResponse);

    // When/Then
    mockMvc.perform(
            multipart("/v1/project/{projectKey}/tms/attachment/upload", projectKey)
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.fileName").value("empty.txt"))
        .andExpect(jsonPath("$.fileSize").value(0L))
        .andExpect(jsonPath("$.fileType").value("text/plain"));

    verify(tmsAttachmentService).uploadAttachment(any());
  }

  @Test
  void uploadAttachmentWithSpecialCharactersInNameTest() throws Exception {
    // Given
    var fileContent = "test content";
    var fileName = "тест файл с русскими символами.txt";
    var file = new MockMultipartFile("file", fileName, "text/plain", fileContent.getBytes());

    var uploadResponse = UploadAttachmentRS.builder()
        .id(1L)
        .fileName(fileName)
        .fileSize((long) fileContent.length())
        .fileType("text/plain")
        .build();

    given(tmsAttachmentService.uploadAttachment(any())).willReturn(uploadResponse);

    // When/Then
    mockMvc.perform(
            multipart("/v1/project/{projectKey}/tms/attachment/upload", projectKey)
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.fileName").value(fileName))
        .andExpect(jsonPath("$.fileSize").value(fileContent.length()))
        .andExpect(jsonPath("$.fileType").value("text/plain"));

    verify(tmsAttachmentService).uploadAttachment(any());
  }

  @Test
  void downloadAttachmentWithSpecialCharactersInNameTest() throws Exception {
    // Given
    var attachmentId = 1L;
    var fileName = "тест файл с русскими символами.txt";
    var attachment = new TmsAttachment();
    attachment.setId(attachmentId);
    attachment.setFileName(fileName);
    attachment.setFileType("text/plain");
    attachment.setFileSize(22L);
    attachment.setPathToFile("/path/to/file");

    var fileContent = "test attachment content";
    InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());

    given(tmsAttachmentService.getTmsAttachment(attachmentId)).willReturn(Optional.of(attachment));
    given(attachmentDataStoreService.load("/path/to/file")).willReturn(Optional.of(inputStream));

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/attachment/{attachmentId}", projectKey, attachmentId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + fileName + "\""))
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/plain"))
        .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, "22"));

    verify(tmsAttachmentService).getTmsAttachment(attachmentId);
    verify(attachmentDataStoreService).load("/path/to/file");
  }
}
