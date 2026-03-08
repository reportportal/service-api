package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseExecutionCommentBtsTicketRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecutionComment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecutionCommentBtsTicket;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmsTestCaseExecutionCommentBtsTicketServiceImpl implements
    TmsTestCaseExecutionCommentBtsTicketService {

  private final TmsTestCaseExecutionCommentBtsTicketRepository tmsTestCaseExecutionCommentBtsTicketRepository;

  @Override
  @Transactional
  public void createBtsTickets(TmsTestCaseExecutionComment comment,
      TmsTestCaseExecutionCommentRQ commentRQ) {
    log.debug("Creating BTS tickets for execution comment: {}", comment.getId());

    if (commentRQ == null || CollectionUtils.isEmpty(commentRQ.getBtsTickets())) {
      log.debug("No BTS tickets to create for execution comment: {}", comment.getId());
      return;
    }

    if (comment.getBtsTickets() == null) {
      comment.setBtsTickets(new HashSet<>());
    }

    commentRQ.getBtsTickets().forEach(link -> {
      var ticket = new TmsTestCaseExecutionCommentBtsTicket();
      ticket.setComment(comment);
      ticket.setLink(link);
      tmsTestCaseExecutionCommentBtsTicketRepository.save(ticket);
      comment.getBtsTickets().add(ticket);
    });
    
    log.debug("Created {} BTS tickets for execution comment: {}", 
        commentRQ.getBtsTickets().size(), comment.getId());
  }

  @Override
  @Transactional
  public void updateBtsTickets(TmsTestCaseExecutionComment existingComment,
      TmsTestCaseExecutionCommentRQ commentRQ) {
    log.debug("Updating BTS tickets for execution comment: {}", existingComment.getId());

    // Delete existing relationships
    if (CollectionUtils.isNotEmpty(existingComment.getBtsTickets())) {
      tmsTestCaseExecutionCommentBtsTicketRepository.deleteByExecutionCommentId(existingComment.getId());
      existingComment.getBtsTickets().clear();
      log.debug("Deleted existing BTS tickets for execution comment: {}", existingComment.getId());
    }

    // Create new relationships
    createBtsTickets(existingComment, commentRQ);
  }

  @Override
  @Transactional
  public void deleteAllByExecutionId(Long executionId) {
    log.debug("Deleting all execution comment BTS tickets by execution ID: {}", executionId);

    if (executionId == null) {
      log.warn("Execution ID is null, skipping delete operation");
      return;
    }

    tmsTestCaseExecutionCommentBtsTicketRepository.deleteByExecutionId(executionId);
    log.debug("Deleted all execution comment BTS tickets for execution: {}", executionId);
  }

  @Override
  @Transactional
  public void deleteByLaunchId(Long launchId) {
    log.debug("Deleting all execution comment BTS tickets by launch ID: {}", launchId);
    
    if (launchId == null) {
      log.warn("Launch ID is null, skipping delete operation");
      return;
    }
    
    tmsTestCaseExecutionCommentBtsTicketRepository.deleteByLaunchId(launchId);
  }
}
