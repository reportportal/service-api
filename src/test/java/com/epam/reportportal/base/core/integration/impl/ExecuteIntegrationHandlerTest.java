package com.epam.reportportal.base.core.integration.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.api.model.PluginCommandContext;
import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.core.integration.ExecuteIntegrationHandler;
import com.epam.reportportal.base.core.plugin.PluginBox;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.builtin.BuiltinCommandResolver;
import com.epam.reportportal.extension.command.ExtensionCommand;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ExecuteIntegrationHandlerTest {

  private static final String PUBLIC_COMMAND_PREFIX = "public_";

  private final IntegrationRepository integrationRepository = mock(IntegrationRepository.class);
  private final IntegrationTypeRepository integrationTypeRepository = mock(IntegrationTypeRepository.class);
  private final BuiltinCommandResolver builtinCommandResolver = mock(BuiltinCommandResolver.class);
  private final PluginBox pluginBox = mock(PluginBox.class);

  private final ExecuteIntegrationHandler executeIntegrationHandler = new ExecuteIntegrationHandlerImpl(
      integrationRepository, integrationTypeRepository, builtinCommandResolver, pluginBox);

  private final static PluginCommandRQ EMPTY_RQ = new PluginCommandRQ()
      .context(new PluginCommandContext());

  @Test
  @DisplayName("Positive Test. Everything is fine")
  public void executePublicCommandPositiveTest() {
    final String pluginName = "signup";
    final String publicCommand = PUBLIC_COMMAND_PREFIX + "testCommand";

    ExtensionCommand commonPluginCommand = mock(ExtensionCommand.class);
    when(commonPluginCommand.executeCommand(EMPTY_RQ))
        .thenReturn("Ok");

    ReportPortalExtensionPoint pluginInstance = mock(ReportPortalExtensionPoint.class);
    when(pluginInstance.getCommonExtensionCommand(publicCommand)).thenReturn(commonPluginCommand);

    when(pluginBox.getInstance(pluginName, ReportPortalExtensionPoint.class)).thenReturn(
        Optional.of(pluginInstance));

    executeIntegrationHandler.executeExtensionCommand(pluginName, publicCommand, EMPTY_RQ);

    verify(pluginBox).getInstance(eq(pluginName), eq(ReportPortalExtensionPoint.class));
    verify(pluginInstance).getCommonExtensionCommand(eq(publicCommand));
  }

  @Test
  @DisplayName("Negative Test. When command is not public")
  public void executeNotPublicCommandTest() {
    final String pluginName = "signup";
    final String publicCommand = "testCommand";

    assertThrows(ReportPortalException.class, () ->
        executeIntegrationHandler.executeExtensionCommand(pluginName, publicCommand, EMPTY_RQ));

    verifyNoInteractions(pluginBox);
  }

  @Test
  @DisplayName("Negative Test. When Plugin not found")
  public void executePublicCommandWOPluginTest() {
    final String pluginName = "signup";
    final String publicCommand = PUBLIC_COMMAND_PREFIX + "testCommand";

    ExtensionCommand<String> commonPluginCommand = mock(ExtensionCommand.class);
    when(commonPluginCommand.executeCommand(EMPTY_RQ)).thenReturn("Ok");

    ReportPortalExtensionPoint pluginInstance = mock(ReportPortalExtensionPoint.class);

    when(pluginBox.getInstance(pluginName, ReportPortalExtensionPoint.class)).thenReturn(
        Optional.empty());

    assertThrows(ReportPortalException.class, () ->
        executeIntegrationHandler.executeExtensionCommand(pluginName, publicCommand, EMPTY_RQ));

    verify(pluginBox).getInstance(eq(pluginName), eq(ReportPortalExtensionPoint.class));
    verifyNoInteractions(pluginInstance);
  }

  @Test
  @DisplayName("Negative Test. When Command not found")
  public void executePublicCommandWOCommandTest() {
    final String pluginName = "signup";
    final String publicCommand = PUBLIC_COMMAND_PREFIX + "testCommand";

    ExtensionCommand<String> commonPluginCommand = mock(ExtensionCommand.class);
    when(commonPluginCommand.executeCommand(EMPTY_RQ)).thenReturn("Ok");

    ReportPortalExtensionPoint pluginInstance = mock(ReportPortalExtensionPoint.class);
    when(pluginInstance.getCommonExtensionCommand(publicCommand)).thenReturn(null);

    when(pluginBox.getInstance(pluginName, ReportPortalExtensionPoint.class)).thenReturn(
        Optional.of(pluginInstance));

    assertThrows(ReportPortalException.class, () ->
        executeIntegrationHandler.executeExtensionCommand(pluginName, publicCommand, EMPTY_RQ));

    verify(pluginBox).getInstance(eq(pluginName), eq(ReportPortalExtensionPoint.class));
    verify(pluginInstance).getCommonExtensionCommand(eq(publicCommand));
  }

}
