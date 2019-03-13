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

package com.epam.ta.reportportal.core.integration.util.validator;

import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class IntegrationValidatorTest {

	@Test
	void validateNonGlobalIntegration() {
		Integration integration = new Integration();
		integration.setId(1L);
		integration.setProject(new Project());
		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> IntegrationValidator.validateProjectLevelIntegrationConstraints(new Project(), integration)
		);
		assertEquals("Impossible interact with integration. Integration with ID = '1' is not global.", exception.getMessage());
	}

	@Test
	void validateIntegrationGroups() {
		Integration integration = new Integration();
		integration.setId(1L);
		IntegrationType type = new IntegrationType();
		type.setIntegrationGroup(IntegrationGroupEnum.NOTIFICATION);
		integration.setType(type);

		Project project = new Project();
		Integration projectIntegration = new Integration();
		IntegrationType projectIntegrationType = new IntegrationType();
		projectIntegrationType.setIntegrationGroup(IntegrationGroupEnum.NOTIFICATION);
		projectIntegration.setType(projectIntegrationType);
		project.setIntegrations(Sets.newHashSet(projectIntegration));

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> IntegrationValidator.validateProjectLevelIntegrationConstraints(project, integration)
		);
		assertEquals(
				"Impossible interact with integration. Global integration with ID = '1' has been found, but you cannot use it, because you have project-level integration(s) of that type",
				exception.getMessage()
		);
	}

	@Test
	void successfullyValidate() {
		Integration integration = new Integration();
		integration.setId(1L);
		IntegrationType type = new IntegrationType();
		type.setIntegrationGroup(IntegrationGroupEnum.NOTIFICATION);
		integration.setType(type);

		Project project = new Project();
		Integration projectIntegration = new Integration();
		IntegrationType projectIntegrationType = new IntegrationType();
		projectIntegrationType.setIntegrationGroup(IntegrationGroupEnum.BTS);
		projectIntegration.setType(projectIntegrationType);
		project.setIntegrations(Sets.newHashSet(projectIntegration));

		IntegrationValidator.validateProjectLevelIntegrationConstraints(project, integration);
	}
}