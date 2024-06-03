package com.epam.ta.reportportal.core.launch.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.launch.attribute.LaunchAttributeHandlerService;
import com.epam.ta.reportportal.core.launch.attribute.impl.RetentionPolicyAttributeHandler;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.RetentionPolicyEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import java.util.HashSet;
import java.util.Set;
import javax.mail.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

public class RetentionPolicyAttributeHandlerTest {

  private RetentionPolicyAttributeHandler retentionPolicyAttributeHandler;
  private MessageBus messageBus;

  @BeforeEach
  public void setUp() {
    retentionPolicyAttributeHandler = new RetentionPolicyAttributeHandler(messageBus);
  }

  @Test
  public void testHandleAttributesWhenLaunchIsNull() {
    retentionPolicyAttributeHandler.handleLaunchStart(null);
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

    retentionPolicyAttributeHandler.handleLaunchStart(launch);

    assertEquals(RetentionPolicyEnum.IMPORTANT, launch.getRetentionPolicy());
    assertEquals(1, launch.getAttributes().size());
  }

  @Test
  public void testHandleAttributesWithImportantOnly() {
    Launch launch = new Launch();
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(createAttribute("important"));
    launch.setAttributes(attributes);

    retentionPolicyAttributeHandler.handleLaunchStart(launch);

    assertEquals(RetentionPolicyEnum.IMPORTANT, launch.getRetentionPolicy());
  }

  @Test
  public void testHandleAttributesWithRegularOnly() {
    Launch launch = new Launch();
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(createAttribute("regular"));
    launch.setAttributes(attributes);

    retentionPolicyAttributeHandler.handleLaunchStart(launch);

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