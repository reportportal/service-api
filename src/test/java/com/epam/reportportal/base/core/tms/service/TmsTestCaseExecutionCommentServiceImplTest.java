package com.epam.reportportal.base.core.tms.service;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRS;
import com.epam.reportportal.base.core.tms.mapper.TmsTestCaseExecutionCommentMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseExecutionCommentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecution;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecutionComment;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsTestCaseExecutionCommentServiceImplTest {

  private final Long executionId = 100L;
  private final Long commentId = 200L;
  private final Long launchId = 300L;
  private final Long projectId = 1L;
  @Mock
  private TmsTestCaseExecutionCommentRepository tmsTestCaseExecutionCommentRepository;
  @Mock
  private TmsTestCaseExecutionCommentAttachmentService tmsTestCaseExecutionCommentAttachmentService;
  @Mock
  private TmsTestCaseExecutionCommentBtsTicketService tmsTestCaseExecutionCommentBtsTicketService;
  @Mock
  private TmsTestCaseExecutionCommentMapper tmsTestCaseExecutionCommentMapper;
  @InjectMocks
  private TmsTestCaseExecutionCommentServiceImpl sut;
  private TmsTestCaseExecution execution;
  private TmsTestCaseExecutionComment existingComment;
  private TmsTestCaseExecutionCommentRQ request;
  private TmsTestCaseExecutionCommentRS response;

  @BeforeEach
  void setUp() {
    execution = new TmsTestCaseExecution();
    execution.setId(executionId);

    existingComment = new TmsTestCaseExecutionComment();
    existingComment.setId(commentId);
    existingComment.setExecution(execution);

    request = new TmsTestCaseExecutionCommentRQ();
    request.setComment("New comment text");

    response = new TmsTestCaseExecutionCommentRS();
    response.setComment("New comment text");
  }

  // -------------------------------------------------------------------------
  // PUT COMMENT
  // -------------------------------------------------------------------------

  @Test
  void putTestCaseExecutionComment_WhenRequestIsNull_ShouldRemoveExistingComment() {
    execution.setExecutionComment(existingComment);

    var result = sut.putTestCaseExecutionComment(execution, null);

    assertNull(result);
    assertNull(execution.getExecutionComment());
    verify(tmsTestCaseExecutionCommentAttachmentService).deleteAllByExecutionId(executionId);
    verify(tmsTestCaseExecutionCommentBtsTicketService).deleteAllByExecutionId(executionId);
    verify(tmsTestCaseExecutionCommentRepository).deleteById(commentId);
  }

  @Test
  void putTestCaseExecutionComment_WhenCommentExists_ShouldUpdateComment() {
    execution.setExecutionComment(existingComment);

    when(tmsTestCaseExecutionCommentRepository.save(existingComment)).thenReturn(existingComment);
    when(tmsTestCaseExecutionCommentMapper.toTmsTestCaseExecutionCommentRS(existingComment))
        .thenReturn(response);

    var result = sut.putTestCaseExecutionComment(execution, request);

    assertNotNull(result);
    assertEquals("New comment text", existingComment.getComment());

    verify(tmsTestCaseExecutionCommentAttachmentService).updateAttachments(existingComment,
        request);
    verify(tmsTestCaseExecutionCommentBtsTicketService).updateBtsTickets(existingComment, request);
    verify(tmsTestCaseExecutionCommentRepository).save(existingComment);
  }

  @Test
  void putTestCaseExecutionComment_WhenCommentDoesNotExist_ShouldCreateNewComment() {
    execution.setExecutionComment(null);
    var newComment = new TmsTestCaseExecutionComment();
    newComment.setId(commentId);

    when(tmsTestCaseExecutionCommentMapper.createTestCaseExecutionComment(execution, request))
        .thenReturn(newComment);
    when(tmsTestCaseExecutionCommentRepository.save(newComment)).thenReturn(newComment);
    when(tmsTestCaseExecutionCommentMapper.toTmsTestCaseExecutionCommentRS(newComment))
        .thenReturn(response);

    var result = sut.putTestCaseExecutionComment(execution, request);

    assertNotNull(result);
    assertEquals(newComment, execution.getExecutionComment());

    verify(tmsTestCaseExecutionCommentAttachmentService).createAttachments(newComment, request);
    verify(tmsTestCaseExecutionCommentBtsTicketService).createBtsTickets(newComment, request);
    verify(tmsTestCaseExecutionCommentRepository).save(newComment);
  }

  // -------------------------------------------------------------------------
  // PATCH COMMENT
  // -------------------------------------------------------------------------

  @Test
  void patchTestCaseExecutionComment_WhenRequestIsEmpty_ShouldRemoveExistingComment() {
    execution.setExecutionComment(existingComment);
    // Request with no comment, no attachments, no tickets
    TmsTestCaseExecutionCommentRQ emptyRequest = new TmsTestCaseExecutionCommentRQ();

    var result = sut.patchTestCaseExecutionComment(execution, emptyRequest);

    assertNull(result);
    assertNull(execution.getExecutionComment());
    verify(tmsTestCaseExecutionCommentRepository).deleteById(commentId);
  }

  @Test
  void patchTestCaseExecutionComment_WhenCommentExists_ShouldPatchComment() {
    execution.setExecutionComment(existingComment);
    request.setAttachments(List.of(mock(
        com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentAttachmentRQ.class)));

    when(tmsTestCaseExecutionCommentRepository.save(existingComment)).thenReturn(existingComment);
    when(tmsTestCaseExecutionCommentMapper.toTmsTestCaseExecutionCommentRS(existingComment))
        .thenReturn(response);

    var result = sut.patchTestCaseExecutionComment(execution, request);

    assertNotNull(result);
    assertEquals("New comment text", existingComment.getComment());
    verify(tmsTestCaseExecutionCommentAttachmentService).updateAttachments(existingComment,
        request);
    // getBtsTickets is null, so it should not call updateBtsTickets
    verify(tmsTestCaseExecutionCommentBtsTicketService, never()).updateBtsTickets(any(), any());
    verify(tmsTestCaseExecutionCommentRepository).save(existingComment);
  }

  // -------------------------------------------------------------------------
  // DELETE
  // -------------------------------------------------------------------------

  @Test
  void deleteTestCaseExecutionComment_WhenExists_ShouldDelete() {
    when(tmsTestCaseExecutionCommentRepository.existsByExecutionId(executionId)).thenReturn(true);

    sut.deleteTestCaseExecutionComment(projectId, launchId, executionId);

    verify(tmsTestCaseExecutionCommentRepository).deleteByExecutionId(executionId);
  }

  @Test
  void deleteTestCaseExecutionComment_WhenDoesNotExist_ShouldThrowNotFoundException() {
    when(tmsTestCaseExecutionCommentRepository.existsByExecutionId(executionId)).thenReturn(false);

    var exception = assertThrows(ReportPortalException.class,
        () -> sut.deleteTestCaseExecutionComment(projectId, launchId, executionId));

    assertEquals(NOT_FOUND, exception.getErrorType());
    verify(tmsTestCaseExecutionCommentRepository, never()).deleteByExecutionId(any());
  }

  @Test
  void deleteByLaunchId_ShouldDeleteCascading() {
    sut.deleteByLaunchId(launchId);

    verify(tmsTestCaseExecutionCommentAttachmentService).deleteByLaunchId(launchId);
    verify(tmsTestCaseExecutionCommentBtsTicketService).deleteByLaunchId(launchId);
    verify(tmsTestCaseExecutionCommentRepository).deleteByLaunchId(launchId);
  }
}
