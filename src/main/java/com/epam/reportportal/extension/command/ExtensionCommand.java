package com.epam.reportportal.extension.command;

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.extension.NamedPluginCommand;

public interface ExtensionCommand<T> extends NamedPluginCommand {

  /**
   * Executes plugin command with provided PluginCommandRQ.
   *
   * @param pluginCommandRq Plugin command request object
   * @return Result
   */
  default T executeCommand(PluginCommandRQ pluginCommandRq) {
    return null;
  }


  /**
   * Executes plugin command with provided PluginCommandRQ.
   *
   * @param integration     Configured ReportPortal integration
   * @param pluginCommandRq Plugin command request object
   * @return Result
   */
  default T executeCommand(Integration integration, PluginCommandRQ pluginCommandRq) {
    return null;
  }

}
