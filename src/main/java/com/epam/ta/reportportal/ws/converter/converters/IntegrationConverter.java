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
import com.epam.ta.reportportal.core.integration.util.property.AuthProperties;
import com.epam.ta.reportportal.core.integration.util.property.BtsProperties;
import com.epam.ta.reportportal.core.integration.util.property.SauceLabsProperties;
import com.epam.ta.reportportal.entity.EmailSettingsEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.ws.model.activity.IntegrationActivityResource;
import com.epam.ta.reportportal.ws.model.integration.AuthFlowEnum;
import com.epam.ta.reportportal.ws.model.integration.IntegrationResource;
import com.epam.ta.reportportal.ws.model.integration.IntegrationTypeResource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Optional.ofNullable;

/**
 * @author Pavel Bortnik
 */
public final class IntegrationConverter {

	private static final List<String> IGNORE_FIELDS = List.of(EmailSettingsEnum.PASSWORD.getAttribute(),
			SauceLabsProperties.ACCESS_TOKEN.getName(),
			BtsProperties.OAUTH_ACCESS_KEY.getName(),
			BtsProperties.API_TOKEN.getName(),
			AuthProperties.MANAGER_PASSWORD.getName()
	);

	private static final Predicate<Map.Entry<String, Object>> IGNORE_FIELDS_CONDITION = entry -> IGNORE_FIELDS.stream()
			.noneMatch(field -> field.equalsIgnoreCase(entry.getKey()));

	public static final Function<Integration, IntegrationResource> TO_INTEGRATION_RESOURCE = integration -> {
		IntegrationResource resource = new IntegrationResource();
		resource.setId(integration.getId());
		resource.setName(integration.getName());
		resource.setCreator(integration.getCreator());
		resource.setCreationDate(EntityUtils.TO_DATE.apply(integration.getCreationDate()));
		resource.setEnabled(integration.isEnabled());
		ofNullable(integration.getProject()).ifPresent(p -> resource.setProjectId(p.getId()));
		ofNullable(integration.getParams()).flatMap(IntegrationConverter::convertToResourceParams)
				.ifPresent(resource::setIntegrationParams);
		IntegrationTypeResource type = new IntegrationTypeResource();
		type.setId(integration.getType().getId());
		type.setName(integration.getType().getName());
		type.setEnabled(integration.getType().isEnabled());
		type.setCreationDate(EntityUtils.TO_DATE.apply(integration.getType().getCreationDate()));
		type.setGroupType(integration.getType().getIntegrationGroup().name());
		ofNullable(integration.getType().getDetails()).ifPresent(it -> type.setDetails(it.getDetails()));
		ofNullable(integration.getType().getAuthFlow()).ifPresent(it -> type.setAuthFlow(AuthFlowEnum.valueOf(it.name())));
		resource.setIntegrationType(type);

		return resource;
	};

	private static Optional<Map<String, Object>> convertToResourceParams(IntegrationParams it) {
		return ofNullable(it.getParams()).map(p -> p.entrySet()
				.stream()
				.filter(IGNORE_FIELDS_CONDITION)
				.collect(HashMap::new, (resourceParams, entry) -> resourceParams.put(entry.getKey(), entry.getValue()), Map::putAll));
	}

	public static final Function<Integration, IntegrationActivityResource> TO_ACTIVITY_RESOURCE = integration -> {
		IntegrationActivityResource resource = new IntegrationActivityResource();
		resource.setId(integration.getId());
		resource.setName(integration.getName());
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
