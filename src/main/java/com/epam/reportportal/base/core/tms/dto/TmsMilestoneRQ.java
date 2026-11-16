package com.epam.reportportal.base.core.tms.dto;

import com.epam.reportportal.base.reporting.databind.MultiFormatDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Valid
public class TmsMilestoneRQ {

  private String name;

  private TmsMilestoneType type;

  private TmsMilestoneStatus status;

  @JsonDeserialize(using = MultiFormatDateDeserializer.class)
  private Instant startDate;

  @JsonDeserialize(using = MultiFormatDateDeserializer.class)
  private Instant endDate;
}
