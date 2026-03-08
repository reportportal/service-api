package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentAttachmentRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentBtsTicketRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionTestFolderRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseSnapshotDTO;
import com.epam.reportportal.base.core.tms.mapper.config.CommonMapperConfig;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttachment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecution;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecutionComment;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@Mapper(config = CommonMapperConfig.class)
@Slf4j
public abstract class TmsTestCaseExecutionMapper {

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * Converts TmsTestCaseExecution entity to TmsTestCaseExecutionRS DTO.
   *
   * @param execution the test case execution entity
   * @return the test case execution response DTO
   */
  public TmsTestCaseExecutionRS convert(TmsTestCaseExecution execution) {
    if (execution == null) {
      return null;
    }

    var builder = TmsTestCaseExecutionRS.builder();

    // Basic fields
    builder.id(execution.getId());
    builder.testCaseId(execution.getTestCaseId());
    builder.testCaseVersionId(execution.getTestCaseVersionId());
    builder.testCasePriority(execution.getPriority());
    builder.testItemId(
        execution.getTestItem() != null ? execution.getTestItem().getItemId() : null);

    // Status and timing from TestItem
    if (execution.getTestItem() != null) {
      var startTime = execution.getTestItem().getStartTime();
      builder.startedAt(startTime != null ? startTime.toEpochMilli() : null);

      if (execution.getTestItem().getItemResults() != null) {
        // Status
        if (execution.getTestItem().getItemResults().getStatus() != null) {
          builder.executionStatus(execution.getTestItem().getItemResults().getStatus().name());
        }

        // End time
        var endTime = execution.getTestItem().getItemResults().getEndTime();
        builder.finishedAt(endTime != null ? endTime.toEpochMilli() : null);

        // Duration calculation
        if (startTime != null && endTime != null) {
          var duration = Duration.between(startTime, endTime);
          builder.duration(duration.toMillis());
        }
      }
    }

    // Parse snapshot to get test case details
    var snapshot = parseSnapshot(execution.getTestCaseSnapshot());
    if (snapshot != null) {
      builder.testCaseName(snapshot.getName());
      builder.testCaseDescription(snapshot.getDescription());
      if (snapshot.getTestFolder() != null) {
        builder.testFolder(
            TmsTestCaseExecutionTestFolderRS.builder()
                .id(snapshot.getTestFolder().getId())
                .testItemId(execution.getTestItem().getParentId())
                .build()
        );
      }
      builder.manualScenario(snapshot.getManualScenario());
      builder.attributes(snapshot.getAttributes());
    }

    // Execution comment
    if (execution.getExecutionComment() != null) {
      builder.executionComment(convertToExecutionCommentRS(execution.getExecutionComment()));
    }

    return builder.build();
  }

  public Page<TmsTestCaseExecutionRS> convertToPageTmsTestCaseExecutionRS(
      Page<TmsTestCaseExecution> executionsPage, Pageable pageable) {
    var tmsTestCaseExecutionRSList = executionsPage
        .getContent()
        .stream()
        .map(this::convert)
        .toList();
    return new PageImpl<>(
        tmsTestCaseExecutionRSList,
        pageable,
        executionsPage.getTotalElements()
    );
  }


  public String createTestCaseSnapshot(TmsTestCaseRS testCase) {
    try {
      return objectMapper.writeValueAsString(testCase);
    } catch (JsonProcessingException e) {
      log.error("Failed to create test case snapshot for test case: {}",
          testCase.getId(), e);
      throw new ReportPortalException(
          ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR,
          "Failed to serialize test case snapshot: " + e.getMessage()
      );
    }
  }

  public TmsTestCaseExecution createTestCaseExecution(TmsTestCaseRS testCase, Launch launch,
      TestItem testItem, Long defaultVersionId) {
    var snapshot = createTestCaseSnapshot(testCase);

    // Create TmsTestCaseExecution
    var execution = new TmsTestCaseExecution();
    execution.setTestCaseId(testCase.getId());
    execution.setName(testCase.getName());
    execution.setLaunchId(launch.getId());
    execution.setTestItem(testItem);
    execution.setPriority(testCase.getPriority());
    execution.setTestCaseVersionId(defaultVersionId);
    execution.setTestCaseSnapshot(snapshot);

    return execution;
  }

  /**
   * Parses test case snapshot JSON string to DTO.
   *
   * @param snapshotJson the JSON string
   * @return parsed snapshot DTO or null if parsing fails
   */
  private TmsTestCaseSnapshotDTO parseSnapshot(String snapshotJson) {
    if (snapshotJson == null || snapshotJson.isEmpty()) {
      log.warn("Test case snapshot is empty");
      return null;
    }

    try {
      return objectMapper.readValue(snapshotJson, TmsTestCaseSnapshotDTO.class);
    } catch (JsonProcessingException e) {
      log.error("Failed to parse test case snapshot: {}", e.getMessage(), e);
      return null;
    }
  }

  /**
   * Converts TmsTestCaseExecutionComment entity to DTO.
   *
   * @param comment the comment entity
   * @return the comment DTO
   */
  private TmsTestCaseExecutionCommentRS convertToExecutionCommentRS(
      TmsTestCaseExecutionComment comment) {
    if (comment == null) {
      return null;
    }

    var attachmentRSList = comment.getAttachments() != null ?
        comment
            .getAttachments()
            .stream()
            .map(this::convertCommentAttachment)
            .collect(Collectors.toList())
        : null;

    var result = new TmsTestCaseExecutionCommentRS();

    result.setComment(comment.getComment());
    if (CollectionUtils.isNotEmpty(attachmentRSList)) {
      result.setAttachments(attachmentRSList);
    }
    
    if (CollectionUtils.isNotEmpty(comment.getBtsTickets())) {
      result.setBtsTickets(comment.getBtsTickets().stream()
          .map(ticket -> TmsTestCaseExecutionCommentBtsTicketRS.builder()
              .id(ticket.getId())
              .link(ticket.getLink())
              .build())
          .toList());
    }

    return result;
  }

  /**
   * Converts TmsTestCaseExecutionCommentAttachment entity to DTO.
   *
   * @param attachment the attachment junction entity
   * @return the attachment DTO
   */
  private TmsTestCaseExecutionCommentAttachmentRS convertCommentAttachment(
      TmsAttachment attachment) {
    if (attachment == null) {
      return null;
    }

    return TmsTestCaseExecutionCommentAttachmentRS.builder()
        .id(attachment.getId())
        .fileName(attachment.getFileName())
        .fileType(attachment.getFileType())
        .fileSize(attachment.getFileSize())
        .build();
  }
}
