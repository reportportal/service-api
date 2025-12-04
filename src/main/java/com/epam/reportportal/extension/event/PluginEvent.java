/*
 * Copyright 2020 EPAM Systems
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

package com.epam.reportportal.extension.event;

import com.epam.reportportal.core.events.domain.AbstractEvent;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

/**
 * System event for plugin lifecycle notifications (load/unload). This event is consumed by external
 * plugins (e.g., billing plugin) for runtime lifecycle notifications. It is NOT used for activity
 * tracking - use PluginUploadedEvent/PluginDeletedEvent for audit trail.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Getter
public class PluginEvent extends AbstractEvent<Void> {

  private String pluginId;
  private String type;
  private Map<String, Object> params;

  public PluginEvent() {
    this.params = new HashMap<>();
  }

  public PluginEvent(String pluginId, String type) {
    super();
    this.pluginId = pluginId;
    this.type = type;
    this.params = new HashMap<>();
  }

}
