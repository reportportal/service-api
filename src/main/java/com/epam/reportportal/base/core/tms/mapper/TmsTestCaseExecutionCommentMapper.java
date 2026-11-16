package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRS;
import com.epam.reportportal.base.core.tms.mapper.config.CommonMapperConfig;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecution;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecutionComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public interface TmsTestCaseExecutionCommentMapper {

  @Mapping(target = "execution", source = "existingExecution")
  @Mapping(target = "comment", source = "executionCommentRQ.comment")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "attachments", ignore = true)
  TmsTestCaseExecutionComment createTestCaseExecutionComment(
      TmsTestCaseExecution existingExecution, TmsTestCaseExecutionCommentRQ executionCommentRQ);

  TmsTestCaseExecutionCommentRS toTmsTestCaseExecutionCommentRS(TmsTestCaseExecutionComment comment);
}
