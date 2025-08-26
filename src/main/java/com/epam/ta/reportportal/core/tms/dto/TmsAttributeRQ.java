package com.epam.ta.reportportal.core.tms.dto;

import com.epam.ta.reportportal.core.tms.validation.ValidTmsAttributeRQ;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ValidTmsAttributeRQ
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsAttributeRQ {

  private Long id;

  private String key;

  private String value;
}
