package com.epam.reportportal.base.core.launch.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.epam.reportportal.base.core.launch.attribute.impl.LaunchTypeAttributeHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
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
  void handleLaunchStartWithSystemIsPipelineTrue() {
    Launch launch = new Launch();
    ItemAttribute attr = new ItemAttribute("isPipeline", "true", true);
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(attr);
    launch.setAttributes(attributes);

    handler.handleLaunchStart(launch);

    assertEquals(LaunchTypeEnum.PIPELINE, launch.getLaunchType());
    assertTrue(launch.getAttributes().contains(attr));
  }

  @Test
  void handleLaunchStartWithSystemIsPipelineFalse() {
    Launch launch = new Launch();
    launch.setLaunchType(LaunchTypeEnum.PIPELINE);
    ItemAttribute attr = new ItemAttribute("isPipeline", "false", true);
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(attr);
    launch.setAttributes(attributes);

    handler.handleLaunchStart(launch);

    assertEquals(LaunchTypeEnum.AUTOMATION, launch.getLaunchType());
    assertTrue(launch.getAttributes().contains(attr));
  }

  @Test
  void handleLaunchStartIgnoresNonSystemIsPipeline() {
    Launch launch = new Launch();
    launch.setLaunchType(LaunchTypeEnum.AUTOMATION);
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(new ItemAttribute("isPipeline", "true", false));
    launch.setAttributes(attributes);

    handler.handleLaunchStart(launch);

    assertEquals(LaunchTypeEnum.AUTOMATION, launch.getLaunchType());
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
  void handleLaunchStartWithConflictingIsAgenticAndIsPipelineThrows() {
    Launch launch = new Launch();
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(new ItemAttribute("isAgentic", "true", true));
    attributes.add(new ItemAttribute("isPipeline", "true", true));
    launch.setAttributes(attributes);

    var exception = org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> handler.handleLaunchStart(launch)
    );
    
    assertTrue(exception.getMessage().contains("isAgentic"));
    assertTrue(exception.getMessage().contains("isPipeline"));
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
