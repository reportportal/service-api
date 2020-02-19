/*
 * Copyright 2020 EPAM Systems
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

import com.epam.ta.reportportal.entity.integration.IntegrationParams;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public enum SauceLabsProperties {
	USERNAME("username"),
	ACCESS_TOKEN("accessToken");

	private String name;

	SauceLabsProperties(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Optional<String> getParameter(Map<String, Object> parameters) {
		return ofNullable(parameters.get(this.name)).map(String::valueOf);
	}

	public void setParameter(IntegrationParams params, String value) {
		if (null == params.getParams()) {
			params.setParams(new HashMap<>());
		}
		params.getParams().put(this.name, value);
	}
}
