package com.epam.ta.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.binary.tms.TmsAttachmentDataStoreService;
import com.epam.ta.reportportal.core.tms.db.entity.TmsAttachment;
import com.epam.ta.reportportal.core.tms.db.repository.TmsAttachmentRepository;
import com.epam.ta.reportportal.core.tms.db.repository.TmsManualScenarioPreconditionsAttachmentRepository;
import com.epam.ta.reportportal.core.tms.db.repository.TmsStepAttachmentRepository;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTextManualScenarioAttachmentRepository;
import com.epam.ta.reportportal.core.tms.dto.UploadAttachmentRS;
import com.epam.ta.reportportal.core.tms.mapper.TmsAttachmentMapper;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class TmsAttachmentServiceImplTest {

  @Mock
  private TmsAttachmentRepository tmsAttachmentRepository;

  @Mock
  private TmsAttachmentDataStoreService tmsAttachmentDataStoreService;

  @Mock
  private TmsAttachmentMapper tmsAttachmentMapper;

  @Mock
  private TmsStepAttachmentRepository tmsStepAttachmentRepository;

  @Mock
  private TmsTextManualScenarioAttachmentRepository tmsTextManualScenarioAttachmentRepository;

  @Mock
  private TmsManualScenarioPreconditionsAttachmentRepository tmsManualScenarioPreconditionsAttachmentRepository;

  @InjectMocks
  private TmsAttachmentServiceImpl sut;

  private TmsAttachment attachment;
  private MultipartFile file;
  private UploadAttachmentRS uploadAttachmentRS;
  private String fileId;
  private Long attachmentId;

  @BeforeEach
  void setUp() {
    attachmentId = 1L;
    fileId = "file-id-123";

    attachment = new TmsAttachment();
    attachment.setId(attachmentId);
    attachment.setFileName("test.txt");
    attachment.setPathToFile(fileId);
    attachment.setFileType("text/plain");
    attachment.setFileSize(100L);

    file = new MockMultipartFile(
        "attachment",
        "test.txt",
        "text/plain",
        "test content".getBytes()
    );

    uploadAttachmentRS = new UploadAttachmentRS();
    uploadAttachmentRS.setId(attachmentId);
    uploadAttachmentRS.setFileName("test.txt");

    // Set TTL value via reflection for testing
    ReflectionTestUtils.setField(sut, "ttl", Duration.ofHours(24));
  }

  @Test
  void uploadAttachment_ShouldSucceed_WhenValidFile() throws Exception {
    // Given valid file to upload
    when(tmsAttachmentDataStoreService.save(anyString(), any(InputStream.class))).thenReturn(fileId);
    when(tmsAttachmentMapper.convertToAttachment(fileId, file)).thenReturn(attachment);
    when(tmsAttachmentRepository.save(attachment)).thenReturn(attachment);
    when(tmsAttachmentMapper.convertToUploadAttachmentRS(attachment)).thenReturn(
        uploadAttachmentRS);

    // When uploading attachment
    var result = sut.uploadAttachment(file);

    // Then attachment should be successfully uploaded
    assertNotNull(result);
    assertEquals(uploadAttachmentRS.getId(), result.getId());
    assertEquals(uploadAttachmentRS.getFileName(), result.getFileName());

    verify(tmsAttachmentDataStoreService).save(eq("test.txt"), any(InputStream.class));
    verify(tmsAttachmentMapper).convertToAttachment(fileId, file);
    verify(tmsAttachmentRepository).save(attachment);
    verify(tmsAttachmentMapper).convertToUploadAttachmentRS(attachment);
  }

  @Test
  void uploadAttachment_ShouldThrowException_WhenFileIsEmpty() {
    // Given empty file
    var emptyFile = new MockMultipartFile("attachment", "", "text/plain", new byte[0]);

    // When/Then exception should be thrown for empty file
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.uploadAttachment(emptyFile));

    assertEquals(ErrorType.BAD_REQUEST_ERROR, exception.getErrorType());
    assertEquals("Error in handled Request. Please, check specified parameters: 'File cannot be empty'", exception.getMessage());

    verifyNoInteractions(tmsAttachmentDataStoreService, tmsAttachmentMapper, tmsAttachmentRepository);
  }

  @Test
  void uploadAttachment_ShouldThrowException_WhenDataStoreServiceFails() throws Exception {
    // Given data store service throws IOException
    when(tmsAttachmentDataStoreService.save(anyString(), any(InputStream.class)))
        .thenThrow(new RuntimeException("Storage error"));

    // When/Then exception should be thrown when storage fails
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.uploadAttachment(file));

    assertEquals(ErrorType.BINARY_DATA_CANNOT_BE_SAVED, exception.getErrorType());
    assertTrue(exception.getMessage().contains("Failed to upload attachment"));

    verify(tmsAttachmentDataStoreService).save(eq("test.txt"), any(InputStream.class));
    verifyNoInteractions(tmsAttachmentRepository);
  }

  @Test
  void getTmsAttachment_ShouldReturnAttachment_WhenExists() {
    // Given attachment exists in repository
    when(tmsAttachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

    // When retrieving attachment by ID
    var result = sut.getTmsAttachment(attachmentId);

    // Then attachment should be returned
    assertTrue(result.isPresent());
    assertEquals(attachment, result.get());
    verify(tmsAttachmentRepository).findById(attachmentId);
  }

  @Test
  void getTmsAttachment_ShouldReturnEmpty_WhenNotExists() {
    // Given attachment does not exist in repository
    when(tmsAttachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());

    // When retrieving non-existent attachment
    var result = sut.getTmsAttachment(attachmentId);

    // Then empty optional should be returned
    assertFalse(result.isPresent());
    verify(tmsAttachmentRepository).findById(attachmentId);
  }

  @Test
  void deleteAttachment_ShouldSucceed_WhenAttachmentExists() {
    // Given attachment exists in repository
    when(tmsAttachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
    doNothing().when(tmsAttachmentDataStoreService).delete(fileId);
    doNothing().when(tmsAttachmentRepository).deleteById(attachmentId);

    // When deleting existing attachment
    sut.deleteAttachment(attachmentId);

    // Then attachment should be deleted from both data store and repository
    verify(tmsAttachmentRepository).findById(attachmentId);
    verify(tmsAttachmentDataStoreService).delete(fileId);
    verify(tmsAttachmentRepository).deleteById(attachmentId);
  }

  @Test
  void deleteAttachment_ShouldThrowException_WhenAttachmentNotFound() {
    // Given attachment does not exist
    when(tmsAttachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());

    // When/Then exception should be thrown for non-existent attachment
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.deleteAttachment(attachmentId));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    assertTrue(exception.getMessage().contains("Attachment not found: " + attachmentId));

    verify(tmsAttachmentRepository).findById(attachmentId);
    verifyNoInteractions(tmsAttachmentDataStoreService);
    verify(tmsAttachmentRepository, never()).deleteById(attachmentId);
  }

  @Test
  void deleteAttachment_ShouldThrowException_WhenDataStoreDeleteFails() {
    // Given attachment exists but data store delete operation fails
    when(tmsAttachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
    doThrow(new RuntimeException("Delete failed")).when(tmsAttachmentDataStoreService).delete(fileId);

    // When/Then exception should be thrown when data store delete fails
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.deleteAttachment(attachmentId));

    assertEquals(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR, exception.getErrorType());

    verify(tmsAttachmentRepository).findById(attachmentId);
    verify(tmsAttachmentDataStoreService).delete(fileId);
  }

  @Test
  void removeTtlFromTmsAttachments_ShouldRemoveTtl_WhenAttachmentIdsProvided() {
    // Given list of attachment IDs
    var attachmentIds = Arrays.asList(1L, 2L, 3L);

    // When removing TTL from attachments
    sut.removeTtlFromTmsAttachments(attachmentIds);

    // Then TTL should be removed from specified attachments
    verify(tmsAttachmentRepository).removeExpirationFromAttachments(attachmentIds);
  }

  @Test
  void removeTtlFromTmsAttachments_ShouldDoNothing_WhenAttachmentIdsEmpty() {
    // Given empty list of attachment IDs
    var attachmentIds = Collections.<Long>emptyList();

    // When removing TTL from empty list
    sut.removeTtlFromTmsAttachments(attachmentIds);

    // Then no repository interactions should occur
    verifyNoInteractions(tmsAttachmentRepository);
  }

  @Test
  void removeTtlFromTmsAttachments_ShouldDoNothing_WhenAttachmentIdsNull() {
    // When removing TTL with null attachment IDs list
    sut.removeTtlFromTmsAttachments(null);

    // Then no repository interactions should occur
    verifyNoInteractions(tmsAttachmentRepository);
  }

  @Test
  void cleanupExpiredAttachments_ShouldCleanup_WhenExpiredAttachmentsExist() {
    // Given expired attachments exist in repository
    var expiredAttachment1 = new TmsAttachment();
    expiredAttachment1.setId(1L);
    expiredAttachment1.setPathToFile("path1");

    var expiredAttachment2 = new TmsAttachment();
    expiredAttachment2.setId(2L);
    expiredAttachment2.setPathToFile("path2");

    var expiredAttachments = Arrays.asList(expiredAttachment1, expiredAttachment2);

    when(tmsAttachmentRepository.findExpiredAttachments(any(Instant.class)))
        .thenReturn(expiredAttachments);

    // When cleaning up expired attachments
    sut.cleanupExpiredAttachments();

    // Then expired attachments should be cleaned up from both data store and repository
    verify(tmsAttachmentRepository).findExpiredAttachments(any(Instant.class));
    verify(tmsAttachmentDataStoreService).delete("path1");
    verify(tmsAttachmentDataStoreService).delete("path2");
    verify(tmsAttachmentRepository).deleteByIds(Arrays.asList(1L, 2L));
  }

  @Test
  void cleanupExpiredAttachments_ShouldDoNothing_WhenNoExpiredAttachments() {
    // Given no expired attachments exist
    when(tmsAttachmentRepository.findExpiredAttachments(any(Instant.class)))
        .thenReturn(Collections.emptyList());

    // When cleaning up expired attachments
    sut.cleanupExpiredAttachments();

    // Then no cleanup operations should be performed
    verify(tmsAttachmentRepository).findExpiredAttachments(any(Instant.class));
    verifyNoInteractions(tmsAttachmentDataStoreService);
    verify(tmsAttachmentRepository, never()).deleteByIds(anyList());
  }

  @Test
  void cleanupExpiredAttachments_ShouldContinueOnFileDeleteError() {
    // Given expired attachment exists but file delete operation fails
    var expiredAttachment = new TmsAttachment();
    expiredAttachment.setId(1L);
    expiredAttachment.setPathToFile("path1");

    when(tmsAttachmentRepository.findExpiredAttachments(any(Instant.class)))
        .thenReturn(List.of(expiredAttachment));
    doThrow(new RuntimeException("File delete failed")).when(tmsAttachmentDataStoreService)
        .delete("path1");

    // When cleaning up expired attachments
    sut.cleanupExpiredAttachments();

    // Then cleanup should continue despite file delete error
    verify(tmsAttachmentRepository).findExpiredAttachments(any(Instant.class));
    verify(tmsAttachmentDataStoreService).delete("path1");
    verify(tmsAttachmentRepository).deleteByIds(List.of(1L));
  }

  @Test
  void getTmsAttachmentsByIds_ShouldReturnAttachments_WhenIdsProvided() {
    // Given list of attachment IDs and corresponding attachments
    var attachmentIds = Arrays.asList(1L, 2L);
    var attachments = Collections.singletonList(attachment);

    when(tmsAttachmentRepository.findAllById(attachmentIds)).thenReturn(attachments);

    // When retrieving attachments by IDs
    var result = sut.getTmsAttachmentsByIds(attachmentIds);

    // Then attachments should be returned
    assertNotNull(result);
    assertEquals(attachments, result);
    verify(tmsAttachmentRepository).findAllById(attachmentIds);
  }

  @Test
  void getTmsAttachmentsByIds_ShouldReturnEmptyList_WhenIdsEmpty() {
    // Given empty list of attachment IDs
    var attachmentIds = Collections.<Long>emptyList();

    // When retrieving attachments with empty IDs list
    var result = sut.getTmsAttachmentsByIds(attachmentIds);

    // Then empty list should be returned without repository interaction
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verifyNoInteractions(tmsAttachmentRepository);
  }

  @Test
  void getTmsAttachmentsByIds_ShouldReturnEmptyList_WhenIdsNull() {
    // When retrieving attachments with null IDs
    var result = sut.getTmsAttachmentsByIds(null);

    // Then empty list should be returned without repository interaction
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verifyNoInteractions(tmsAttachmentRepository);
  }

  @Test
  void duplicateTmsAttachment_ShouldSucceed_WhenValidAttachment() throws Exception {
    // Given valid attachment to duplicate and successful data store operations
    var newFileId = "new-file-id";
    var duplicatedAttachment = new TmsAttachment();
    duplicatedAttachment.setId(2L);
    duplicatedAttachment.setFileName("test_copy_123456_abc.txt");
    duplicatedAttachment.setPathToFile(newFileId);

    var originalFileStream = new ByteArrayInputStream("test content".getBytes());

    when(tmsAttachmentDataStoreService.load(attachment.getPathToFile()))
        .thenReturn(Optional.of(originalFileStream));
    when(tmsAttachmentDataStoreService.save(anyString(), any(InputStream.class))).thenReturn(
        newFileId);
    when(tmsAttachmentMapper.duplicateAttachment(eq(attachment), eq(newFileId)))
        .thenReturn(duplicatedAttachment);
    when(tmsAttachmentRepository.save(duplicatedAttachment)).thenReturn(duplicatedAttachment);

    // When duplicating attachment
    var result = sut.duplicateTmsAttachment(attachment);

    // Then duplicated attachment should be returned
    assertNotNull(result);
    assertEquals(duplicatedAttachment, result);

    verify(tmsAttachmentDataStoreService).load(attachment.getPathToFile());
    verify(tmsAttachmentDataStoreService).save(anyString(), eq(originalFileStream));
    verify(tmsAttachmentMapper).duplicateAttachment(attachment, newFileId);
    verify(tmsAttachmentRepository).save(duplicatedAttachment);
  }

  @Test
  void duplicateTmsAttachment_ShouldThrowException_WhenOriginalFileNotFound() {
    // Given original file does not exist in data store
    when(tmsAttachmentDataStoreService.load(attachment.getPathToFile())).thenReturn(Optional.empty());

    // When/Then exception should be thrown when original file is not found
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.duplicateTmsAttachment(attachment));

    assertEquals(ErrorType.BINARY_DATA_CANNOT_BE_SAVED, exception.getErrorType());

    verify(tmsAttachmentDataStoreService).load(attachment.getPathToFile());
    verifyNoInteractions(tmsAttachmentRepository);
  }

  @Test
  void duplicateTmsAttachment_ShouldThrowException_WhenDataStoreSaveFails() throws Exception {
    // Given original file exists but data store save operation fails
    var originalFileStream = new ByteArrayInputStream("test content".getBytes());
    when(tmsAttachmentDataStoreService.load(attachment.getPathToFile()))
        .thenReturn(Optional.of(originalFileStream));
    when(tmsAttachmentDataStoreService.save(anyString(), any(InputStream.class)))
        .thenThrow(new RuntimeException("Storage error"));

    // When/Then exception should be thrown when data store save fails
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.duplicateTmsAttachment(attachment));

    assertEquals(ErrorType.BINARY_DATA_CANNOT_BE_SAVED, exception.getErrorType());
    assertTrue(exception.getMessage().contains("Failed to duplicate TMS attachment"));
  }

  @Test
  void setExpirationForUnusedAttachments_ShouldSetExpiration_WhenUnusedAttachmentsExist() {
    // Given unused attachments exist in repository
    var unusedAttachment1 = new TmsAttachment();
    unusedAttachment1.setId(1L);

    var unusedAttachment2 = new TmsAttachment();
    unusedAttachment2.setId(2L);

    var usedAttachment = new TmsAttachment();
    usedAttachment.setId(3L);

    var attachmentsWithoutTtl = Arrays.asList(unusedAttachment1, unusedAttachment2, usedAttachment);

    when(tmsAttachmentRepository.findAttachmentsWithoutTtl()).thenReturn(attachmentsWithoutTtl);
    when(tmsStepAttachmentRepository.findAllAttachmentIds()).thenReturn(List.of(3L));
    when(tmsTextManualScenarioAttachmentRepository.findAllAttachmentIds()).thenReturn(
        Collections.emptyList());
    when(tmsManualScenarioPreconditionsAttachmentRepository.findAllAttachmentIds()).thenReturn(
        Collections.emptyList());
    when(tmsAttachmentRepository.setExpirationForAttachments(anyList(),
        any(Instant.class))).thenReturn(2);

    // When setting expiration for unused attachments
    sut.setExpirationForUnusedAttachments();

    // Then expiration should be set only for unused attachments
    verify(tmsAttachmentRepository).findAttachmentsWithoutTtl();
    verify(tmsStepAttachmentRepository).findAllAttachmentIds();
    verify(tmsTextManualScenarioAttachmentRepository).findAllAttachmentIds();
    verify(tmsManualScenarioPreconditionsAttachmentRepository).findAllAttachmentIds();

    var idsCaptor = ArgumentCaptor.forClass(List.class);
    var instantCaptor = ArgumentCaptor.forClass(Instant.class);
    verify(tmsAttachmentRepository).setExpirationForAttachments(idsCaptor.capture(),
        instantCaptor.capture());

    var capturedIds = idsCaptor.getValue();
    assertEquals(2, capturedIds.size());
    assertTrue(capturedIds.contains(1L));
    assertTrue(capturedIds.contains(2L));
    assertFalse(capturedIds.contains(3L)); // Used attachment should not be included

    assertNotNull(instantCaptor.getValue());
  }

  @Test
  void setExpirationForUnusedAttachments_ShouldDoNothing_WhenNoAttachmentsWithoutTtl() {
    // Given no attachments without TTL exist
    when(tmsAttachmentRepository.findAttachmentsWithoutTtl()).thenReturn(Collections.emptyList());

    // When setting expiration for unused attachments
    sut.setExpirationForUnusedAttachments();

    // Then no expiration setting operations should be performed
    verify(tmsAttachmentRepository).findAttachmentsWithoutTtl();
    verifyNoInteractions(tmsStepAttachmentRepository);
    verifyNoInteractions(tmsTextManualScenarioAttachmentRepository);
    verifyNoInteractions(tmsManualScenarioPreconditionsAttachmentRepository);
    verify(tmsAttachmentRepository, never()).setExpirationForAttachments(anyList(),
        any(Instant.class));
  }

  @Test
  void setExpirationForUnusedAttachments_ShouldDoNothing_WhenAllAttachmentsAreUsed() {
    // Given all attachments are used in various scenarios
    var usedAttachment1 = new TmsAttachment();
    usedAttachment1.setId(1L);

    var usedAttachment2 = new TmsAttachment();
    usedAttachment2.setId(2L);

    var attachmentsWithoutTtl = Arrays.asList(usedAttachment1, usedAttachment2);

    when(tmsAttachmentRepository.findAttachmentsWithoutTtl()).thenReturn(attachmentsWithoutTtl);
    when(tmsStepAttachmentRepository.findAllAttachmentIds()).thenReturn(Arrays.asList(1L, 2L));
    when(tmsTextManualScenarioAttachmentRepository.findAllAttachmentIds()).thenReturn(
        Collections.emptyList());
    when(tmsManualScenarioPreconditionsAttachmentRepository.findAllAttachmentIds()).thenReturn(
        Collections.emptyList());

    // When setting expiration for unused attachments
    sut.setExpirationForUnusedAttachments();

    // Then no expiration should be set as all attachments are used
    verify(tmsAttachmentRepository).findAttachmentsWithoutTtl();
    verify(tmsStepAttachmentRepository).findAllAttachmentIds();
    verify(tmsTextManualScenarioAttachmentRepository).findAllAttachmentIds();
    verify(tmsManualScenarioPreconditionsAttachmentRepository).findAllAttachmentIds();
    verify(tmsAttachmentRepository, never()).setExpirationForAttachments(anyList(),
        any(Instant.class));
  }

  @Test
  void setExpirationForUnusedAttachments_ShouldHandleMixedUsageCorrectly() {
    // Given mixed usage scenario - some attachments used in different tables, some not used
    var attachment1 = new TmsAttachment();
    attachment1.setId(1L);

    var attachment2 = new TmsAttachment();
    attachment2.setId(2L);

    var attachment3 = new TmsAttachment();
    attachment3.setId(3L);

    var attachmentsWithoutTtl = Arrays.asList(attachment1, attachment2, attachment3);

    when(tmsAttachmentRepository.findAttachmentsWithoutTtl()).thenReturn(attachmentsWithoutTtl);
    when(tmsStepAttachmentRepository.findAllAttachmentIds()).thenReturn(List.of(1L));
    when(tmsTextManualScenarioAttachmentRepository.findAllAttachmentIds()).thenReturn(
        List.of(2L));
    when(tmsManualScenarioPreconditionsAttachmentRepository.findAllAttachmentIds()).thenReturn(
        Collections.emptyList());
    when(tmsAttachmentRepository.setExpirationForAttachments(anyList(),
        any(Instant.class))).thenReturn(1);

    // When setting expiration for unused attachments
    sut.setExpirationForUnusedAttachments();

    // Then expiration should be set only for genuinely unused attachment
    var idsCaptor = ArgumentCaptor.forClass(List.class);
    verify(tmsAttachmentRepository).setExpirationForAttachments(idsCaptor.capture(),
        any(Instant.class));

    var capturedIds = idsCaptor.getValue();
    assertEquals(1, capturedIds.size());
    assertTrue(capturedIds.contains(3L));
    assertFalse(capturedIds.contains(1L)); // Used in step attachments
    assertFalse(capturedIds.contains(2L)); // Used in text scenario attachments
  }

  @Test
  void setExpirationForUnusedAttachments_ShouldHandleOverlappingUsage() {
    // Given overlapping usage scenario - same attachment referenced in multiple tables
    var attachment1 = new TmsAttachment();
    attachment1.setId(1L);

    var attachment2 = new TmsAttachment();
    attachment2.setId(2L);

    var attachmentsWithoutTtl = Arrays.asList(attachment1, attachment2);

    when(tmsAttachmentRepository.findAttachmentsWithoutTtl()).thenReturn(attachmentsWithoutTtl);
    when(tmsStepAttachmentRepository.findAllAttachmentIds()).thenReturn(List.of(1L));
    when(tmsTextManualScenarioAttachmentRepository.findAllAttachmentIds()).thenReturn(
        List.of(1L)); // Same ID in multiple tables
    when(tmsManualScenarioPreconditionsAttachmentRepository.findAllAttachmentIds()).thenReturn(
        Collections.emptyList());
    when(tmsAttachmentRepository.setExpirationForAttachments(anyList(),
        any(Instant.class))).thenReturn(1);

    // When setting expiration for unused attachments
    sut.setExpirationForUnusedAttachments();

    // Then expiration should be set only for attachment not used in any table
    var idsCaptor = ArgumentCaptor.forClass(List.class);
    verify(tmsAttachmentRepository).setExpirationForAttachments(idsCaptor.capture(),
        any(Instant.class));

    var capturedIds = idsCaptor.getValue();
    assertEquals(1, capturedIds.size());
    assertTrue(capturedIds.contains(2L));
    assertFalse(capturedIds.contains(1L)); // Used in multiple tables
  }
}
