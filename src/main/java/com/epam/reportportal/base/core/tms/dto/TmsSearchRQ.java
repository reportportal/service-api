package com.epam.reportportal.base.core.tms.dto;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.ConvertibleCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for TMS search endpoints. Carries the same filter conditions as the
 * {@code filter.<condition>.<criteria>=<value>} query parameters of the GET endpoints, but in the
 * request body, so a large number of values (e.g. tag IDs) does not hit URL length limits.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsSearchRQ {

  @Valid
  @Builder.Default
  @Schema(description = "Filter conditions combined with AND")
  private List<TmsSearchConditionRQ> conditions = new ArrayList<>();

  /**
   * Converts the request into a {@link Filter} for the given target entity.
   *
   * @param target Entity class the filter is built for.
   * @return Filter containing all conditions from the request.
   */
  public Filter toFilter(Class<?> target) {
    List<ConvertibleCondition> filterConditions = new ArrayList<>();
    if (conditions != null) {
      conditions.forEach(c -> filterConditions.add(c.toFilterCondition()));
    }
    return new Filter(target, filterConditions);
  }

  /**
   * Single filter condition.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TmsSearchConditionRQ {

    @NotBlank
    @Schema(description = "Filter criteria, e.g. 'attributes', 'name', 'testFolderId'",
        example = "attributes")
    private String filterKey;

    @NotBlank
    @Schema(description = "Condition marker, same as in GET filters: eq, ne, cnt, in, has, any, gt, lt, ...",
        example = "has")
    private String condition;

    @NotBlank
    @Schema(description = "Condition value; multiple values are comma-separated", example = "1,2,3")
    private String value;

    @Schema(description = "Negate the condition (same as '!' prefix in GET filters)",
        defaultValue = "false")
    private boolean negative;

    FilterCondition toFilterCondition() {
      Condition parsed = Condition.findByMarker(condition)
          .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_FILTER_PARAMETERS,
              "Unable to find condition with marker '" + condition + "'"));
      return new FilterCondition(parsed, negative, value, filterKey);
    }
  }
}
