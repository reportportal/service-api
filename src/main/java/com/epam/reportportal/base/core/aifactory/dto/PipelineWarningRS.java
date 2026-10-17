package com.epam.reportportal.base.core.aifactory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Something in a report that was accepted but not fully applied. Only returned in the response to a report.
 * <p>
 * Example: a stage row links {@code {entityType: "TEST_CASE", entityId: "TC-404"}} but the
 * project has no test case TC-404. The report is still saved and the response carries
 * {@code {code: "RELATED_ENTITY_NOT_FOUND", stageId: 12, entityType: "TEST_CASE", entityIds: ["TC-404"]}}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineWarningRS {

  /** Related entities of a known type that could not be found, e.g. a test case id that does not exist. */
  public static final String RELATED_ENTITY_NOT_FOUND = "RELATED_ENTITY_NOT_FOUND";

  /** What kind of problem this is, e.g. {@code RELATED_ENTITY_NOT_FOUND}. */
  private String code;
  /** Human-readable explanation. */
  private String message;
  /** The stage the problem belongs to. */
  private Long stageId;
  /** For related-entity warnings: the kind of entity, e.g. {@code TEST_CASE}. */
  private String entityType;
  /** For related-entity warnings: the ids that could not be handled, e.g. {@code ["TC-404"]}. */
  private List<String> entityIds;
}
