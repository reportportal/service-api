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

import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface CreatePluginHandler {

	/**
	 * Upload and start up the plugin
	 *
	 * @param pluginFile Plugin file
	 * @return {@link EntryCreatedRS} with the newly created {@link com.epam.ta.reportportal.entity.integration.IntegrationType#id}
	 */
	EntryCreatedRS uploadPlugin(MultipartFile pluginFile);
}
