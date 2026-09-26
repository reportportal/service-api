package com.epam.reportportal.base.core.aifactory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.core.aifactory.enums.AttributeRollup;
import com.epam.reportportal.base.core.aifactory.enums.AttributeType;
import com.epam.reportportal.base.core.aifactory.enums.AttributeValueType;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineAttributes;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PipelineAttributeSupportTest {

  private static PipelineAttribute attribute(String key, String value) {
    return PipelineAttribute.builder().key(key).value(value).build();
  }

  @Test
  void normalize_ShouldApplyDefaultsAndKeepTheLastAttributePerKey() {
    var reported = new ArrayList<PipelineAttribute>();
    reported.add(PipelineAttribute.builder().key(" env ").value("beta4").type(null).valueType(null).build());
    reported.add(attribute("model", "auto"));
    reported.add(attribute("env", "beta5"));

    var result = PipelineAttributeSupport.normalize(reported);

    assertEquals(2, result.size());
    assertEquals("env", result.get(0).getKey());
    assertEquals("beta5", result.get(0).getValue());
    assertEquals(AttributeType.TAG, result.get(0).getType());
    assertEquals(AttributeValueType.TEXT, result.get(0).getValueType());
  }

  @Test
  void normalize_WithBlankKey_ShouldBeRejected() {
    assertThrows(ReportPortalException.class,
        () -> PipelineAttributeSupport.normalize(List.of(attribute(" ", "x"))));
  }

  @Test
  void normalize_WithTooManyAttributes_ShouldBeRejected() {
    var tooMany = new ArrayList<PipelineAttribute>();
    for (int i = 0; i <= PipelineAttributeSupport.MAX_ATTRIBUTES; i++) {
      tooMany.add(attribute("k" + i, "v"));
    }
    assertThrows(ReportPortalException.class, () -> PipelineAttributeSupport.normalize(tooMany));
  }

  @Test
  void normalize_WithNull_ShouldBeEmpty() {
    assertTrue(PipelineAttributeSupport.normalize(null).isEmpty());
    assertNull(PipelineAttributeSupport.toColumn(List.of()));
  }

  @Test
  void merge_ShouldReplaceByKeyAddNewAndRemoveListedKeys() {
    var existing = new PipelineAttributes(List.of(attribute("env", "beta4"), attribute("ci", "#1"),
        attribute("jira", "A-1")));

    var result = PipelineAttributeSupport.merge(existing, List.of(attribute("env", "beta5"), attribute("model", "m")),
        List.of("jira"));

    assertEquals(List.of("env", "ci", "model"), result.stream().map(PipelineAttribute::getKey).toList());
    assertEquals("beta5", result.get(0).getValue());
  }

  @Test
  void searchText_ShouldJoinLowerCasedValuesAndIgnoreBlanks() {
    var text = PipelineAttributeSupport.searchText(List.of(attribute("requirement", "US-TMS-EXP-002"),
        attribute("trigger", "Web form"), attribute("empty", " "), attribute("none", null)));

    assertEquals("us-tms-exp-002 web form", text);
    assertNull(PipelineAttributeSupport.searchText(List.of()));
  }

  @Test
  void attributeIndex_ShouldHaveOneTokenPerAttributeMatchingTheFilterToken() {
    var index = PipelineAttributeSupport.attributeIndex(List.of(attribute("Env", "Beta5"), attribute("ci", "#1")));

    assertTrue(index.contains(PipelineAttribute.indexToken("env", "beta5")));
    assertTrue(index.contains(PipelineAttribute.indexToken("CI", "#1")));
    assertTrue(!index.contains(PipelineAttribute.indexToken("env", "beta")));
  }

  @Test
  void rollup_ShouldSumFlaggedStageAttributesExceptKeysTheIterationAlreadyHas() {
    var create = stage(PipelineAttribute.builder().key("cost").label("Cost").value("0.5")
        .valueType(AttributeValueType.MONEY).rollup(AttributeRollup.SUM).build(),
        PipelineAttribute.builder().key("tokens").value("100").rollup(AttributeRollup.SUM).build());
    var grade = stage(PipelineAttribute.builder().key("cost").value("0.46")
        .valueType(AttributeValueType.MONEY).rollup(AttributeRollup.SUM).build(),
        PipelineAttribute.builder().key("note").value("x").build());

    var result = PipelineAttributeSupport.rollup(List.of(create, grade), Set.of("tokens"));

    assertEquals(1, result.size());
    assertEquals("cost", result.get(0).getKey());
    assertEquals("Cost", result.get(0).getLabel());
    assertEquals("0.96", result.get(0).getValue());
    assertEquals(AttributeType.MAIN, result.get(0).getType());
    assertEquals(AttributeValueType.MONEY, result.get(0).getValueType());
  }

  @Test
  void rollup_ShouldSkipNonNumericValuesAndStagesWithoutAttributes() {
    var broken = stage(PipelineAttribute.builder().key("cost").value("n/a").rollup(AttributeRollup.SUM).build());

    assertTrue(PipelineAttributeSupport.rollup(List.of(broken, new PipelineStage()), Set.of()).isEmpty());
  }

  private static PipelineStage stage(PipelineAttribute... attributes) {
    var stage = new PipelineStage();
    stage.setAttributes(new PipelineAttributes(List.of(attributes)));
    return stage;
  }
}
