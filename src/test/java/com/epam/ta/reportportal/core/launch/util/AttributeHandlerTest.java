package com.epam.ta.reportportal.core.launch.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.epam.ta.reportportal.core.launch.AttributeHandler;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.RetentionPolicyEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AttributeHandlerTest {

  private AttributeHandler attributeHandler;

  @BeforeEach
  public void setUp() {
    attributeHandler = new AttributeHandler();
  }

  @Test
  public void testHandleAttributesWhenLaunchIsNull() {
    attributeHandler.handleAttributes(null);
    // Since the launch is null, there's no state to assert.
    // The test just ensures no exception is thrown.
  }

  @Test
  public void testHandleAttributesWithImportantAndRegular() {
    Launch launch = new Launch();
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(createAttribute("important"));
    attributes.add(createAttribute("regular"));
    launch.setAttributes(attributes);

    attributeHandler.handleAttributes(launch);

    assertEquals(RetentionPolicyEnum.IMPORTANT, launch.getRetentionPolicy());
    assertEquals(1, launch.getAttributes().size());
  }

  @Test
  public void testHandleAttributesWithImportantOnly() {
    Launch launch = new Launch();
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(createAttribute("important"));
    launch.setAttributes(attributes);

    attributeHandler.handleAttributes(launch);

    assertEquals(RetentionPolicyEnum.IMPORTANT, launch.getRetentionPolicy());
  }

  @Test
  public void testHandleAttributesWithRegularOnly() {
    Launch launch = new Launch();
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(createAttribute("regular"));
    launch.setAttributes(attributes);

    attributeHandler.handleAttributes(launch);

    assertEquals(RetentionPolicyEnum.REGULAR, launch.getRetentionPolicy());
  }

  private ItemAttribute createAttribute(String value) {
    ItemAttribute attribute = new ItemAttribute();
    attribute.setKey("retentionPolicy");
    attribute.setValue(value);
    attribute.setSystem(true);
    return attribute;
  }
}