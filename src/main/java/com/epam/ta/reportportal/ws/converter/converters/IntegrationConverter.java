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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.ws.model.activity.IntegrationActivityResource;
import com.epam.ta.reportportal.ws.model.integration.AuthFlowEnum;
import com.epam.ta.reportportal.ws.model.integration.IntegrationResource;
import com.epam.ta.reportportal.ws.model.integration.IntegrationTypeResource;

import java.util.function.Function;

import static java.util.Optional.ofNullable;

/**
 * @author Pavel Bortnik
 */
public final class IntegrationConverter {

	public static final Function<Integration, IntegrationResource> TO_INTEGRATION_RESOURCE = integration -> {
		IntegrationResource resource = new IntegrationResource();
		resource.setId(integration.getId());
		resource.setCreationDate(EntityUtils.TO_DATE.apply(integration.getCreationDate()));
		resource.setEnabled(integration.isEnabled());
		ofNullable(integration.getProject()).ifPresent(p -> resource.setProjectId(p.getId()));
		ofNullable(integration.getParams()).ifPresent(it -> resource.setIntegrationParams(it.getParams()));

		IntegrationTypeResource type = new IntegrationTypeResource();
		type.setId(integration.getType().getId());
		type.setName(integration.getType().getName());
		type.setCreationDate(EntityUtils.TO_DATE.apply(integration.getType().getCreationDate()));
		type.setGroupType(integration.getType().getIntegrationGroup().name());
		ofNullable(integration.getType().getDetails()).ifPresent(it -> type.setDetails(it.getDetails()));
		ofNullable(integration.getType().getAuthFlow()).ifPresent(it -> type.setAuthFlow(AuthFlowEnum.valueOf(it.name())));
		resource.setIntegrationType(type);

		return resource;
	};

	public static final Function<Integration, IntegrationActivityResource> TO_ACTIVITY_RESOURCE = integration -> {
		IntegrationActivityResource resource = new IntegrationActivityResource();
		resource.setId(integration.getId());
		ofNullable(integration.getProject()).ifPresent(p -> {
			resource.setProjectId(p.getId());
			resource.setProjectName(p.getName());
		});
		resource.setTypeName(integration.getType().getName());
		return resource;
	};

	private IntegrationConverter() {
		//static only
	}

}
