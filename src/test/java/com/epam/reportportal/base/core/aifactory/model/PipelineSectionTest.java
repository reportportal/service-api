package com.epam.reportportal.base.core.aifactory.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.core.aifactory.PipelineTestData;
import com.epam.reportportal.base.core.aifactory.enums.SectionRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class PipelineSectionTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void entityReferences_ShouldListEachTargetOnceInTheOrderOfAppearance() {
    var sections = PipelineTestData.sectionLinking(List.of(
        new PipelineSection.EntityReference("TEST_CASE", "TC-2"),
        new PipelineSection.EntityReference("TEST_CASE", "TC-1"),
        new PipelineSection.EntityReference("TEST_CASE", "TC-2")));

    var references = PipelineSection.entityReferences(sections);

    assertEquals(List.of(new PipelineSection.EntityReference("TEST_CASE", "TC-2"),
        new PipelineSection.EntityReference("TEST_CASE", "TC-1")), references);
  }

  @Test
  void entityReferences_WithoutSectionsOrTargets_ShouldBeEmpty() {
    assertTrue(PipelineSection.entityReferences(null).isEmpty());
    var plain = PipelineSection.builder().key("t").rows(List.of(PipelineSection.Row.builder()
        .cells(java.util.Map.of("a", PipelineSection.Cell.builder().value("x").build())).build())).build();
    assertTrue(PipelineSection.entityReferences(List.of(plain)).isEmpty());
  }

  @Test
  void fromJson_ShouldDefaultTheRoleToResultAndReadTheRowDetails() throws Exception {
    var json = """
        {"key": "graded", "columns": [{"key": "name"}],
         "rows": [{"key": "r1", "cells": {"name": "Case 1"},
                   "details": [{"heading": "Clear steps 12/20", "items": ["Two actions in one step"]}]}]}
        """;

    var section = mapper.readValue(json, PipelineSection.class);

    assertEquals(SectionRole.RESULT, section.getRole());
    var details = section.getRows().get(0).getDetails();
    assertEquals("Clear steps 12/20", details.get(0).getHeading());
    assertEquals(List.of("Two actions in one step"), details.get(0).getItems());
  }

  @Test
  void fromJson_ShouldReadTheDetailsRole() throws Exception {
    var section = mapper.readValue("{\"key\": \"tokenUsage\", \"role\": \"DETAILS\"}", PipelineSection.class);

    assertEquals(SectionRole.DETAILS, section.getRole());
  }
}
