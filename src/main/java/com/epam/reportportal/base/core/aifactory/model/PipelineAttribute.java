package com.epam.reportportal.base.core.aifactory.model;

import com.epam.reportportal.base.core.aifactory.enums.AttributeDirection;
import com.epam.reportportal.base.core.aifactory.enums.AttributeRollup;
import com.epam.reportportal.base.core.aifactory.enums.AttributeType;
import com.epam.reportportal.base.core.aifactory.enums.AttributeValueType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A typed, displayable fact about a pipeline, iteration, stage or stage attempt: the requirement an
 * iteration was run for, its model, cost or score, an {@code env:beta5} tag, a link to the CI run. The
 * type says where the UI puts it, the value type how to format it; the optional hints let ReportPortal
 * aggregate and compare attributes without knowing what they mean.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineAttribute implements Serializable {

  @NotBlank
  @Size(max = 128)
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Identifies the attribute within its owner; "
      + "reporting an attribute with an existing key replaces it")
  private String key;

  @Size(max = 256)
  @Schema(description = "Human readable name; the key is shown when absent")
  private String label;

  @Size(max = 2048)
  @Schema(description = "Value as text; its interpretation is given by valueType")
  private String value;

  @Size(max = 1024)
  @Schema(description = "Longer text that belongs to the value. Example: attribute requirement = "
      + "\"US-TMS-MIG-001\" with description \"Test Case Library empty state display and navigation panel\"; "
      + "the card shows the value, the details header shows both")
  private String description;

  @Builder.Default
  @Schema(description = "Placement hint for the UI (default TAG)")
  private AttributeType type = AttributeType.TAG;

  @Builder.Default
  @Schema(description = "How to interpret and format the value (default TEXT)")
  private AttributeValueType valueType = AttributeValueType.TEXT;

  @Size(max = 2048)
  @Schema(description = "Link target, used with valueType LINK")
  private String url;

  @Schema(description = "Position among attributes of the same type (lower first); attributes without it follow "
      + "in the reported order")
  private Integer order;

  @Schema(description = "For numeric attributes: which direction is an improvement when iterations are compared")
  private AttributeDirection direction;

  @Schema(description = "Stage attributes only: sum this attribute over the stages into the iteration")
  private AttributeRollup rollup;

  @Schema(description = "Marks the attribute that tells what the iteration is about (e.g. the requirement). "
      + "Iterations whose comparison-key attributes are equal cover the same scope, so Compare lines up their "
      + "result rows case by case; otherwise it only notes that the scopes differ")
  private boolean comparisonKey;

  /**
   * Token stored in the iteration's attribute index for an attribute: control characters around the
   * lower-cased key and value, so an exact {@code key=value} filter is a plain substring match that
   * cannot hit part of another key or value.
   */
  public static String indexToken(String key, String value) {
    return "\u0001" + key.trim().toLowerCase(java.util.Locale.ROOT) + "\u0002"
        + (value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT)) + "\u0001";
  }
}
