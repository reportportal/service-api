package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.enums.StageOwner;
import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.core.aifactory.model.PipelineLink;
import com.epam.reportportal.base.core.aifactory.model.PipelineSection;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineStageRS {
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private Long id;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private Integer sequence;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "1-based number of the stage's current attempt; bumped by a retry or a new report")
  private int attempt;
  private Long parentStageId;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Status text exactly as reported")
  private String status;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private StatusCategory statusCategory;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private StageOwner ownedBy;
  private Instant startedAt;
  private Instant finishedAt;
  private Long durationMillis;
  private List<PipelineAttribute> attributes;
  @Schema(description = "Short text for the stage chip, e.g. \"4 cases\" or \"79\"; as reported")
  private String summary;
  @Schema(description = "What the stage is doing or waiting for right now")
  private String message;
  @Schema(description = "Whether the stage's status counts towards the iteration status; false for a stage that "
      + "is evaluation only (e.g. Grade)")
  private boolean affectsIterationStatus;
  @Schema(description = "Things elsewhere in ReportPortal the stage worked on or produced, collected by ReportPortal from the "
      + "cell targets of its sections (the agent sends each only once, in the row). "
      + "Example: an Upload stage whose rows link TC-12 returns "
      + "[{entityType: \"TEST_CASE\", entityId: \"TC-12\"}], which the UI shows as a link to that test case")
  private List<PipelineSection.EntityReference> relatedEntities;
  @Schema(description = "Result tables of the stage")
  private List<PipelineSection> sections;
  private PipelineStepResultRS result;
  @Schema(description = "The raw output the stage reported, exactly as received")
  private Map<String, Object> rawOutput;
  private String rawOutputSchemaName;
  @Schema(description = "Links out of the stage, e.g. to the run that executed it")
  private List<PipelineLink> links;
  @Schema(description = "Whether the stage can be run again through the handler of its execution type")
  private boolean retryable;
  @Schema(description = "Earlier finished attempts of this stage, oldest first; the current attempt is the stage itself")
  private List<PipelineStageAttemptRS> attempts;
  private Instant lastRetriedAt;
  private Long lastRetriedBy;
}
