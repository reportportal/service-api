package com.epam.ta.reportportal.core.integration.impl;

import com.epam.reportportal.extension.CommonPluginCommand;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.ta.reportportal.core.integration.ExecuteIntegrationHandler;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ExecuteIntegrationHandlerTest {
	private static final String PUBLIC_COMMAND_PREFIX = "public_";

	private final IntegrationRepository integrationRepository = mock(IntegrationRepository.class);
	private final PluginBox pluginBox = mock(PluginBox.class);

	private final ExecuteIntegrationHandler executeIntegrationHandler = new ExecuteIntegrationHandlerImpl(integrationRepository, pluginBox);

	@Test
	@DisplayName("Positive Test. Everything is fine")
	public void executePublicCommandPositiveTest() {
		final String pluginName = "signup";
		final String command = "testCommand";
		final String publicCommand = PUBLIC_COMMAND_PREFIX + command;
		final Map<String, Object> params = Collections.emptyMap();

		CommonPluginCommand<String> commonPluginCommand = mock(CommonPluginCommand.class);
		when(commonPluginCommand.executeCommand(params)).thenReturn("Ok");

		ReportPortalExtensionPoint pluginInstance = mock(ReportPortalExtensionPoint.class);
		when(pluginInstance.getCommonCommand(publicCommand)).thenReturn(commonPluginCommand);

		when(pluginBox.getInstance(pluginName, ReportPortalExtensionPoint.class)).thenReturn(Optional.of(pluginInstance));

		executeIntegrationHandler.executePublicCommand(pluginName, command, params);

		verify(pluginBox).getInstance(eq(pluginName), eq(ReportPortalExtensionPoint.class));
		verify(pluginInstance).getCommonCommand(eq(publicCommand));
	}

	@Test
	@DisplayName("Negative Test. When Plugin not found")
	public void executePublicCommandWOPluginTest() {
		final String pluginName = "signup";
		final String command = "testCommand";
		final Map<String, Object> params = Collections.emptyMap();

		CommonPluginCommand<String> commonPluginCommand = mock(CommonPluginCommand.class);
		when(commonPluginCommand.executeCommand(params)).thenReturn("Ok");

		ReportPortalExtensionPoint pluginInstance = mock(ReportPortalExtensionPoint.class);

		when(pluginBox.getInstance(pluginName, ReportPortalExtensionPoint.class)).thenReturn(Optional.empty());

		assertThrows(ReportPortalException.class, () ->
				executeIntegrationHandler.executePublicCommand(pluginName, command, params));

		verify(pluginBox).getInstance(eq(pluginName), eq(ReportPortalExtensionPoint.class));
		verifyNoInteractions(pluginInstance);
	}

	@Test
	@DisplayName("Negative Test. When Command not found")
	public void executePublicCommandWOCommandTest() {
		final String pluginName = "signup";
		final String command = "testCommand";
		final String publicCommand = PUBLIC_COMMAND_PREFIX + command;
		final Map<String, Object> params = Collections.emptyMap();

		CommonPluginCommand<String> commonPluginCommand = mock(CommonPluginCommand.class);
		when(commonPluginCommand.executeCommand(params)).thenReturn("Ok");

		ReportPortalExtensionPoint pluginInstance = mock(ReportPortalExtensionPoint.class);
		when(pluginInstance.getCommonCommand(publicCommand)).thenReturn(null);

		when(pluginBox.getInstance(pluginName, ReportPortalExtensionPoint.class)).thenReturn(Optional.of(pluginInstance));

		assertThrows(ReportPortalException.class, () ->
				executeIntegrationHandler.executePublicCommand(pluginName, command, params));

		verify(pluginBox).getInstance(eq(pluginName), eq(ReportPortalExtensionPoint.class));
		verify(pluginInstance).getCommonCommand(eq(publicCommand));
	}

}
