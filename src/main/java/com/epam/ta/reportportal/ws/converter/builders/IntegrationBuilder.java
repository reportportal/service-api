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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;

import java.time.LocalDateTime;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class IntegrationBuilder implements Supplier<Integration> {

	private Integration integration;

	public IntegrationBuilder() {
		integration = new Integration();
	}

	public IntegrationBuilder(Integration integration) {
		this.integration = integration;
	}

	public IntegrationBuilder withCreationDate(LocalDateTime date) {
		this.integration.setCreationDate(date);
		return this;
	}

	public IntegrationBuilder withCreator(String creator) {
		this.integration.setCreator(creator);
		return this;
	}

	public IntegrationBuilder withEnabled(boolean enabled) {
		this.integration.setEnabled(enabled);
		return this;
	}

	public IntegrationBuilder withName(String name) {
		this.integration.setName(name);
		return this;
	}

	public IntegrationBuilder withType(IntegrationType type) {
		this.integration.setType(type);
		return this;
	}

	public IntegrationBuilder withProject(Project project) {
		this.integration.setProject(project);
		return this;
	}

	public IntegrationBuilder withParams(IntegrationParams params) {
		this.integration.setParams(params);
		return this;
	}

	@Override
	public Integration get() {
		return integration;
	}
}
