package com.epam.reportportal.base.core.tms.mapper;

import static com.epam.reportportal.base.reporting.ValidationConstraints.MAX_TEST_ITEM_NAME_LENGTH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NestedStepItemBuilderTest {

  private NestedStepItemBuilder builder;
  private TestItem parentItem;
  private Launch launch;

  @BeforeEach
  void setUp() {
    builder = new NestedStepItemBuilder();

    launch = new Launch();
    launch.setId(10L);

    parentItem = new TestItem();
    parentItem.setItemId(20L);
    parentItem.setTestCaseId("148");
    parentItem.setTestCaseHash(12345);
  }

  @Test
  void buildNestedStepItem_WithNormalName_ShouldSetCorrectProperties() {
    var step = builder.buildNestedStepItem(parentItem, "Step 1: Click button", "Expected result", launch);

    assertNotNull(step);
    assertEquals("Step 1: Click button", step.getName());
    assertEquals("Expected result", step.getDescription());
    assertEquals(TestItemTypeEnum.STEP, step.getType());
    assertEquals(10L, step.getLaunchId());
    assertEquals(20L, step.getParentId());
    assertEquals("148", step.getTestCaseId());
    assertEquals(12345, step.getTestCaseHash());
    assertNotNull(step.getItemResults());
    assertEquals(StatusEnum.INFO, step.getItemResults().getStatus());
  }

  @Test
  void buildNestedStepItem_WithLongName_ShouldTruncateToMaxAllowedLength() {
    var longInstructions = "A".repeat(1500);
    var stepName = builder.buildStepName(longInstructions, 0);

    var step = builder.buildNestedStepItem(parentItem, stepName, "Expected result", launch);

    assertNotNull(step);
    assertEquals(MAX_TEST_ITEM_NAME_LENGTH, step.getName().length());
    assertEquals(stepName.substring(0, MAX_TEST_ITEM_NAME_LENGTH), step.getName());
  }
}
