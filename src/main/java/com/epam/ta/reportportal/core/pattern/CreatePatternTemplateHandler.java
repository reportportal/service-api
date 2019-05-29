/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.pattern;

import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.ws.model.project.config.pattern.CreatePatternTemplateRQ;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface CreatePatternTemplateHandler {

	/**
	 * Create {@link com.epam.ta.reportportal.entity.pattern.PatternTemplate} entity for specified {@link com.epam.ta.reportportal.entity.project.Project}
	 *
	 * @param projectId               {@link com.epam.ta.reportportal.entity.pattern.PatternTemplate#projectId}
	 * @param createPatternTemplateRQ {@link CreatePatternTemplateRQ}
	 * @return {@link java.util.regex.Pattern}
	 */
	PatternTemplate createPatternTemplate(Long projectId, CreatePatternTemplateRQ createPatternTemplateRQ);
}
