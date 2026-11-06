package com.epam.reportportal.base.core.aifactory.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A link out of a stage: to a page elsewhere ({@code url}, e.g. the run that executed it) or to something
 * in ReportPortal ({@code target}, which ReportPortal does not interpret - the UI resolves it).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineLink implements Serializable {

  public static final String RUN = "RUN";

  @NotBlank
  @Size(max = 64)
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Kind of link, e.g. RUN")
  private String type;
  @Size(max = 2048)
  private String url;
  @Size(max = 255)
  private String label;
  @Valid
  @Schema(description = "A thing inside ReportPortal the link leads to; type and ref are opaque to the API")
  private PipelineSection.EntityReference target;
}
