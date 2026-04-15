package com.epam.reportportal.base.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseExecutionCommentBtsTicketRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecutionComment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecutionCommentBtsTicket;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsTestCaseExecutionCommentBtsTicketServiceImplTest {

  private final Long commentId = 100L;
  private final Long executionId = 200L;
  private final Long launchId = 300L;
  @Mock
  private TmsTestCaseExecutionCommentBtsTicketRepository repository;
  @InjectMocks
  private TmsTestCaseExecutionCommentBtsTicketServiceImpl sut;
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
  void createBtsTickets_WhenTicketsEmpty_ShouldDoNothing() {
    commentRQ.setBtsTickets(Collections.emptyList());
    sut.createBtsTickets(existingComment, commentRQ);
    verify(repository, never()).save(any(TmsTestCaseExecutionCommentBtsTicket.class));
  }
  
  @Test
  void createBtsTickets_WhenRequestIsNull_ShouldDoNothing() {
    sut.createBtsTickets(existingComment, null);
    verify(repository, never()).save(any(TmsTestCaseExecutionCommentBtsTicket.class));
  }

  @Test
  void createBtsTickets_WithValidTickets_ShouldCreateAndSave() {
    commentRQ.setBtsTickets(List.of("http://jira.com/123", "http://jira.com/456"));
  
    var idGenerator = new AtomicLong(1);
    when(repository.save(any(TmsTestCaseExecutionCommentBtsTicket.class))).thenAnswer(inv -> {
      TmsTestCaseExecutionCommentBtsTicket t = inv.getArgument(0);
      t.setId(idGenerator.getAndIncrement());
      return t;
    });
  
    sut.createBtsTickets(existingComment, commentRQ);
  
    assertNotNull(existingComment.getBtsTickets());
    assertEquals(2, existingComment.getBtsTickets().size());
    verify(repository, org.mockito.Mockito.times(2)).save(any(TmsTestCaseExecutionCommentBtsTicket.class));
  }

  // -------------------------------------------------------------------------
  // UPDATE
  // -------------------------------------------------------------------------

  @Test
  void updateBtsTickets_WithExistingTickets_ShouldDeleteOldAndCreateNew() {
    var oldTicket = new TmsTestCaseExecutionCommentBtsTicket();
    oldTicket.setId(10L);
    existingComment.setBtsTickets(new HashSet<>(List.of(oldTicket)));
  
    commentRQ.setBtsTickets(List.of("http://jira.com/999"));
  
    var idGenerator = new AtomicLong(11);
    when(repository.save(any(TmsTestCaseExecutionCommentBtsTicket.class))).thenAnswer(inv -> {
      TmsTestCaseExecutionCommentBtsTicket t = inv.getArgument(0);
      t.setId(idGenerator.getAndIncrement());
      return t;
    });
  
    sut.updateBtsTickets(existingComment, commentRQ);

    verify(repository).deleteByExecutionCommentId(commentId);
    assertEquals(1, existingComment.getBtsTickets().size());
    verify(repository).save(any(TmsTestCaseExecutionCommentBtsTicket.class));
  }

  // -------------------------------------------------------------------------
  // DELETE
  // -------------------------------------------------------------------------

  @Test
  void deleteAllByExecutionId_WithNullId_ShouldDoNothing() {
    sut.deleteAllByExecutionId(null);
    verify(repository, never()).deleteByExecutionId(anyLong());
  }
  
  @Test
  void deleteAllByExecutionId_WithValidId_ShouldDelete() {
    sut.deleteAllByExecutionId(executionId);
    verify(repository).deleteByExecutionId(executionId);
  }
  
  @Test
  void deleteByLaunchId_WithNullId_ShouldDoNothing() {
    sut.deleteByLaunchId(null);
    verify(repository, never()).deleteByLaunchId(anyLong());
  }

  @Test
  void deleteByLaunchId_WithValidId_ShouldDelete() {
    sut.deleteByLaunchId(launchId);
    verify(repository).deleteByLaunchId(launchId);
  }
}
