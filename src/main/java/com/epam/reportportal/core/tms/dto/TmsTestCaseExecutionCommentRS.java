package com.epam.reportportal.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TmsTestCaseExecutionCommentRS {

  private String comment;

  private TmsTestCaseExecutionCommentBtsTicketRS btsTicket;

  private List<TmsTestCaseExecutionCommentAttachmentRS> attachments;
}
