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

package com.epam.ta.reportportal.util.email;

import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.project.email.LaunchAttribute;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class EmailRulesValidatorTest {

	@Test
	void validateBlankLaunchName() {
		ReportPortalException exception = assertThrows(ReportPortalException.class, () -> EmailRulesValidator.validateLaunchName(""));
		assertEquals(
				"Error in handled Request. Please, check specified parameters: 'Launch name values cannot be empty. Please specify it or not include in request.'",
				exception.getMessage()
		);
	}

	@Test
	void validateNullLaunchName() {
		ReportPortalException exception = assertThrows(ReportPortalException.class, () -> EmailRulesValidator.validateLaunchName(null));
		assertEquals(
				"Error in handled Request. Please, check specified parameters: 'Launch name values cannot be empty. Please specify it or not include in request.'",
				exception.getMessage()
		);
	}

	@Test
	void validateLaunchNameLength() {
		String largeString = RandomStringUtils.randomAlphabetic(257);
		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> EmailRulesValidator.validateLaunchName(largeString)
		);
		assertEquals("Error in handled Request. Please, check specified parameters: 'One of provided launch names '" + largeString + "' is too long. Acceptable name length is [1..256]'",
				exception.getMessage()
		);
	}

	@Test
	void successfullyValidateLaunchName() {
		EmailRulesValidator.validateLaunchName("launch_name");
	}

	@Test
	void validateEmptyLaunchAttributes() {
		LaunchAttribute attribute = new LaunchAttribute();
		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> EmailRulesValidator.validateLaunchAttribute(attribute)
		);
		assertEquals(
				"Error in handled Request. Please, check specified parameters: 'Attribute' values cannot be empty. Please specify them or do not include in a request.'",
				exception.getMessage()
		);
	}

	@Test
	void validateNullLaunchAttribute() {
		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> EmailRulesValidator.validateLaunchAttribute(null)
		);
		assertEquals("Error in handled Request. Please, check specified parameters: 'Launch attribute cannot be null.'",
				exception.getMessage()
		);
	}

	@Test
	void successfullyValidateLaunchAttribute() {
		LaunchAttribute attribute = new LaunchAttribute();
		attribute.setKey("key");
		attribute.setValue("value");
		EmailRulesValidator.validateLaunchAttribute(attribute);
	}

	@Test
	void validateNullRecipientName() {
		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> EmailRulesValidator.validateRecipient(new Project(), null)
		);
		assertEquals("Error in handled Request. Please, check specified parameters: 'Provided recipient email 'null' is invalid'",
				exception.getMessage()
		);
	}

	@Test
	void validateInvalidRecipientEmail() {
		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> EmailRulesValidator.validateRecipient(new Project(), "invalid@domain")
		);
		assertEquals(
				"Error in handled Request. Please, check specified parameters: 'Provided recipient email 'invalid@domain' is invalid'",
				exception.getMessage()
		);
	}

	@Test
	void successfullyValidateRecipientEmail() {
		EmailRulesValidator.validateRecipient(new Project(), "valid.email@domain.com");
	}

	@Test
	void successfullyValidateOwnerRecipient() {
		EmailRulesValidator.validateRecipient(new Project(), "OWNER");
	}

	@Test
	void validateShortLoginRecipient() {
		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> EmailRulesValidator.validateRecipient(new Project(), "")
		);
		assertEquals("Error in handled Request. Please, check specified parameters: 'Acceptable login length  [1..128]'",
				exception.getMessage()
		);
	}

	@Test
	void validateLongLoginRecipient() {
		String largeLogin = RandomStringUtils.randomAlphabetic(129);
		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> EmailRulesValidator.validateRecipient(new Project(), largeLogin)
		);
		assertEquals("Error in handled Request. Please, check specified parameters: 'Acceptable login length  [1..128]'",
				exception.getMessage()
		);
	}

	@Test
	void validateNotAssignedUserLoginRecipient() {
		Project project = new Project();
		project.setId(1L);
		ProjectUser projectUser = new ProjectUser();
		projectUser.setProject(project);
		User user = new User();
		user.setLogin("exists");
		projectUser.setUser(user);
		project.setUsers(Sets.newHashSet(projectUser));
		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> EmailRulesValidator.validateRecipient(project, "not_exists")
		);
		assertEquals("User 'not_exists' not found. User not found in project 1", exception.getMessage());
	}

	@Test
	void successfullyValidateLoginRecipient() {
		Project project = new Project();
		project.setId(1L);
		ProjectUser projectUser = new ProjectUser();
		projectUser.setProject(project);
		User user = new User();
		user.setLogin("exists");
		projectUser.setUser(user);
		project.setUsers(Sets.newHashSet(projectUser));
		EmailRulesValidator.validateRecipient(project, "exists");
	}
}