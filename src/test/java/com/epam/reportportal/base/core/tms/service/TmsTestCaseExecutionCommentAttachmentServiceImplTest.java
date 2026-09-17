package com.epam.reportportal.base.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentAttachmentRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseExecutionCommentAttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttachment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecutionComment;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsTestCaseExecutionCommentAttachmentServiceImplTest {

  private final Long commentId = 100L;
  private final Long projectId = 400L;
  @Mock
  private TmsAttachmentService tmsAttachmentService;
  @Mock
  private TmsTestCaseExecutionCommentAttachmentRepository tmsTestCaseExecutionCommentAttachmentRepository;
  @InjectMocks
  private TmsTestCaseExecutionCommentAttachmentServiceImpl sut;
  private TmsTestCaseExecutionComment existingComment;
  private TmsTestCaseExecutionCommentRQ commentRQ;

  @BeforeEach
  void setUp() {
    existingComment = new TmsTestCaseExecutionComment();
    existingComment.setId(commentId);

    commentRQ = new TmsTestCaseExecutionCommentRQ();
  }

  // -------------------------------------------------------------------------
  // CREATE
  // -------------------------------------------------------------------------

  @Test
  void createAttachments_WhenRequestIsNull_ShouldDoNothing() {
    sut.createAttachments(projectId, existingComment, null);

    verify(tmsAttachmentService, never()).getTmsAttachmentsByIds(any(), anyList());
  }

  @Test
  void createAttachments_WhenAttachmentsEmpty_ShouldDoNothing() {
    commentRQ.setAttachments(Collections.emptyList());

    sut.createAttachments(projectId, existingComment, commentRQ);

    verify(tmsAttachmentService, never()).getTmsAttachmentsByIds(any(), anyList());
  }

  @Test
  void createAttachments_WhenAttachmentsFound_ShouldCreateRelationsAndRemoveTtl() {
    TmsTestCaseExecutionCommentAttachmentRQ attachmentRQ = new TmsTestCaseExecutionCommentAttachmentRQ();
    attachmentRQ.setId("1");
    commentRQ.setAttachments(List.of(attachmentRQ));

    TmsAttachment attachment = new TmsAttachment();
    attachment.setId(1L);
    attachment.setExpiresAt(Instant.now());

    when(tmsAttachmentService.getTmsAttachmentsByIds(projectId, List.of(1L)))
        .thenReturn(List.of(attachment));

    sut.createAttachments(projectId, existingComment, commentRQ);

    // Should remove TTL
    assertNull(attachment.getExpiresAt());
    // Should establish relation
    assertEquals(1, existingComment.getAttachments().size());
    assertEquals(1, attachment.getExecutionComments().size());
  }

  @Test
  void createAttachments_WhenAttachmentsNotFound_ShouldThrowException() {
    TmsTestCaseExecutionCommentAttachmentRQ attachmentRQ = new TmsTestCaseExecutionCommentAttachmentRQ();
    attachmentRQ.setId("1");
    commentRQ.setAttachments(List.of(attachmentRQ));

    when(tmsAttachmentService.getTmsAttachmentsByIds(projectId, List.of(1L)))
        .thenThrow(new ReportPortalException(ErrorType.NOT_FOUND, "Attachments not found: [1]"));

    var exception = assertThrows(ReportPortalException.class,
        () -> sut.createAttachments(projectId, existingComment, commentRQ));
    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
  }

  // -------------------------------------------------------------------------
  // UPDATE
  // -------------------------------------------------------------------------

  @Test
  void updateAttachments_WithExistingAttachments_ShouldDeleteOldAndCreateNew() {
    // Setup existing attachment
    existingComment.setAttachments(new HashSet<>(List.of(new TmsAttachment())));

    // Setup new attachment to create
    TmsTestCaseExecutionCommentAttachmentRQ attachmentRQ = new TmsTestCaseExecutionCommentAttachmentRQ();
    attachmentRQ.setId("2");
    commentRQ.setAttachments(List.of(attachmentRQ));

    TmsAttachment newAttachment = new TmsAttachment();
    newAttachment.setId(2L);

    when(tmsAttachmentService.getTmsAttachmentsByIds(projectId, List.of(2L)))
        .thenReturn(List.of(newAttachment));

    sut.updateAttachments(projectId, existingComment, commentRQ);

    verify(tmsTestCaseExecutionCommentAttachmentRepository).deleteByExecutionCommentId(commentId);
    assertEquals(1, existingComment.getAttachments().size());
    assertEquals(newAttachment, existingComment.getAttachments().iterator().next());
  }

  // -------------------------------------------------------------------------
  // DELETE
  // -------------------------------------------------------------------------

  @Test
  void deleteAllByExecutionId_WithNullId_ShouldDoNothing() {
    sut.deleteAllByExecutionId(null);
    verify(tmsTestCaseExecutionCommentAttachmentRepository, never()).deleteByExecutionId(any());
  }

  @Test
  void deleteAllByExecutionId_WithValidId_ShouldDelete() {
    Long executionId = 200L;
    sut.deleteAllByExecutionId(executionId);
    verify(tmsTestCaseExecutionCommentAttachmentRepository).deleteByExecutionId(executionId);
  }

  @Test
  void deleteByLaunchId_ShouldDelete() {
    sut.deleteByLaunchId(300L);
    verify(tmsTestCaseExecutionCommentAttachmentRepository).deleteByLaunchId(300L);
  }
}
