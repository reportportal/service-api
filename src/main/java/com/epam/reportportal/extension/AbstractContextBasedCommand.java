/*
 * Copyright (C) 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.extension;

import com.epam.reportportal.api.model.PluginCommandContext;
import com.epam.reportportal.api.model.PluginCommandRQ;

/**
 * Plugin command that validates the caller's role, then runs {@link #invokeCommand(PluginCommandRQ)}.
 *
 * @param <T> command return type
 */
public abstract class AbstractContextBasedCommand<T> implements CommonPluginCommand<T> {

  /**
   * Ensures the current principal may run this command in the given context.
   *
   * @param commandContext request context (project, user, etc.)
   */
  protected abstract void validateRole(PluginCommandContext commandContext);

  /**
   * Runs the command body after role validation.
   *
   * @param pluginCommandRq full request payload
   * @return command result
   */
  protected abstract T invokeCommand(PluginCommandRQ pluginCommandRq);

  @Override
  public T executeCommand(PluginCommandRQ pluginCommandRq) {
    validateRole(pluginCommandRq.getContext());
    return invokeCommand(pluginCommandRq);
  }

}
