package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.enums.TraceEventType;
import com.epam.reportportal.base.core.aifactory.enums.TraceStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The trace of one attempt of a stage, or a part of it, reported by the CI job in its own request.
 * <p>
 * Example: a job that cannot carry the whole attempt in one request sends events 0-499, then 500-812, both with
 * {@code attempt = 1}; sending either part again changes nothing. A job that could not collect the trace sends
 * {@code status = NOT_REPORTED} with a {@code reason} and no events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineTraceRQ {

  @NotNull
  @Min(1)
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Attempt of the stage the events belong to: "
      + "the attempt number the stage reported, 1 for its first run")
  private Integer attempt;

  @Builder.Default
  @Schema(description = "REPORTED (default) when the request carries events; NOT_REPORTED when the job could not "
      + "collect the trace, then the reason says why and the stage result is untouched")
  private TraceStatus status = TraceStatus.REPORTED;

  @Size(max = 4000)
  @Schema(description = "Why the trace could not be reported; shown on the trace page")
  private String reason;

  @Valid
  @Size(max = 2000)
  @Schema(description = "Events of this request. An event with a seq that is already stored replaces it, so "
      + "reporting the same attempt or part again does not duplicate events")
  private List<Event> events;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Event {

    @NotNull
    @Min(0)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Position of the event within the attempt; "
        + "events are shown in this order")
    private Integer seq;

    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private TraceEventType type;

    @Size(max = 512)
    @Schema(description = "Shown on the collapsed event, e.g. the tool name or \"Read requirement\"")
    private String title;

    @Min(0)
    @Schema(description = "Milliseconds from the start of the attempt")
    private Long offsetMs;

    @Min(0)
    @Schema(description = "TOOL_CALL: how long the call took")
    private Long durationMs;

    @Size(max = 1024)
    @Schema(description = "TOOL_CALL: what the call acted on, a file, a Library id or a command")
    private String target;

    @Size(max = 10_000_000)
    @Schema(description = "Full text of the event; stored and returned without cutting. Must not contain credentials")
    private String text;

    @Schema(description = "Structured part, e.g. a GRADERS event: {\"caseId\": \"TC-12\", \"total\": 88, "
        + "\"criteria\": [{\"name\": \"Clear expected results\", \"score\": 17, \"max\": 20}]}")
    private Map<String, Object> data;
  }
}
