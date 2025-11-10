package com.epam.reportportal.extension;

import com.epam.reportportal.api.model.PluginCommandRQ;
import java.util.Map;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public interface CommonPluginCommand<T> extends NamedPluginCommand {

  /**
   * Executes plugin command without existed integration
   *
   * @param params Plugin Command parameters
   * @return Result
   */
  default T executeCommand(Map<String, Object> params) {
    return null;
  }

  /**
   * Executes plugin command with provided PluginCommandRQ.
   *
   * @param pluginCommandRq Plugin command request object
   * @return Result
   */
  default T executeCommand(PluginCommandRQ pluginCommandRq) {
    return null;
  }
}
