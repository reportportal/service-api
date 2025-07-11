package com.epam.ta.reportportal.core.launch.util;

import static com.epam.ta.reportportal.core.settings.handlers.ImportantLaunchSettingHandler.IMPORTANT_SETTINGS_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.MarkLaunchAsImportantEvent;
import com.epam.ta.reportportal.core.events.activity.UnmarkLaunchAsImportantEvent;
import com.epam.ta.reportportal.core.launch.attribute.impl.RetentionPolicyAttributeHandler;
import com.epam.ta.reportportal.core.project.ProjectService;
import com.epam.ta.reportportal.core.settings.ServerSettingsService;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.RetentionPolicyEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RetentionPolicyAttributeHandlerTest {

  RetentionPolicyAttributeHandler retentionPolicyAttributeHandler;
  MessageBus messageBus;
  ServerSettingsService serverSettingsService;
  ProjectService projectService;


  @BeforeEach
  public void setUp() {
    messageBus = mock(MessageBus.class);
    projectService = mock(ProjectService.class);
    serverSettingsService = mock(ServerSettingsService.class);
    retentionPolicyAttributeHandler = new RetentionPolicyAttributeHandler(messageBus, projectService,
        serverSettingsService);
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
    attributes.add(createSystemAttribute("important"));
    attributes.add(createSystemAttribute("regular"));
    launch.setAttributes(attributes);

    retentionPolicyAttributeHandler.handleLaunchStart(launch);

    assertEquals(RetentionPolicyEnum.IMPORTANT, launch.getRetentionPolicy());
    assertEquals(1, launch.getAttributes().size());
  }

  @Test
  public void testHandleLaunchStartWithRegularAttribute() {
    Launch launch = new Launch();
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(createSystemAttribute("regular"));
    launch.setAttributes(attributes);

    retentionPolicyAttributeHandler.handleLaunchStart(launch);

    assertEquals(RetentionPolicyEnum.REGULAR, launch.getRetentionPolicy());
    assertEquals(1, launch.getAttributes().size());
  }

  @Test
  public void testHandleLaunchStartWithImportantAttribute() {
    Launch launch = new Launch();
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(createSystemAttribute("important"));
    launch.setAttributes(attributes);

    retentionPolicyAttributeHandler.handleLaunchStart(launch);

    assertEquals(RetentionPolicyEnum.IMPORTANT, launch.getRetentionPolicy());
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
  public void testHandleLaunchUpdateWithImportantOldAttributeAndRegularNew() {
    Launch launch = new Launch();
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(createSystemAttribute("important"));
    attributes.add(createAttribute("regular"));
    launch.setAttributes(attributes);
    ReportPortalUser user = mock(ReportPortalUser.class);
    when(projectService.findProjectById(any())).thenReturn(new Project());

    retentionPolicyAttributeHandler.handleLaunchUpdate(launch, user);

    assertEquals(RetentionPolicyEnum.REGULAR, launch.getRetentionPolicy());
    assertEquals(1, launch.getAttributes().size());
    assertTrue(launch.getAttributes().iterator().next().isSystem());

    verify(messageBus).publishActivity(any(UnmarkLaunchAsImportantEvent.class));
  }

  @Test
  public void testHandleLaunchUpdateWithRegularOldAttributeAndImportantNew() {
    Launch launch = new Launch();
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(createSystemAttribute("regular"));
    attributes.add(createAttribute("important"));
    launch.setAttributes(attributes);
    ReportPortalUser user = mock(ReportPortalUser.class);

    when(projectService.findProjectById(any())).thenReturn(new Project());
    retentionPolicyAttributeHandler.handleLaunchUpdate(launch, user);

    assertEquals(RetentionPolicyEnum.IMPORTANT, launch.getRetentionPolicy());
    assertEquals(1, launch.getAttributes().size());
    assertTrue(launch.getAttributes().iterator().next().isSystem());

    verify(messageBus).publishActivity(any(MarkLaunchAsImportantEvent.class));
  }

  @Test
  public void testHandleLaunchUpdateWithSameOldAndNewAttributes() {
    Launch launch = new Launch();
    launch.setRetentionPolicy(RetentionPolicyEnum.REGULAR);
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(createSystemAttribute("regular"));
    attributes.add(createAttribute("regular"));
    launch.setAttributes(attributes);
    ReportPortalUser user = mock(ReportPortalUser.class);

    retentionPolicyAttributeHandler.handleLaunchUpdate(launch, user);

    assertEquals(RetentionPolicyEnum.REGULAR, launch.getRetentionPolicy());
    assertEquals(1, launch.getAttributes().size());
    assertTrue(launch.getAttributes().iterator().next().isSystem());

    verify(messageBus, never()).publishActivity(any());
  }

  @Test
  public void testDoNotHandleImportantUpdateIfFeatureDisabled() {
    Launch launch = new Launch();
    launch.setRetentionPolicy(RetentionPolicyEnum.REGULAR);
    Set<ItemAttribute> attributes = new HashSet<>();
    attributes.add(createSystemAttribute("regular"));
    attributes.add(createAttribute("regular"));
    launch.setAttributes(attributes);
    ReportPortalUser user = mock(ReportPortalUser.class);

    when(serverSettingsService.checkServerSettingsState(IMPORTANT_SETTINGS_KEY,
        Boolean.FALSE.toString())).thenReturn(true);

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> retentionPolicyAttributeHandler.handleLaunchUpdate(launch, user)
    );

    assertEquals("Forbidden operation. Feature is disabled",
        exception.getMessage());
  }

  private ItemAttribute createSystemAttribute(String value) {
    ItemAttribute attribute = new ItemAttribute();
    attribute.setKey("retentionPolicy");
    attribute.setValue(value);
    attribute.setSystem(true);
    return attribute;
  }

  private ItemAttribute createAttribute(String value) {
    ItemAttribute attribute = new ItemAttribute();
    attribute.setKey("retentionPolicy");
    attribute.setValue(value);
    attribute.setSystem(false);
    return attribute;
  }
}
