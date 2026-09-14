package com.epam.reportportal.base.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.dto.UploadAttachmentRS;
import com.epam.reportportal.base.core.tms.mapper.TmsAttachmentMapper;
import com.epam.reportportal.base.infrastructure.persistence.binary.tms.TmsAttachmentDataStoreService;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsAttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsManualScenarioPreconditionsAttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsStepAttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseExecutionCommentAttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTextManualScenarioAttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.FeatureFlag;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttachment;
import com.epam.reportportal.base.infrastructure.persistence.util.FeatureFlagHandler;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
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
  private TmsAttachmentPersistenceService tmsAttachmentPersistenceService;

  @Mock
  private TmsStepAttachmentRepository tmsStepAttachmentRepository;

  @Mock
  private TmsTextManualScenarioAttachmentRepository tmsTextManualScenarioAttachmentRepository;

  @Mock
  private TmsManualScenarioPreconditionsAttachmentRepository tmsManualScenarioPreconditionsAttachmentRepository;

  @Mock
  private TmsTestCaseExecutionCommentAttachmentRepository tmsTestCaseExecutionCommentAttachmentRepository;

  @Mock
  private FeatureFlagHandler featureFlagHandler;

  @InjectMocks
  private TmsAttachmentServiceImpl sut;

  private TmsAttachment attachment;
  private MultipartFile file;
  private UploadAttachmentRS uploadAttachmentRS;
  private String fileId;
  private Long attachmentId;
  private static final Long PROJECT_ID = 42L;
  private static final Long OTHER_PROJECT_ID = 99L;

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
  void uploadAttachment_ShouldSucceed_WhenValidFile() {
    // Given valid file to upload
    when(tmsAttachmentDataStoreService.save(anyString(), any(InputStream.class))).thenReturn(
        fileId);
    when(tmsAttachmentMapper.convertToAttachment(eq(fileId), any(), eq(file), eq(PROJECT_ID))).thenReturn(
        attachment);
    when(tmsAttachmentPersistenceService.persist(attachment)).thenReturn(attachment);
    when(tmsAttachmentMapper.convertToUploadAttachmentRS(attachment)).thenReturn(
        uploadAttachmentRS);

    // When uploading attachment
    var result = sut.uploadAttachment(PROJECT_ID, file);

    // Then attachment should be successfully uploaded
    assertNotNull(result);
    assertEquals(uploadAttachmentRS.getId(), result.getId());
    assertEquals(uploadAttachmentRS.getFileName(), result.getFileName());

    var storageKeyCaptor = ArgumentCaptor.forClass(String.class);
    verify(tmsAttachmentDataStoreService).save(storageKeyCaptor.capture(), any(InputStream.class));
    assertTrue(storageKeyCaptor.getValue().startsWith(PROJECT_ID + "/"));
    assertTrue(storageKeyCaptor.getValue().endsWith("_test.txt"));
    verify(tmsAttachmentMapper).convertToAttachment(eq(fileId), any(), eq(file), eq(PROJECT_ID));
    verify(tmsAttachmentPersistenceService).persist(attachment);
    verify(tmsAttachmentMapper).convertToUploadAttachmentRS(attachment);
  }

  @Test
  void uploadAttachment_ShouldThrowException_WhenFileIsEmpty() {
    // Given empty file
    var emptyFile = new MockMultipartFile("attachment", "", "text/plain", new byte[0]);

    // When/Then exception should be thrown for empty file
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.uploadAttachment(PROJECT_ID, emptyFile));

    assertEquals(ErrorType.BAD_REQUEST_ERROR, exception.getErrorType());
    assertEquals(
        "Error in handled Request. Please, check specified parameters: 'File cannot be empty'",
        exception.getMessage());

    verifyNoInteractions(tmsAttachmentDataStoreService, tmsAttachmentMapper,
        tmsAttachmentPersistenceService);
  }

  @Test
  void uploadAttachment_ShouldThrowException_WhenDataStoreServiceFails() {
    // Given data store service throws an unchecked exception
    when(tmsAttachmentDataStoreService.save(anyString(), any(InputStream.class)))
        .thenThrow(new RuntimeException("Storage error"));

    // When/Then exception should be thrown when storage fails
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.uploadAttachment(PROJECT_ID, file));

    assertEquals(ErrorType.BINARY_DATA_CANNOT_BE_SAVED, exception.getErrorType());
    assertTrue(exception.getMessage().contains("Failed to read/store attachment"));

    var storageKeyCaptor = ArgumentCaptor.forClass(String.class);
    verify(tmsAttachmentDataStoreService).save(storageKeyCaptor.capture(), any(InputStream.class));
    assertTrue(storageKeyCaptor.getValue().startsWith(PROJECT_ID + "/"));
    assertTrue(storageKeyCaptor.getValue().endsWith("_test.txt"));
    verifyNoInteractions(tmsAttachmentPersistenceService);
  }

  @Test
  void uploadAttachment_ShouldDeleteOrphanedBlob_WhenPersistenceServiceFails() {
    // Given the blob is stored successfully but the DB write fails
    when(tmsAttachmentDataStoreService.save(anyString(), any(InputStream.class))).thenReturn(
        fileId);
    when(tmsAttachmentMapper.convertToAttachment(eq(fileId), any(), eq(file), eq(PROJECT_ID))).thenReturn(
        attachment);
    when(tmsAttachmentPersistenceService.persist(attachment))
        .thenThrow(new RuntimeException("DB error"));

    // When/Then exception should be thrown and the orphaned blob cleaned up
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.uploadAttachment(PROJECT_ID, file));

    assertEquals(ErrorType.BINARY_DATA_CANNOT_BE_SAVED, exception.getErrorType());
    assertTrue(exception.getMessage().contains("Failed to persist attachment metadata"));
    verify(tmsAttachmentDataStoreService).delete(fileId);
  }

  @Test
  void uploadAttachment_ShouldUseDistinctProjectScopedStorageKeys_WhenSameOriginalFilename() {
    when(tmsAttachmentDataStoreService.save(anyString(), any(InputStream.class)))
        .thenReturn("file-id-1", "file-id-2");
    when(tmsAttachmentMapper.convertToAttachment(anyString(), any(), eq(file), any())).thenReturn(
        attachment);
    when(tmsAttachmentPersistenceService.persist(attachment)).thenReturn(attachment);
    when(tmsAttachmentMapper.convertToUploadAttachmentRS(attachment)).thenReturn(
        uploadAttachmentRS);

    sut.uploadAttachment(PROJECT_ID, file);
    sut.uploadAttachment(OTHER_PROJECT_ID, file);

    var storageKeyCaptor = ArgumentCaptor.forClass(String.class);
    verify(tmsAttachmentDataStoreService, times(2))
        .save(storageKeyCaptor.capture(), any(InputStream.class));

    var storageKeys = storageKeyCaptor.getAllValues();
    assertEquals(2, storageKeys.size());
    assertNotEquals(storageKeys.get(0), storageKeys.get(1));
    assertTrue(storageKeys.get(0).startsWith(PROJECT_ID + "/"));
    assertTrue(storageKeys.get(1).startsWith(OTHER_PROJECT_ID + "/"));
    assertTrue(storageKeys.get(0).endsWith("_test.txt"));
    assertTrue(storageKeys.get(1).endsWith("_test.txt"));
  }

  @Test
  void getTmsAttachment_ShouldReturnAttachment_WhenExists() {
    // Given attachment exists in repository
    when(tmsAttachmentRepository.findByIdAndProjectId(attachmentId, PROJECT_ID)).thenReturn(Optional.of(attachment));

    // When retrieving attachment by ID
    var result = sut.getTmsAttachment(PROJECT_ID, attachmentId);

    // Then attachment should be returned
    assertTrue(result.isPresent());
    assertEquals(attachment, result.get());
    verify(tmsAttachmentRepository).findByIdAndProjectId(attachmentId, PROJECT_ID);
  }

  @Test
  void getTmsAttachment_ShouldReturnEmpty_WhenNotExists() {
    // Given attachment does not exist in repository
    when(tmsAttachmentRepository.findByIdAndProjectId(attachmentId, PROJECT_ID)).thenReturn(Optional.empty());

    // When retrieving non-existent attachment
    var result = sut.getTmsAttachment(PROJECT_ID, attachmentId);

    // Then empty optional should be returned
    assertFalse(result.isPresent());
    verify(tmsAttachmentRepository).findByIdAndProjectId(attachmentId, PROJECT_ID);
  }

  @Test
  void deleteAttachment_ShouldSucceed_WhenAttachmentExists() {
    // Given attachment exists in repository
    when(tmsAttachmentRepository.findByIdAndProjectId(attachmentId, PROJECT_ID)).thenReturn(Optional.of(attachment));
    doNothing().when(tmsAttachmentDataStoreService).delete(fileId);
    doNothing().when(tmsAttachmentRepository).deleteById(attachmentId);

    // When deleting existing attachment
    sut.deleteAttachment(PROJECT_ID, attachmentId);

    // Then attachment should be deleted from both data store and repository
    verify(tmsAttachmentRepository).findByIdAndProjectId(attachmentId, PROJECT_ID);
    verify(tmsAttachmentDataStoreService).delete(fileId);
    verify(tmsAttachmentRepository).deleteById(attachmentId);
  }

  @Test
  void deleteAttachment_ShouldThrowException_WhenAttachmentNotFound() {
    // Given attachment does not exist
    when(tmsAttachmentRepository.findByIdAndProjectId(attachmentId, PROJECT_ID)).thenReturn(Optional.empty());

    // When/Then exception should be thrown for non-existent attachment
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.deleteAttachment(PROJECT_ID, attachmentId));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    assertTrue(exception.getMessage().contains("Attachment not found: " + attachmentId));

    verify(tmsAttachmentRepository).findByIdAndProjectId(attachmentId, PROJECT_ID);
    verifyNoInteractions(tmsAttachmentDataStoreService);
    verify(tmsAttachmentRepository, never()).deleteById(attachmentId);
  }

  @Test
  void deleteAttachment_ShouldThrowException_WhenDataStoreDeleteFails() {
    // Given attachment exists but data store delete operation fails
    when(tmsAttachmentRepository.findByIdAndProjectId(attachmentId, PROJECT_ID)).thenReturn(Optional.of(attachment));
    doThrow(new RuntimeException("Delete failed")).when(tmsAttachmentDataStoreService)
        .delete(fileId);

    // When/Then exception should be thrown when data store delete fails
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.deleteAttachment(PROJECT_ID, attachmentId));

    assertEquals(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR, exception.getErrorType());

    verify(tmsAttachmentRepository).findByIdAndProjectId(attachmentId, PROJECT_ID);
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
    // Given list of attachment IDs and corresponding attachments, all belonging to the project
    var attachmentIds = Arrays.asList(1L, 2L);
    var attachment2 = new TmsAttachment();
    attachment2.setId(2L);
    var attachments = Arrays.asList(attachment, attachment2);

    when(tmsAttachmentRepository.findAllByIdInAndProjectId(attachmentIds, PROJECT_ID))
        .thenReturn(attachments);

    // When retrieving attachments by IDs
    var result = sut.getTmsAttachmentsByIds(PROJECT_ID, attachmentIds);

    // Then attachments should be returned
    assertNotNull(result);
    assertEquals(attachments, result);
    verify(tmsAttachmentRepository).findAllByIdInAndProjectId(attachmentIds, PROJECT_ID);
  }

  @Test
  void getTmsAttachmentsByIds_ShouldReturnEmptyList_WhenIdsEmpty() {
    // Given empty list of attachment IDs
    var attachmentIds = Collections.<Long>emptyList();

    // When retrieving attachments with empty IDs list
    var result = sut.getTmsAttachmentsByIds(PROJECT_ID, attachmentIds);

    // Then empty list should be returned without repository interaction
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verifyNoInteractions(tmsAttachmentRepository);
  }

  @Test
  void getTmsAttachmentsByIds_ShouldReturnEmptyList_WhenIdsNull() {
    // When retrieving attachments with null IDs
    var result = sut.getTmsAttachmentsByIds(PROJECT_ID, null);

    // Then empty list should be returned without repository interaction
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verifyNoInteractions(tmsAttachmentRepository);
  }

  @Test
  void getTmsAttachmentsByIds_ShouldThrowAccessDenied_WhenSomeAttachmentsBelongToAnotherProject() {
    // Given only one of the two requested attachment IDs belongs to this project
    var attachmentIds = Arrays.asList(1L, 2L);
    var attachments = Collections.singletonList(attachment);

    when(tmsAttachmentRepository.findAllByIdInAndProjectId(attachmentIds, PROJECT_ID))
        .thenReturn(attachments);

    // When/Then requesting attachments should be rejected as cross-project access
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.getTmsAttachmentsByIds(PROJECT_ID, attachmentIds));

    assertEquals(ErrorType.ACCESS_DENIED, exception.getErrorType());
    verify(tmsAttachmentRepository).findAllByIdInAndProjectId(attachmentIds, PROJECT_ID);
  }

  @Test
  void duplicateTmsAttachment_ShouldSucceed_WhenValidAttachment() {
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
    when(tmsAttachmentMapper.duplicateAttachment(eq(attachment), eq(newFileId), any()))
        .thenReturn(duplicatedAttachment);
    when(tmsAttachmentRepository.save(duplicatedAttachment)).thenReturn(duplicatedAttachment);

    // When duplicating attachment
    var result = sut.duplicateTmsAttachment(attachment);

    // Then duplicated attachment should be returned
    assertNotNull(result);
    assertEquals(duplicatedAttachment, result);

    verify(tmsAttachmentDataStoreService).load(attachment.getPathToFile());
    verify(tmsAttachmentDataStoreService).save(anyString(), eq(originalFileStream));
    verify(tmsAttachmentMapper).duplicateAttachment(eq(attachment), eq(newFileId), any());
    verify(tmsAttachmentRepository).save(duplicatedAttachment);
  }

  @Test
  void duplicateTmsAttachment_ShouldThrowException_WhenOriginalFileNotFound() {
    // Given original file does not exist in data store
    when(tmsAttachmentDataStoreService.load(attachment.getPathToFile())).thenReturn(
        Optional.empty());

    // When/Then exception should be thrown when original file is not found
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.duplicateTmsAttachment(attachment));

    assertEquals(ErrorType.BINARY_DATA_CANNOT_BE_SAVED, exception.getErrorType());

    verify(tmsAttachmentDataStoreService).load(attachment.getPathToFile());
    verifyNoInteractions(tmsAttachmentRepository);
  }

  @Test
  void duplicateTmsAttachment_ShouldThrowException_WhenDataStoreSaveFails() {
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
    when(tmsTestCaseExecutionCommentAttachmentRepository.findAllAttachmentIds()).thenReturn(
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
    verify(tmsTestCaseExecutionCommentAttachmentRepository).findAllAttachmentIds();

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
    verifyNoInteractions(tmsTestCaseExecutionCommentAttachmentRepository);
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
    when(tmsTestCaseExecutionCommentAttachmentRepository.findAllAttachmentIds()).thenReturn(
        Collections.emptyList());

    // When setting expiration for unused attachments
    sut.setExpirationForUnusedAttachments();

    // Then no expiration should be set as all attachments are used
    verify(tmsAttachmentRepository).findAttachmentsWithoutTtl();
    verify(tmsStepAttachmentRepository).findAllAttachmentIds();
    verify(tmsTextManualScenarioAttachmentRepository).findAllAttachmentIds();
    verify(tmsManualScenarioPreconditionsAttachmentRepository).findAllAttachmentIds();
    verify(tmsTestCaseExecutionCommentAttachmentRepository).findAllAttachmentIds();
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
    when(tmsTestCaseExecutionCommentAttachmentRepository.findAllAttachmentIds()).thenReturn(
        Collections.emptyList());
    when(tmsAttachmentRepository.setExpirationForAttachments(anyList(),
        any(Instant.class))).thenReturn(1);

    // When setting expiration for unused attachments
    sut.setExpirationForUnusedAttachments();

    // Then expiration should be set only for genuinely unused attachment
    verify(tmsStepAttachmentRepository).findAllAttachmentIds();
    verify(tmsTextManualScenarioAttachmentRepository).findAllAttachmentIds();
    verify(tmsManualScenarioPreconditionsAttachmentRepository).findAllAttachmentIds();
    verify(tmsTestCaseExecutionCommentAttachmentRepository).findAllAttachmentIds();

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
    when(tmsTestCaseExecutionCommentAttachmentRepository.findAllAttachmentIds()).thenReturn(
        Collections.emptyList());
    when(tmsAttachmentRepository.setExpirationForAttachments(anyList(),
        any(Instant.class))).thenReturn(1);

    // When setting expiration for unused attachments
    sut.setExpirationForUnusedAttachments();

    // Then expiration should be set only for attachment not used in any table
    verify(tmsStepAttachmentRepository).findAllAttachmentIds();
    verify(tmsTextManualScenarioAttachmentRepository).findAllAttachmentIds();
    verify(tmsManualScenarioPreconditionsAttachmentRepository).findAllAttachmentIds();
    verify(tmsTestCaseExecutionCommentAttachmentRepository).findAllAttachmentIds();

    var idsCaptor = ArgumentCaptor.forClass(List.class);
    verify(tmsAttachmentRepository).setExpirationForAttachments(idsCaptor.capture(),
        any(Instant.class));

    var capturedIds = idsCaptor.getValue();
    assertEquals(1, capturedIds.size());
    assertTrue(capturedIds.contains(2L));
    assertFalse(capturedIds.contains(1L)); // Used in multiple tables
  }

  @Test
  void deleteAllByProjectId_ShouldDeleteContainer_WhenSingleBucketDisabled() {
    // Given single-bucket mode is disabled, so each project has its own dedicated container
    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(false);

    // When deleting all TMS attachment storage for a project
    sut.deleteAllByProjectId(PROJECT_ID);

    // Then the whole per-project container should be removed
    verify(tmsAttachmentDataStoreService).deleteContainer(PROJECT_ID.toString());
    verify(tmsAttachmentRepository, never()).findAllByProjectId(any());
  }

  @Test
  void deleteAllByProjectId_ShouldDeleteOnlyProjectFiles_WhenSingleBucketEnabled() {
    // Given single-bucket mode is enabled, so all projects share the same bucket
    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(true);
    var attachmentWithThumbnail = new TmsAttachment();
    attachmentWithThumbnail.setPathToFile("path/to/file1");
    attachmentWithThumbnail.setThumbnailPath("path/to/thumb1");
    var attachmentWithoutThumbnail = new TmsAttachment();
    attachmentWithoutThumbnail.setPathToFile("path/to/file2");
    when(tmsAttachmentRepository.findAllByProjectId(PROJECT_ID)).thenReturn(
        List.of(attachmentWithThumbnail, attachmentWithoutThumbnail));

    // When deleting all TMS attachment storage for a project
    sut.deleteAllByProjectId(PROJECT_ID);

    // Then only the files belonging to that project should be removed, by explicit path
    var pathsCaptor = ArgumentCaptor.forClass(List.class);
    verify(tmsAttachmentDataStoreService).deleteAll(pathsCaptor.capture(),
        eq(PROJECT_ID.toString()));
    assertEquals(List.of("path/to/file1", "path/to/thumb1", "path/to/file2"),
        pathsCaptor.getValue());
    verify(tmsAttachmentDataStoreService, never()).deleteContainer(any());
  }

  @Test
  void deleteAllByProjectId_ShouldNotThrow_WhenDataStoreDeleteContainerFails() {
    // Given the underlying data store fails to delete the container
    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(false);
    doThrow(new RuntimeException("Storage error")).when(tmsAttachmentDataStoreService)
        .deleteContainer(PROJECT_ID.toString());

    // When/Then deleting storage for the project should not propagate the exception
    sut.deleteAllByProjectId(PROJECT_ID);

    verify(tmsAttachmentDataStoreService).deleteContainer(PROJECT_ID.toString());
  }
}
