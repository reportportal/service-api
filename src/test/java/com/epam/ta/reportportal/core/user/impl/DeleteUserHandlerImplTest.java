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

package com.epam.ta.reportportal.core.user.impl;

import com.epam.ta.reportportal.binary.UserBinaryDataService;
import com.epam.ta.reportportal.core.user.content.remover.UserContentRemover;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.DeleteBulkRQ;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class DeleteUserHandlerImplTest {

	@Mock
	private UserRepository repository;

	@Mock
	private UserBinaryDataService dataStore;

	@Mock
	private UserContentRemover userContentRemover;

	@Mock
	private ProjectRepository projectRepository;

	@InjectMocks
	private DeleteUserHandlerImpl handler;

	@Test
	void deleteUser() {
		User user = new User();
		user.setId(2L);
		user.setLogin("test");

		doReturn(Optional.of(user)).when(repository).findById(2L);
		when(projectRepository.findUserProjects(anyString())).thenReturn(Lists.newArrayList());
		doNothing().when(dataStore).deleteUserPhoto(any());

		handler.deleteUser(2L, getRpUser("test", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L));

		verify(repository, times(1)).findById(2L);
		verify(dataStore, times(1)).deleteUserPhoto(any());

	}

	@Test
	void deleteNotExistedUser() {
		when(repository.findById(12345L)).thenReturn(Optional.empty());

		final ReportPortalException exception = assertThrows(
				ReportPortalException.class,
				() -> handler.deleteUser(12345L, getRpUser("test", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L))
		);
		assertEquals("User '12345' not found.", exception.getMessage());
	}

	@Test
	void deleteOwnAccount() {
		User user = new User();
		user.setId(1L);

		doReturn(Optional.of(user)).when(repository).findById(1L);

		final ReportPortalException exception = assertThrows(
				ReportPortalException.class,
				() -> handler.deleteUser(1L, getRpUser("test", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L))
		);
		assertEquals("Incorrect Request. You cannot delete own account", exception.getMessage());

		verify(repository, times(1)).findById(1L);
		verify(repository, times(0)).delete(any(User.class));
	}

	@Test
	void bulkDelete() {
		User user = new User();
		user.setId(2L);

		User user2 = new User();
		user.setId(3L);

		doNothing().when(dataStore).deleteUserPhoto(any());

		doReturn(Optional.of(user)).when(repository).findById(2L);
		doReturn(Optional.of(user2)).when(repository).findById(3L);

		DeleteBulkRQ deleteBulkRQ = new DeleteBulkRQ();
		deleteBulkRQ.setIds(Lists.newArrayList(2L, 3L));

		handler.deleteUsers(deleteBulkRQ, getRpUser("test", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L));

		verify(repository, times(2)).findById(any(Long.class));
		verify(repository, times(2)).delete(any(User.class));
		verify(userContentRemover, times(2)).removeContent(any(User.class));
		verify(dataStore, times(2)).deleteUserPhoto(any(User.class));
	}
}