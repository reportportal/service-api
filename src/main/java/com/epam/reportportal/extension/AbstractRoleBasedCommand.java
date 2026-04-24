package com.epam.reportportal.extension;

import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import java.util.Map;

/**
 * Abstract base for plugin commands that enforce role-based access control before delegating to the concrete
 * implementation.
 *
 * @param <T> the return type of the command
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractRoleBasedCommand<T> implements PluginCommand<T> {

  /**
   * Ensures the current caller may use the provided params for this command.
   *
   * @param params command parameters
   */
  protected abstract void validateRole(Map<String, Object> params);

  /**
   * Runs the command for the given integration and parameters.
   *
   * @param integration configured integration
   * @param params      command parameters
   * @return command result
   */
  protected abstract T invokeCommand(Integration integration, Map<String, Object> params);

  @Override
  public T executeCommand(Integration integration, Map<String, Object> params) {
    validateRole(params);
    return invokeCommand(integration, params);
  }

}
