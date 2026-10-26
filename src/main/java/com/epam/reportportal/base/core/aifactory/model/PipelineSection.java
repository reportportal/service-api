package com.epam.reportportal.base.core.aifactory.model;

import com.epam.reportportal.base.core.aifactory.enums.AttributeValueType;
import com.epam.reportportal.base.core.aifactory.enums.SectionRole;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A named table a stage reports about what it did - graded cases, an upload report, token usage per
 * model, fix rounds, anything. The UI renders every section with the same table component, so a new
 * kind of result needs no new endpoint or field.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineSection implements Serializable {

  @NotBlank
  @Size(max = 128)
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Identifies the section within its stage; "
      + "reporting a section with an existing key replaces it")
  private String key;

  @Size(max = 256)
  private String title;

  @Builder.Default
  @Schema(description = "RESULT (default) for what the stage produced, DETAILS for how it ran, e.g. token usage")
  private SectionRole role = SectionRole.RESULT;

  @Valid
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private List<Column> columns;

  @Valid
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private List<Row> rows;

  /**
   * The distinct entities the cells of the given sections point to, in the order they appear.
   * <p>
   * Example: an upload report whose rows carry the targets TEST_CASE TC-12 and TEST_CASE TC-13 yields those two.
   */
  public static List<EntityReference> entityReferences(List<PipelineSection> sections) {
    var references = new LinkedHashSet<EntityReference>();
    if (sections != null) {
      for (var section : sections) {
        for (var row : section.getRows() == null ? List.<Row>of() : section.getRows()) {
          for (var cell : row.getCells() == null ? List.<Cell>of() : row.getCells().values()) {
            if (cell != null && cell.getTarget() != null) {
              references.add(cell.getTarget());
            }
          }
        }
      }
    }
    return new java.util.ArrayList<>(references);
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Column implements Serializable {

    @NotBlank
    @Size(max = 128)
    private String key;

    @Size(max = 256)
    private String label;

    @Builder.Default
    private AttributeValueType valueType = AttributeValueType.TEXT;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Row implements Serializable {

    @Size(max = 256)
    @Schema(description = "Identifies the row within the section; rows with the same key in two iterations "
        + "are matched when they are compared")
    private String key;

    @Valid
    @Schema(description = "Cells by column key. A cell may be sent as a bare scalar instead of an object")
    private Map<String, Cell> cells;

    @Valid
    @Schema(description = "What the row shows when it is expanded. Example for a graded test case: "
        + "[{heading: \"Clear expected results 17/20\", items: [\"Step 3 does not state which fields the modal "
        + "shows.\"]}]")
    private List<Detail> details;
  }

  /**
   * One block of an expanded row: a heading and the points under it.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Detail implements Serializable {

    @Size(max = 256)
    private String heading;

    @Size(max = 100)
    @Schema(description = "The points under the heading, e.g. failure reasons")
    private List<@Size(max = 2048) String> items;
  }

  /**
   * A table cell: a value, optionally with a link or a reference to an entity the UI knows how to open.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Cell implements Serializable {

    @Size(max = 2048)
    private String value;

    @Size(max = 2048)
    private String url;

    @Valid
    private EntityReference target;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Cell fromJson(JsonNode node) {
      if (node == null || node.isNull()) {
        return new Cell();
      }
      if (!node.isObject()) {
        return Cell.builder().value(node.asText()).build();
      }
      var cell = new Cell();
      cell.setValue(text(node, "value"));
      cell.setUrl(text(node, "url"));
      var target = node.get("target");
      if (target != null && target.isObject()) {
        cell.setTarget(new EntityReference(text(target, "entityType"), text(target, "entityId")));
      }
      return cell;
    }

    private static String text(JsonNode node, String field) {
      var child = node.get(field);
      return child == null || child.isNull() ? null : child.asText();
    }
  }

  /**
   * A thing owned by another part of ReportPortal, named by its kind and its id. ReportPortal's pipeline
   * layer does not interpret it; a resolver or the UI does.
   * <p>
   * Example: {@code {entityType: "TEST_CASE", entityId: "TC-12"}} is test case TC-12 of the Test Case
   * Library; a table cell with that target is shown as a link to the test case.
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class EntityReference implements Serializable {

    /** Kind of entity, e.g. {@code TEST_CASE}. */
    @NotBlank
    @Size(max = 64)
    private String entityType;

    /** Id of the entity as its owner knows it, e.g. the display id {@code TC-12}. */
    @NotBlank
    @Size(max = 255)
    private String entityId;
  }
}
