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

package com.epam.ta.reportportal.core.user.impl;

import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class DeleteUserHandlerImplTest {

	@Mock
	private UserRepository repository;

	@InjectMocks
	private DeleteUserHandlerImpl handler;

	@Test
	void deleteNotExistedUser() {
		when(repository.findByLogin("not_exist")).thenReturn(Optional.empty());

		final ReportPortalException exception = assertThrows(
				ReportPortalException.class,
				() -> handler.deleteUser("not_exist", getRpUser("test", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L))
		);
		assertEquals("User 'not_exist' not found.", exception.getMessage());
	}

	@Test
	void deleteOwnAccount() {
		User user = new User();
		user.setLogin("test");
		when(repository.findByLogin("test")).thenReturn(Optional.of(user));

		final ReportPortalException exception = assertThrows(
				ReportPortalException.class,
				() -> handler.deleteUser("test", getRpUser("test", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L))
		);
		assertEquals("Incorrect Request. You cannot delete own account", exception.getMessage());
	}
}