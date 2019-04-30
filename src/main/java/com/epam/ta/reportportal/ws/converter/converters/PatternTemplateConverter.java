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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.ws.model.project.config.pattern.PatternTemplateResource;

import java.util.function.Function;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PatternTemplateConverter {

	private PatternTemplateConverter() {
		//static only
	}

	public static final Function<PatternTemplate, PatternTemplateResource> TO_RESOURCE = patternTemplate -> {
		PatternTemplateResource resource = new PatternTemplateResource();
		resource.setId(patternTemplate.getId());
		resource.setType(patternTemplate.getTemplateType().name());
		resource.setName(patternTemplate.getName());
		resource.setValue(patternTemplate.getValue());
		resource.setEnabled(patternTemplate.isEnabled());

		return resource;
	};
}
