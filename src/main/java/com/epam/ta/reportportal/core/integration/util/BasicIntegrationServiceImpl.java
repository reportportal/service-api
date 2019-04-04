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

package com.epam.ta.reportportal.core.integration.util;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class BasicIntegrationServiceImpl implements IntegrationService {

	private final IntegrationRepository integrationRepository;

	@Autowired
	public BasicIntegrationServiceImpl(IntegrationRepository integrationRepository) {
		this.integrationRepository = integrationRepository;
	}

	public Map<String, Object> retrieveIntegrationParams(Map<String, Object> integrationParams) {
		return integrationParams;
	}

	public boolean validateIntegration(Integration integration, ReportPortalUser.ProjectDetails projectDetails) {
		if (projectDetails == null) {
			if (CollectionUtils.isNotEmpty(integrationRepository.findAllGlobalByType(integration.getType()))) {
				throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						"Integration with type " + integration.getType().getName() + " is already exists."
				);
			}
		} else {
			if (CollectionUtils.isNotEmpty(integrationRepository.findAllByProjectIdAndType(projectDetails.getProjectId(),
					integration.getType()
			))) {
				throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						"Integration with type " + integration.getType().getName() + " is already exists."
				);
			}
		}
		return true;
	}

}
