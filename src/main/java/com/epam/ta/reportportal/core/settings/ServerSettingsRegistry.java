/*
 * Copyright 2025 EPAM Systems
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
package com.epam.ta.reportportal.core.settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Registry that maps server setting keys to their respective handlers. Automatically populated via
 * Spring's component scanning.
 * <p>
 * Supports dynamic and extensible handler registration using Spring DI.
 * <p>
 * Add new handlers by implementing {@link ServerSettingHandler} and annotating with {@code
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 * @Component}.
 */
@Component
public class ServerSettingsRegistry {

  private final Map<String, ServerSettingHandler> handlers = new HashMap<>();

  public ServerSettingsRegistry(List<ServerSettingHandler> handlerList) {
    for (ServerSettingHandler handler : handlerList) {
      handlers.put(handler.getKey(), handler);
    }
  }

  /**
   * Returns the handler for a given key if one exists.
   *
   * @param key the server setting key
   * @return Optional containing the handler or empty if not found
   */
  public Optional<ServerSettingHandler> getHandler(String key) {
    return Optional.ofNullable(handlers.get(key));
  }

}
