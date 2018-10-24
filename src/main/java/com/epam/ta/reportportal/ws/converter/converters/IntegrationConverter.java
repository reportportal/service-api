package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.ws.model.integration.AuthFlowEnum;
import com.epam.ta.reportportal.ws.model.integration.IntegrationResource;
import com.epam.ta.reportportal.ws.model.integration.IntegrationTypeResource;

import java.util.function.Function;

/**
 * @author Pavel Bortnik
 */
public final class IntegrationConverter {

	private IntegrationConverter() {
		//static only
	}

	public static final Function<Integration, IntegrationResource> TO_INTEGRATION_RESOURCE = integration -> {
		IntegrationResource resource = new IntegrationResource();
		resource.setId(integration.getId());
		resource.setCreationDate(EntityUtils.TO_DATE.apply(integration.getCreationDate()));
		resource.setEnabled(integration.isEnabled());
		resource.setProjectId(integration.getProject().getId());
		resource.setIntegrationParams(integration.getParams().getParams());

		IntegrationTypeResource type = new IntegrationTypeResource();
		type.setId(integration.getType().getId());
		type.setName(integration.getType().getName());
		type.setCreationDate(EntityUtils.TO_DATE.apply(integration.getType().getCreationDate()));
		type.setGroupType(integration.getType().getIntegrationGroup().name());
		type.setDetails(integration.getParams().getParams());
		type.setAuthFlow(AuthFlowEnum.valueOf(integration.getType().getAuthFlow().name()));

		resource.setIntegrationType(type);

		return resource;
	};

}
