package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.enums.AttributeValueType;
import com.epam.reportportal.base.core.aifactory.model.PipelineSection;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Two iterations of one pipeline side by side. Nothing here knows what the iterations are about: stages
 * are matched by name, metrics are the iterations' comparable attributes, and per-row detail is joined
 * from the stages' result sections.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineCompareRS {

  private PipelineIterationDetailRS baseline;
  private PipelineIterationDetailRS candidate;

  @Schema(description = "True when the identity attributes of both iterations are equal (e.g. the same requirement), "
      + "so their result rows can be compared case by case")
  private boolean sameScope;

  @Schema(description = "Set when the iterations do not cover the same scope")
  private String note;

  private List<StageCompare> stages;
  private List<MetricCompare> metrics;
  @Schema(description = "Side-by-side rows of result sections present in both iterations; only filled when sameScope")
  private List<SectionCompare> sections;

  public enum Verdict {
    BETTER, WORSE, SAME, NOT_APPLICABLE
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class StageCompare {
    private String name;
    private PipelineStageSummaryRS baseline;
    private PipelineStageSummaryRS candidate;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class MetricCompare {
    private String key;
    private String label;
    private AttributeValueType valueType;
    private String baseline;
    private String candidate;
    @Schema(description = "candidate - baseline, for numeric metrics present in both iterations")
    private String delta;
    private Verdict verdict;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class SectionCompare {
    private String stageName;
    private String sectionKey;
    private String title;
    private List<PipelineSection.Column> columns;
    private List<RowCompare> rows;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class RowCompare {
    private String key;
    private PipelineSection.Row baseline;
    private PipelineSection.Row candidate;
  }
}
