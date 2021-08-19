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

package com.epam.ta.reportportal.core.item.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.util.TestProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@ExtendWith(MockitoExtension.class)
class LaunchAccessValidatorImplTest {

	@Mock
	private LaunchRepository launchRepository;

	@InjectMocks
	private LaunchAccessValidatorImpl launchAccessValidator;

	@Test
	void validateNotExistingLaunch() {

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		Launch launch = new Launch();
		launch.setId(1L);
		when(launchRepository.findById(1L)).thenReturn(Optional.empty());

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> launchAccessValidator.validate(1L, extractProjectDetails(rpUser, "test_project"), rpUser)
		);
		assertEquals("Launch '1' not found. Did you use correct Launch ID?", exception.getMessage());
	}

	@Test
	void validateLaunchUnderAnotherProject() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		TestItem item = new TestItem();
		Launch launch = new Launch();
		launch.setId(1L);
		launch.setProjectId(2L);
		item.setLaunchId(launch.getId());
		when(launchRepository.findById(1L)).thenReturn(Optional.of(launch));

		final Executable executable = () -> launchAccessValidator.validate(1L, extractProjectDetails(rpUser, "test_project"), rpUser);

		final ReportPortalException exception = assertThrows(ReportPortalException.class, executable);
		assertEquals("Forbidden operation. Specified launch with id '1' not referenced to specified project with id '1'",
				exception.getMessage()
		);
	}

	@Test
	void validateLaunchWithOperatorRole() {
		ReportPortalUser operator = getRpUser("operator", UserRole.USER, ProjectRole.OPERATOR, 1L);

		Launch launch = new Launch();
		launch.setId(1L);
		launch.setMode(LaunchModeEnum.DEBUG);
		launch.setProjectId(1L);

		when(launchRepository.findById(1L)).thenReturn(Optional.of(launch));

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> launchAccessValidator.validate(1L, extractProjectDetails(operator, "test_project"), operator)
		);
		assertEquals("You do not have enough permissions.", exception.getMessage());
	}
}