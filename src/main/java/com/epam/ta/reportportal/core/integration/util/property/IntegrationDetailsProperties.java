/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.integration.util.property;

import com.epam.ta.reportportal.entity.integration.IntegrationTypeDetails;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public enum IntegrationDetailsProperties {

	FILE_ID("id"),
	COMMANDS("allowedCommands"),
	VERSION("version"),
	FILE_NAME("name");

	private String attribute;

	IntegrationDetailsProperties(String attribute) {
		this.attribute = attribute;
	}

	public Optional<Object> getValue(Map<String, Object> details) {
		return ofNullable(details.get(this.attribute));
	}

	public void setValue(@NotNull IntegrationTypeDetails integrationTypeDetails, Object value) {
		Map<String, Object> details = ofNullable(integrationTypeDetails.getDetails()).orElseGet(HashMap::new);
		details.put(this.attribute, value);
		integrationTypeDetails.setDetails(details);
	}

	public String getAttribute() {
		return attribute;
	}
}
