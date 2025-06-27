package com.epam.ta.reportportal.core.tms.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsManualScenarioPreconditionsRQ {

  private String preconditions;

  private List<TmsManualScenarioAttachmentRQ> attachments;
}
