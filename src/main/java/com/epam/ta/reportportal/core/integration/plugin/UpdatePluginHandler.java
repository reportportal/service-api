/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.integration.plugin;

import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.integration.UpdatePluginStateRQ;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface UpdatePluginHandler {

	/**
	 * Updates plugin state. If 'enabled == true', plugin file will be downloaded from the {@link com.epam.ta.reportportal.filesystem.DataStore}
	 * (if not exists in the plugins' root path) and loaded in the memory.
	 * If 'enabled == false', plugin will be unloaded from the memory
	 *
	 * @param id                  {@link com.epam.ta.reportportal.entity.integration.IntegrationType#id}
	 * @param updatePluginStateRQ {@link UpdatePluginStateRQ}
	 * @return {@link OperationCompletionRS}
	 */
	OperationCompletionRS updatePluginState(Long id, UpdatePluginStateRQ updatePluginStateRQ);
}
