package com.epam.reportportal.base.core.aifactory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.core.aifactory.dto.PipelineCompareRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationDetailRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRS;
import com.epam.reportportal.base.core.aifactory.enums.AttributeDirection;
import com.epam.reportportal.base.core.aifactory.enums.AttributeType;
import com.epam.reportportal.base.core.aifactory.enums.AttributeValueType;
import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.core.aifactory.model.PipelineSection;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PipelineComparatorTest {

  private final PipelineComparator comparator = new PipelineComparator();

  private static PipelineAttribute requirement(String value) {
    return PipelineAttribute.builder().key("requirement").value(value).type(AttributeType.META).comparisonKey(true).build();
  }

  private static PipelineAttribute metric(String key, String value, AttributeValueType type,
      AttributeDirection direction) {
    return PipelineAttribute.builder().key(key).label(key).value(value).type(AttributeType.META).valueType(type)
        .direction(direction).build();
  }

  private static PipelineStageRS stage(String name, String status, StatusCategory category,
      List<PipelineSection> sections) {
    return PipelineStageRS.builder().name(name).status(status).statusCategory(category).sections(sections).build();
  }

  private static PipelineSection grades(String key, String score) {
    return PipelineSection.builder().key("grades").title("Grades")
        .columns(List.of(PipelineSection.Column.builder().key("score").build()))
        .rows(List.of(PipelineSection.Row.builder().key(key)
            .cells(Map.of("score", PipelineSection.Cell.builder().value(score).build())).build()))
        .build();
  }

  private static PipelineIterationDetailRS iteration(List<PipelineAttribute> attributes, PipelineStageRS... stages) {
    return PipelineIterationDetailRS.builder().attributes(attributes).stages(List.of(stages)).build();
  }

  @Test
  void compare_ShouldComputeDeltasAndVerdictsFromAttributeDirection() {
    var baseline = iteration(List.of(requirement("US-1"),
        metric("suiteScore", "79", AttributeValueType.SCORE, AttributeDirection.HIGHER_BETTER),
        metric("cost", "1.49", AttributeValueType.MONEY, AttributeDirection.LOWER_BETTER),
        metric("cases", "4", AttributeValueType.NUMBER, AttributeDirection.NONE)));
    var candidate = iteration(List.of(requirement("US-1"),
        metric("suiteScore", "83", AttributeValueType.SCORE, AttributeDirection.HIGHER_BETTER),
        metric("cost", "1.62", AttributeValueType.MONEY, AttributeDirection.LOWER_BETTER),
        metric("cases", "4", AttributeValueType.NUMBER, AttributeDirection.NONE)));

    var result = comparator.compare(baseline, candidate);

    var byKey = result.getMetrics().stream()
        .collect(java.util.stream.Collectors.toMap(PipelineCompareRS.MetricCompare::getKey, m -> m));
    assertEquals("4", byKey.get("suiteScore").getDelta());
    assertEquals(PipelineCompareRS.Verdict.BETTER, byKey.get("suiteScore").getVerdict());
    assertEquals("0.13", byKey.get("cost").getDelta());
    assertEquals(PipelineCompareRS.Verdict.WORSE, byKey.get("cost").getVerdict());
    assertEquals(PipelineCompareRS.Verdict.NOT_APPLICABLE, byKey.get("cases").getVerdict());
    assertEquals("79", byKey.get("suiteScore").getBaseline());
    assertEquals("83", byKey.get("suiteScore").getCandidate());
  }

  @Test
  void compare_ShouldListMetricsPresentOnlyOnOneSideWithoutDelta() {
    var baseline = iteration(List.of(metric("cost", "1", AttributeValueType.MONEY, AttributeDirection.LOWER_BETTER)));
    var candidate = iteration(List.of(metric("suiteScore", "83", AttributeValueType.SCORE,
        AttributeDirection.HIGHER_BETTER)));

    var result = comparator.compare(baseline, candidate);

    assertEquals(2, result.getMetrics().size());
    result.getMetrics().forEach(m -> {
      assertNull(m.getDelta());
      assertEquals(PipelineCompareRS.Verdict.NOT_APPLICABLE, m.getVerdict());
    });
  }

  @Test
  void compare_WithNonNumericAttributes_ShouldNotListThemAsMetrics() {
    var attrs = List.of(PipelineAttribute.builder().key("model").value("auto").build());

    var result = comparator.compare(iteration(attrs), iteration(attrs));

    assertTrue(result.getMetrics().isEmpty());
  }

  @Test
  void verdict_ShouldHandleEqualValuesAndLowerIsBetter() {
    assertEquals(PipelineCompareRS.Verdict.SAME, comparator.verdict(BigDecimal.ZERO, AttributeDirection.HIGHER_BETTER));
    assertEquals(PipelineCompareRS.Verdict.BETTER,
        comparator.verdict(new BigDecimal("-2"), AttributeDirection.LOWER_BETTER));
    assertEquals(PipelineCompareRS.Verdict.WORSE,
        comparator.verdict(new BigDecimal("-2"), AttributeDirection.HIGHER_BETTER));
    assertEquals(PipelineCompareRS.Verdict.NOT_APPLICABLE, comparator.verdict(null, AttributeDirection.HIGHER_BETTER));
    assertEquals(PipelineCompareRS.Verdict.NOT_APPLICABLE, comparator.verdict(BigDecimal.ONE, null));
  }

  @Test
  void compare_ShouldMatchStagesByNameAndKeepOneSidedOnes() {
    var baseline = iteration(List.of(), stage("Create", "Passed", StatusCategory.PASSED, null),
        stage("Removed", "Failed", StatusCategory.FAILED, null));
    var candidate = iteration(List.of(), stage("Create", "Failed", StatusCategory.FAILED, null),
        stage("New", "Passed", StatusCategory.PASSED, null));

    var result = comparator.compare(baseline, candidate);

    assertEquals(List.of("Create", "New", "Removed"),
        result.getStages().stream().map(PipelineCompareRS.StageCompare::getName).toList());
    var create = result.getStages().get(0);
    assertEquals("Passed", create.getBaseline().getStatus());
    assertEquals("Failed", create.getCandidate().getStatus());
    assertNull(result.getStages().get(1).getBaseline());
    assertNull(result.getStages().get(2).getCandidate());
  }

  @Test
  void compare_WithEqualIdentity_ShouldBeSameScopeAndJoinResultRowsByKey() {
    var baseline = iteration(List.of(requirement("US-1")),
        stage("Grade", "Passed", StatusCategory.PASSED, List.of(grades("TC-1", "70"))));
    var candidate = iteration(List.of(requirement("US-1")),
        stage("Grade", "Passed", StatusCategory.PASSED, List.of(grades("TC-1", "90"))));

    var result = comparator.compare(baseline, candidate);

    assertTrue(result.isSameScope());
    assertNull(result.getNote());
    assertEquals(1, result.getSections().size());
    var section = result.getSections().get(0);
    assertEquals("Grade", section.getStageName());
    assertEquals("grades", section.getSectionKey());
    var row = section.getRows().get(0);
    assertEquals("TC-1", row.getKey());
    assertEquals("70", row.getBaseline().getCells().get("score").getValue());
    assertEquals("90", row.getCandidate().getCells().get("score").getValue());
  }

  @Test
  void compare_WithDifferentIdentity_ShouldNoteItAndSkipRows() {
    var baseline = iteration(List.of(requirement("US-1")),
        stage("Grade", "Passed", StatusCategory.PASSED, List.of(grades("TC-1", "70"))));
    var candidate = iteration(List.of(requirement("US-2")),
        stage("Grade", "Passed", StatusCategory.PASSED, List.of(grades("TC-1", "90"))));

    var result = comparator.compare(baseline, candidate);

    assertFalse(result.isSameScope());
    assertEquals(PipelineComparator.DIFFERENT_SCOPE_NOTE, result.getNote());
    assertTrue(result.getSections().isEmpty());
  }

  @Test
  void compare_WithoutIdentityAttributes_ShouldNotBeSameScope() {
    var result = comparator.compare(iteration(List.of()), iteration(List.of()));

    assertFalse(result.isSameScope());
  }
}
