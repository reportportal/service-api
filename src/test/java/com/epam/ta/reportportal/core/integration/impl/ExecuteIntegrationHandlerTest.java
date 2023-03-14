package com.epam.ta.reportportal.core.integration.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.extension.CommonPluginCommand;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.ta.reportportal.core.integration.ExecuteIntegrationHandler;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.exception.ReportPortalException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ExecuteIntegrationHandlerTest {

  private static final String PUBLIC_COMMAND_PREFIX = "public_";

  private final IntegrationRepository integrationRepository = mock(IntegrationRepository.class);
  private final PluginBox pluginBox = mock(PluginBox.class);

  private final ExecuteIntegrationHandler executeIntegrationHandler = new ExecuteIntegrationHandlerImpl(
      integrationRepository, pluginBox);

  @Test
  @DisplayName("Positive Test. Everything is fine")
  public void executePublicCommandPositiveTest() {
    final String pluginName = "signup";
    final String publicCommand = PUBLIC_COMMAND_PREFIX + "testCommand";
    final Map<String, Object> params = Collections.emptyMap();

    CommonPluginCommand<String> commonPluginCommand = mock(CommonPluginCommand.class);
    when(commonPluginCommand.executeCommand(params)).thenReturn("Ok");

    ReportPortalExtensionPoint pluginInstance = mock(ReportPortalExtensionPoint.class);
    when(pluginInstance.getCommonCommand(publicCommand)).thenReturn(commonPluginCommand);

    when(pluginBox.getInstance(pluginName, ReportPortalExtensionPoint.class)).thenReturn(
        Optional.of(pluginInstance));

    executeIntegrationHandler.executePublicCommand(pluginName, publicCommand, params);

    verify(pluginBox).getInstance(eq(pluginName), eq(ReportPortalExtensionPoint.class));
    verify(pluginInstance).getCommonCommand(eq(publicCommand));
  }

  @Test
  @DisplayName("Negative Test. When command is not public")
  public void executeNotPublicCommandTest() {
    final String pluginName = "signup";
    final String publicCommand = "testCommand";
    final Map<String, Object> params = Collections.emptyMap();

    assertThrows(ReportPortalException.class, () ->
        executeIntegrationHandler.executePublicCommand(pluginName, publicCommand, params));

    verifyNoInteractions(pluginBox);
  }

  @Test
  @DisplayName("Negative Test. When Plugin not found")
  public void executePublicCommandWOPluginTest() {
    final String pluginName = "signup";
    final String publicCommand = PUBLIC_COMMAND_PREFIX + "testCommand";
    final Map<String, Object> params = Collections.emptyMap();

    CommonPluginCommand<String> commonPluginCommand = mock(CommonPluginCommand.class);
    when(commonPluginCommand.executeCommand(params)).thenReturn("Ok");

    ReportPortalExtensionPoint pluginInstance = mock(ReportPortalExtensionPoint.class);

    when(pluginBox.getInstance(pluginName, ReportPortalExtensionPoint.class)).thenReturn(
        Optional.empty());

    assertThrows(ReportPortalException.class, () ->
        executeIntegrationHandler.executePublicCommand(pluginName, publicCommand, params));

    verify(pluginBox).getInstance(eq(pluginName), eq(ReportPortalExtensionPoint.class));
    verifyNoInteractions(pluginInstance);
  }

  @Test
  @DisplayName("Negative Test. When Command not found")
  public void executePublicCommandWOCommandTest() {
    final String pluginName = "signup";
    final String publicCommand = PUBLIC_COMMAND_PREFIX + "testCommand";
    final Map<String, Object> params = Collections.emptyMap();

    CommonPluginCommand<String> commonPluginCommand = mock(CommonPluginCommand.class);
    when(commonPluginCommand.executeCommand(params)).thenReturn("Ok");

    ReportPortalExtensionPoint pluginInstance = mock(ReportPortalExtensionPoint.class);
    when(pluginInstance.getCommonCommand(publicCommand)).thenReturn(null);

    when(pluginBox.getInstance(pluginName, ReportPortalExtensionPoint.class)).thenReturn(
        Optional.of(pluginInstance));

    assertThrows(ReportPortalException.class, () ->
        executeIntegrationHandler.executePublicCommand(pluginName, publicCommand, params));

    verify(pluginBox).getInstance(eq(pluginName), eq(ReportPortalExtensionPoint.class));
    verify(pluginInstance).getCommonCommand(eq(publicCommand));
  }

}
