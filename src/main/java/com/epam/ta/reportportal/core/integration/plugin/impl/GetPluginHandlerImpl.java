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

package com.epam.ta.reportportal.core.integration.plugin.impl;

import com.epam.ta.reportportal.core.integration.plugin.GetPluginHandler;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.ws.converter.converters.IntegrationTypeConverter;
import com.epam.ta.reportportal.ws.model.integration.IntegrationTypeResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class GetPluginHandlerImpl implements GetPluginHandler {

	private final IntegrationTypeRepository integrationTypeRepository;

	@Autowired
	public GetPluginHandlerImpl(IntegrationTypeRepository integrationTypeRepository) {
		this.integrationTypeRepository = integrationTypeRepository;
	}

	@Override
	public List<IntegrationTypeResource> getPlugins() {
		return integrationTypeRepository.findAllByOrderByCreationDate()
				.stream()
				.map(IntegrationTypeConverter.TO_RESOURCE)
				.collect(Collectors.toList());
	}
}
