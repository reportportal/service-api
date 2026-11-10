package com.epam.reportportal.base.core.aifactory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The user an iteration was started by.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineUserRS {
  private Long id;
  private String login;
  private String fullName;
}
