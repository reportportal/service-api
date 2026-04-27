package com.epam.reportportal.extension;

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import java.util.Map;

/**
 * Plugin command that is executed against an existing
 * {@link com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration}.
 *
 * @param <T> the return type of the command
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public interface PluginCommand<T> extends NamedPluginCommand {

  /**
   * Executes plugin command for existed integration
   *
   * @param integration Configured ReportPortal integration
   * @param params      Plugin Command parameters
   * @return Result
   */
  default T executeCommand(Integration integration, Map<String, Object> params) {
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
