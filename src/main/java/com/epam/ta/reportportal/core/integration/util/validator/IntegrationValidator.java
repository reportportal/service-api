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

package com.epam.ta.reportportal.core.integration.util.validator;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.util.Objects;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class IntegrationValidator {

	private IntegrationValidator() {
		//static only
	}

	/**
	 * Validation fails if a project has at least one integration with the same type as the provided global integration has
	 *
	 * @param project     {@link Project}
	 * @param integration {@link Integration} with {@link Integration#project == NULL}
	 */
	public static void validateProjectLevelIntegrationConstraints(Project project, Integration integration) {

		BusinessRule.expect(integration.getProject(), Objects::isNull).verify(
				ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				Suppliers.formattedSupplier("Integration with ID = '{}' is not global.", integration.getId())
		);

		BusinessRule.expect(project.getIntegrations()
				.stream()
				.map(Integration::getType)
				.noneMatch(it -> it.getIntegrationGroup() == integration.getType().getIntegrationGroup()), equalTo(true))
				.verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, Suppliers.formattedSupplier(
						"Global integration with ID = '{}' has been found, but you cannot use it, because you have project-level integration(s) of that type",
						integration.getId()
				).get());
	}
}
