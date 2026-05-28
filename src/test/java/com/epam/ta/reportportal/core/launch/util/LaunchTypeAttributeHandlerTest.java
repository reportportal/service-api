package com.epam.ta.reportportal.core.launch.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.launch.attribute.impl.LaunchTypeAttributeHandler;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.LaunchTypeEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LaunchTypeAttributeHandlerTest {

  private LaunchTypeAttributeHandler handler;

  @BeforeEach
  void setUp() {
    handler = new LaunchTypeAttributeHandler();
  }

  @Test
  void handleLaunchStartWhenLaunchIsNull() {
    handler.handleLaunchStart(null);
  }

  @Test
  void handleLaunchStartWhenNoIsAgenticAttributeKeepsLaunchType() {
    Launch launch = new Launch();
    launch.setLaunchType(LaunchTypeEnum.AUTOMATION);
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(new ItemAttribute("other", "v", true));
    launch.setAttributes(attributes);

    handler.handleLaunchStart(launch);

    assertEquals(LaunchTypeEnum.AUTOMATION, launch.getLaunchType());
    assertEquals(1, launch.getAttributes().size());
  }

  @Test
  void handleLaunchStartWithSystemIsAgenticTrue() {
    Launch launch = new Launch();
    ItemAttribute attr = new ItemAttribute("isAgentic", "true", true);
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(attr);
    launch.setAttributes(attributes);

    handler.handleLaunchStart(launch);

    assertEquals(LaunchTypeEnum.AGENTIC, launch.getLaunchType());
    assertTrue(launch.getAttributes().contains(attr));
  }

  @Test
  void handleLaunchStartWithSystemIsAgenticFalse() {
    Launch launch = new Launch();
    launch.setLaunchType(LaunchTypeEnum.AGENTIC);
    ItemAttribute attr = new ItemAttribute("isAgentic", "false", true);
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(attr);
    launch.setAttributes(attributes);

    handler.handleLaunchStart(launch);

    assertEquals(LaunchTypeEnum.AUTOMATION, launch.getLaunchType());
    assertTrue(launch.getAttributes().contains(attr));
  }

  @Test
  void handleLaunchStartIgnoresNonSystemIsAgentic() {
    Launch launch = new Launch();
    launch.setLaunchType(LaunchTypeEnum.AUTOMATION);
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(new ItemAttribute("isAgentic", "true", false));
    launch.setAttributes(attributes);

    handler.handleLaunchStart(launch);

    assertEquals(LaunchTypeEnum.AUTOMATION, launch.getLaunchType());
  }

  @Test
  void handleLaunchUpdateDoesNotChangeLaunchType() {
    Launch launch = new Launch();
    launch.setLaunchType(LaunchTypeEnum.AUTOMATION);
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(new ItemAttribute("isAgentic", "true", true));
    launch.setAttributes(attributes);
    ReportPortalUser user = mock(ReportPortalUser.class);

    handler.handleLaunchUpdate(launch, user);

    assertEquals(LaunchTypeEnum.AUTOMATION, launch.getLaunchType());
  }
}
