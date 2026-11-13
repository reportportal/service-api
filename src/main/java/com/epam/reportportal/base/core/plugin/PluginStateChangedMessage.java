/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.core.plugin;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Broadcast on the {@code broadcast.events} fanout exchange whenever a plugin is installed,
 * enabled, disabled, replaced or removed, so that every service-api instance - not just the one
 * that handled the request - can reconcile its local plugin state immediately instead of waiting
 * for its next {@link com.epam.reportportal.base.job.LoadPluginsJob} /
 * {@link com.epam.reportportal.base.job.CleanOutdatedPluginsJob} poll.
 *
 * <p>Carries only the plugin id: the receiving instance re-reads the current state from the
 * database rather than trusting a snapshot in the message, so delivery order and duplicate
 * delivery don't matter.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PluginStateChangedMessage implements Serializable {

  private String pluginId;

}
