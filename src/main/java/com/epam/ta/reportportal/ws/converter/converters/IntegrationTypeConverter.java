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

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.ws.model.integration.IntegrationTypeResource;

import java.util.function.Function;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class IntegrationTypeConverter {

	public static final Function<IntegrationType, IntegrationTypeResource> TO_RESOURCE = integrationType -> {
		IntegrationTypeResource resource = new IntegrationTypeResource();
		resource.setId(integrationType.getId());
		resource.setName(integrationType.getName());
		resource.setEnabled(integrationType.isEnabled());
		resource.setCreationDate(EntityUtils.TO_DATE.apply(integrationType.getCreationDate()));
		resource.setGroupType(integrationType.getIntegrationGroup().name());
		ofNullable(integrationType.getDetails()).ifPresent(it -> resource.setDetails(integrationType.getDetails().getDetails()));
		return resource;
	};

	private IntegrationTypeConverter() {
		//static only
	}
}
