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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.project.config.pattern.CreatePatternTemplateRQ;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PatternTemplateBuilder implements Supplier<PatternTemplate> {

	private PatternTemplate patternTemplate;

	public PatternTemplateBuilder() {
		patternTemplate = new PatternTemplate();
	}

	public PatternTemplateBuilder withCreateRequest(CreatePatternTemplateRQ createRequest) {
		patternTemplate.setTemplateType(PatternTemplateType.fromString(createRequest.getType()).orElseThrow(() -> new ReportPortalException(
				ErrorType.BAD_REQUEST_ERROR,
				Suppliers.formattedSupplier("Unknown pattern template type - '{}'", createRequest.getType()).get()
		)));
		patternTemplate.setName(StringUtils.trim(createRequest.getName()));
		patternTemplate.setValue(createRequest.getValue());
		patternTemplate.setEnabled(createRequest.getEnabled());
		return this;
	}

	public PatternTemplateBuilder withName(String name) {
		patternTemplate.setName(StringUtils.trim(name));
		return this;
	}

	public PatternTemplateBuilder withValue(String value) {
		patternTemplate.setValue(value);
		return this;
	}

	public PatternTemplateBuilder withType(PatternTemplateType type) {
		patternTemplate.setTemplateType(type);
		return this;
	}

	public PatternTemplateBuilder withEnabled(boolean isEnabled) {
		patternTemplate.setEnabled(isEnabled);
		return this;
	}

	public PatternTemplateBuilder withProjectId(Long projectId) {
		patternTemplate.setProjectId(projectId);
		return this;
	}

	@Override
	public PatternTemplate get() {
		return patternTemplate;
	}
}
