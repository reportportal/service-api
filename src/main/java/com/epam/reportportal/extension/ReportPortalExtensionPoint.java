package com.epam.reportportal.extension;

import java.util.Map;
import org.pf4j.ExtensionPoint;

/**
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

  /**
   * Returns concrete plugin command
   *
   * @param commandName Command name
   * @return {@link CommonPluginCommand}
   */
  CommonPluginCommand getCommonCommand(String commandName);

  /**
   * Returns concrete plugin command for existed integration
   *
   * @param commandName Command name
   * @return {@link PluginCommand}
   */
  PluginCommand getIntegrationCommand(String commandName);

  default IntegrationGroupEnum getIntegrationGroup() {
    return IntegrationGroupEnum.OTHER;
  }

}
