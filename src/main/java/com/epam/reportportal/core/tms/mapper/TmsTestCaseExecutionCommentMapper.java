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

  @Mapping(target = "execution", source = "existingExecution")
  @Mapping(target = "comment", source = "executionCommentRQ.comment")
  @Mapping(target = "btsTicketId", source = "executionCommentRQ.btsTicket.id")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "attachments", ignore = true)
  TmsTestCaseExecutionComment createTestCaseExecutionComment(
      TmsTestCaseExecution existingExecution, TmsTestCaseExecutionCommentRQ executionCommentRQ);

  @Mapping(target = "btsTicket.id", source = "btsTicketId")
  TmsTestCaseExecutionCommentRS toTmsTestCaseExecutionCommentRS(TmsTestCaseExecutionComment comment);
}
