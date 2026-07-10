package com.epam.reportportal.extension;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationAuthFlowEnum;
import com.epam.reportportal.extension.command.ExtensionCommand;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.pf4j.ExtensionPoint;

/**
 * PF4J extension point that every ReportPortal plugin must implement to expose its commands and parameters.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public interface ReportPortalExtensionPoint extends ExtensionPoint {

  /**
   * Should be provided in the {@link #getPluginParams()} method as a key parameter key. Value is supported commands by
   * plugin
   */
  String ALLOWED_COMMANDS = "allowedCommands";

  /**
   * Should be provided in the {@link #getPluginParams()} method as a key parameter key. Value is supported commands by
   * plugin
   */
  String COMMON_COMMANDS = "commonCommands";

  /**
   * Return available plugin parameters
   *
   * @return Map of plugin params
   */
  Map<String, ?> getPluginParams();

  default IntegrationGroupEnum getIntegrationGroup() {
    return IntegrationGroupEnum.OTHER;
  }

  default Optional<IntegrationAuthFlowEnum> getAuthFlow() {
    return Optional.empty();
  }

  default ExtensionCommand<?> getCommonExtensionCommand(String commandName) {
    return getCommonExtensionCommands().get(commandName);
  }

  default ExtensionCommand<?> getIntegrationExtensionCommand(String commandName) {
    return getIntegrationExtensionCommands().get(commandName);
  }

  default Map<String, ExtensionCommand<?>> getCommonExtensionCommands() {
    return new HashMap<>();
  }

  default Map<String, ExtensionCommand<?>> getIntegrationExtensionCommands() {
    return new HashMap<>();
  }

}
