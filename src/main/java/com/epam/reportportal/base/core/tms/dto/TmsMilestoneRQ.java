package com.epam.reportportal.base.core.tms.dto;

import com.epam.reportportal.base.reporting.databind.MultiFormatDateDeserializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;

@Data
@NoArgsConstructor
@Valid
public class TmsMilestoneRQ {

  private String name;

  private TmsMilestoneType type;

  private TmsMilestoneStatus status;

  private Instant startDate;

  private Instant endDate;

  /**
   * Whether the {@code startDate} property was present in the JSON payload, including an explicit
   * {@code null}. Allows PATCH to tell "clear the date" from "keep the date".
   */
  @JsonIgnore
  private boolean startDateSpecified;

  /**
   * Whether the {@code endDate} property was present in the JSON payload, including an explicit
   * {@code null}.
   */
  @JsonIgnore
  private boolean endDateSpecified;

  // Disabled so Jackson binds through the setters (which record presence), not this constructor.
  @JsonCreator(mode = JsonCreator.Mode.DISABLED)
  @Builder
  public TmsMilestoneRQ(String name, TmsMilestoneType type, TmsMilestoneStatus status,
      Instant startDate, Instant endDate) {
    this.name = name;
    this.type = type;
    this.status = status;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  @JsonSetter("startDate")
  @JsonDeserialize(using = MultiFormatDateDeserializer.class)
  public void setStartDate(Instant startDate) {
    this.startDate = startDate;
    this.startDateSpecified = true;
  }

  @JsonSetter("endDate")
  @JsonDeserialize(using = MultiFormatDateDeserializer.class)
  public void setEndDate(Instant endDate) {
    this.endDate = endDate;
    this.endDateSpecified = true;
  }
}
