package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRS;
import com.epam.reportportal.base.core.tms.mapper.config.CommonMapperConfig;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentBtsTicketRS;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecution;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecutionComment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecutionCommentBtsTicket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public interface TmsTestCaseExecutionCommentMapper {

  @Mapping(target = "execution", source = "existingExecution")
  @Mapping(target = "comment", source = "executionCommentRQ.comment")
  @Mapping(target = "btsTickets", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "attachments", ignore = true)
  TmsTestCaseExecutionComment createTestCaseExecutionComment(
      TmsTestCaseExecution existingExecution, TmsTestCaseExecutionCommentRQ executionCommentRQ);

  TmsTestCaseExecutionCommentRS toTmsTestCaseExecutionCommentRS(TmsTestCaseExecutionComment comment);

  TmsTestCaseExecutionCommentBtsTicketRS toTmsTestCaseExecutionCommentBtsTicketRS(
      TmsTestCaseExecutionCommentBtsTicket ticket);
}
