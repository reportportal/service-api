package com.epam.reportportal.core.tms.mapper;

import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionCommentRS;
import com.epam.reportportal.core.tms.mapper.config.CommonMapperConfig;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCaseExecution;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCaseExecutionComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public interface TmsTestCaseExecutionCommentMapper {

  default TmsTestCaseExecutionComment createTestCaseExecutionComment(
      TmsTestCaseExecution existingExecution, TmsTestCaseExecutionCommentRQ executionCommentRQ) {
    return TmsTestCaseExecutionComment.builder()
        .execution(existingExecution)
        .comment(executionCommentRQ.getComment())
        .btsTicketId(executionCommentRQ.getBtsTicket().getId())
        .build();
  }

  @Mapping(target = "btsTicket.id", source = "btsTicketId")
  TmsTestCaseExecutionCommentRS toTmsTestCaseExecutionCommentRS(TmsTestCaseExecutionComment comment);
}
