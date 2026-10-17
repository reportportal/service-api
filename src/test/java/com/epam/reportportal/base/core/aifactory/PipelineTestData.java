package com.epam.reportportal.base.core.aifactory;

import com.epam.reportportal.base.core.aifactory.model.PipelineSection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Builders for report fragments shared by the pipeline tests.
 */
public final class PipelineTestData {

  private PipelineTestData() {
  }

  /**
   * A result table with one row per entity whose "item" cell links to that entity, the way an upload report
   * links the test cases it created.
   */
  public static List<PipelineSection> sectionLinking(List<PipelineSection.EntityReference> entities) {
    var rows = entities.stream().map(entity -> {
      var cells = new LinkedHashMap<String, PipelineSection.Cell>();
      cells.put("item", PipelineSection.Cell.builder().value(entity.getEntityId()).target(entity).build());
      return PipelineSection.Row.builder().key(entity.getEntityId()).cells(cells).build();
    }).toList();
    return List.of(PipelineSection.builder().key("report")
        .columns(List.of(PipelineSection.Column.builder().key("item").label("Item").build()))
        .rows(rows).build());
  }
}
