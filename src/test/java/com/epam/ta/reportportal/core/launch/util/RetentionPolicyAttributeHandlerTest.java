package com.epam.ta.reportportal.core.launch.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.launch.attribute.impl.RetentionPolicyAttributeHandler;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.RetentionPolicyEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RetentionPolicyAttributeHandlerTest {

  private RetentionPolicyAttributeHandler retentionPolicyAttributeHandler;
  private MessageBus messageBus;

  @BeforeEach
  public void setUp() {
    messageBus = mock(MessageBus.class);
    retentionPolicyAttributeHandler = new RetentionPolicyAttributeHandler(messageBus);
  }

  @Test
  public void testHandleLaunchStartWhenLaunchIsNull() {
    retentionPolicyAttributeHandler.handleLaunchStart(null);
    // Since the launch is null, there's no state to assert.
    // The test just ensures no exception is thrown.
  }

  @Test
  public void testHandleLaunchStartWithBothAttributes() {
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
  public void testHandleLaunchStartWithRegularAttribute() {
    Launch launch = new Launch();
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(createAttribute("regular"));
    launch.setAttributes(attributes);

    retentionPolicyAttributeHandler.handleLaunchStart(launch);

    assertEquals(RetentionPolicyEnum.REGULAR, launch.getRetentionPolicy());
    assertEquals(1, launch.getAttributes().size());
  }

  @Test
  public void testHandleLaunchUpdateWhenLaunchIsNull() {
    ReportPortalUser user = mock(ReportPortalUser.class);
    retentionPolicyAttributeHandler.handleLaunchUpdate(null, user);
    // Since the launch is null, there's no state to assert.
    // The test just ensures no exception is thrown.
  }

  @Test
  public void testHandleLaunchUpdateWithBothAttributes() {
    Launch launch = new Launch();
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(createAttribute("important"));
    attributes.add(createAttribute("regular"));
    launch.setAttributes(attributes);
    ReportPortalUser user = mock(ReportPortalUser.class);

    retentionPolicyAttributeHandler.handleLaunchUpdate(launch, user);

    assertEquals(RetentionPolicyEnum.IMPORTANT, launch.getRetentionPolicy());
    assertEquals(1, launch.getAttributes().size());
  }

  @Test
  public void testHandleLaunchUpdateWithRegularAttribute() {
    Launch launch = new Launch();
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(createAttribute("regular"));
    launch.setAttributes(attributes);
    ReportPortalUser user = mock(ReportPortalUser.class);

    retentionPolicyAttributeHandler.handleLaunchUpdate(launch, user);

    assertEquals(RetentionPolicyEnum.REGULAR, launch.getRetentionPolicy());
    assertEquals(1, launch.getAttributes().size());
  }

  private ItemAttribute createAttribute(String value) {
    ItemAttribute attribute = new ItemAttribute();
    attribute.setKey("retentionPolicy");
    attribute.setValue(value);
    attribute.setSystem(true);
    return attribute;
  }
}