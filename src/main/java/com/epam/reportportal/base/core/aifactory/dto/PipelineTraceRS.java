package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.enums.TraceEventType;
import com.epam.reportportal.base.core.aifactory.enums.TraceStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The trace of one attempt of a stage as the Open trace page loads it. It is the only response that carries
 * event texts; the iteration list and the stage panel never do.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineTraceRS {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private Long iterationId;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private String stageKey;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "The attempt the events belong to")
  private int attempt;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "NOT_REPORTED when no trace arrived for "
      + "this attempt; the page then shows \"Trace not reported\" with the reason")
  private TraceStatus status;
  private String reason;
  private Instant reportedAt;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "In the order the attempt ran")
  private List<Event> events;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Event {
    private int seq;
    private TraceEventType type;
    private String title;
    private Long offsetMs;
    private Long durationMs;
    @Schema(description = "Key of the row in the stage's units section that produced the event, e.g. "
        + "\"test-case-review-single#2#1\"; absent for events of the stage itself")
    private String unitKey;
    private String target;
    private String text;
    private Map<String, Object> data;
  }
}
