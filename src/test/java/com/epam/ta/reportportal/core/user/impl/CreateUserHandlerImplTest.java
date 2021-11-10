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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.project.GetProjectHandler;
import com.epam.ta.reportportal.dao.UserCreationBidRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserCreationBid;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQ;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQConfirm;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQFull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class CreateUserHandlerImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private GetProjectHandler getProjectHandler;

	@Mock
	private UserCreationBidRepository userCreationBidRepository;

	@InjectMocks
	private CreateUserHandlerImpl handler;

	@Test
	void createByNotExistedAdmin() {

		final ReportPortalUser rpUser = getRpUser("admin", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);
		when(userRepository.findRawById(rpUser.getUserId())).thenReturn(Optional.empty());

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.createUserByAdmin(new CreateUserRQFull(), rpUser, "url")
		);
		assertEquals("User 'admin' not found.", exception.getMessage());
	}

	@Test
	void createByNotAdmin() {
		final ReportPortalUser rpUser = getRpUser("admin", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);
		User user = new User();
		user.setRole(UserRole.USER);
		when(userRepository.findRawById(1L)).thenReturn(Optional.of(user));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.createUserByAdmin(new CreateUserRQFull(), rpUser, "url")
		);
		assertEquals("You do not have enough permissions. Only administrator can create new user. Your role is - USER",
				exception.getMessage()
		);
	}

	@Test
	void createByAdminUserAlreadyExists() {
		final ReportPortalUser rpUser = getRpUser("admin", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);
		User creator = new User();
		creator.setRole(UserRole.ADMINISTRATOR);

		doReturn(Optional.of(creator)).when(userRepository).findRawById(rpUser.getUserId());
		doReturn(Optional.of(new User())).when(userRepository).findByLogin("new_user");

		final CreateUserRQFull request = new CreateUserRQFull();
		request.setLogin("new_user");
		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.createUserByAdmin(request, rpUser, "url")
		);
		assertEquals("User with 'login='new_user'' already exists. You couldn't create the duplicate.", exception.getMessage());
	}

	@Test
	void createByAdminWithIncorrectName() {
		final ReportPortalUser rpUser = getRpUser("admin", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);
		User creator = new User();
		creator.setRole(UserRole.ADMINISTRATOR);
		doReturn(Optional.of(creator)).when(userRepository).findRawById(rpUser.getUserId());
		doReturn(Optional.empty()).when(userRepository).findByLogin("#$$/");

		final CreateUserRQFull request = new CreateUserRQFull();
		request.setLogin("#$$/");
		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.createUserByAdmin(request, rpUser, "url")
		);
		assertEquals("Incorrect Request. Username '#$$/' consists only of special characters", exception.getMessage());
	}

	@Test
	void createByAdminWithIncorrectEmail() {
		final ReportPortalUser rpUser = getRpUser("admin", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);
		User creator = new User();
		creator.setRole(UserRole.ADMINISTRATOR);
		doReturn(Optional.of(creator)).when(userRepository).findRawById(rpUser.getUserId());
		doReturn(Optional.empty()).when(userRepository).findByLogin("new_user");

		final CreateUserRQFull request = new CreateUserRQFull();
		request.setLogin("new_user");
		request.setEmail("incorrect@email");
		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.createUserByAdmin(request, rpUser, "url")
		);
		assertEquals("Error in handled Request. Please, check specified parameters: 'email='incorrect@email''", exception.getMessage());
	}

	@Test
	void createByAdminWithExistedEmail() {
		final ReportPortalUser rpUser = getRpUser("admin", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);
		User creator = new User();
		creator.setRole(UserRole.ADMINISTRATOR);
		doReturn(Optional.of(creator)).when(userRepository).findRawById(rpUser.getUserId());
		doReturn(Optional.empty()).when(userRepository).findByLogin("new_user");
		when(userRepository.findByEmail("correct@domain.com")).thenReturn(Optional.of(new User()));

		final CreateUserRQFull request = new CreateUserRQFull();
		request.setLogin("new_user");
		request.setEmail("correct@domain.com");
		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.createUserByAdmin(request, rpUser, "url")
		);
		assertEquals("User with 'email='correct@domain.com'' already exists. You couldn't create the duplicate.", exception.getMessage());
	}

	@Test
	void createByAdminWithExistedEmailUppercase() {
		final ReportPortalUser rpUser = getRpUser("admin", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);
		User creator = new User();
		creator.setRole(UserRole.ADMINISTRATOR);
		doReturn(Optional.of(creator)).when(userRepository).findRawById(rpUser.getUserId());
		doReturn(Optional.empty()).when(userRepository).findByLogin("new_user");
		when(userRepository.findByEmail("correct@domain.com")).thenReturn(Optional.of(new User()));

		final CreateUserRQFull request = new CreateUserRQFull();
		request.setLogin("new_user");
		request.setEmail("CORRECT@domain.com");
		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.createUserByAdmin(request, rpUser, "url")
		);
		assertEquals("User with 'email='CORRECT@domain.com'' already exists. You couldn't create the duplicate.", exception.getMessage());
	}

	@Test
	void CreateUserBidOnNotExistedProject() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		when(getProjectHandler.getProject("not_exists")).thenThrow(new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, "not_exists"));

		CreateUserRQ request = new CreateUserRQ();
		request.setDefaultProject("not_exists");
		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.createUserBid(request, rpUser, "emailUrl")
		);
		assertEquals("Project 'not_exists' not found. Did you use correct project name?", exception.getMessage());
	}

	@Test
	void createUserWithoutBid() {
		when(userCreationBidRepository.findById("uuid")).thenReturn(Optional.empty());

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.createUser(new CreateUserRQConfirm(), "uuid")
		);
		assertEquals("Incorrect Request. Impossible to register user. UUID expired or already registered.", exception.getMessage());
	}

	@Test
	void createAlreadyExistedUser() {
		final UserCreationBid creationBid = new UserCreationBid();
		creationBid.setDefaultProject(new Project());
		when(userCreationBidRepository.findById("uuid")).thenReturn(Optional.of(creationBid));
		when(userRepository.findByLogin("test")).thenReturn(Optional.of(new User()));

		final CreateUserRQConfirm request = new CreateUserRQConfirm();
		request.setLogin("test");
		final ReportPortalException exception = assertThrows(ReportPortalException.class, () -> handler.createUser(request, "uuid"));
		assertEquals("User with 'login='test'' already exists. You couldn't create the duplicate.", exception.getMessage());
	}

	@Test
	public void createUserWithIncorrectLogin() {
		final UserCreationBid creationBid = new UserCreationBid();
		creationBid.setDefaultProject(new Project());
		when(userCreationBidRepository.findById("uuid")).thenReturn(Optional.of(creationBid));
		when(userRepository.findByLogin("##$%/")).thenReturn(Optional.empty());

		final CreateUserRQConfirm request = new CreateUserRQConfirm();
		request.setLogin("##$%/");
		final ReportPortalException exception = assertThrows(ReportPortalException.class, () -> handler.createUser(request, "uuid"));
		assertEquals("Incorrect Request. Username '##$%/' consists only of special characters", exception.getMessage());
	}

	@Test
	void createUserWithIncorrectEmail() {
		final UserCreationBid bid = new UserCreationBid();
		Project project = new Project();
		project.setName("test_project");
		bid.setDefaultProject(project);
		when(userCreationBidRepository.findById("uuid")).thenReturn(Optional.of(bid));
		when(userRepository.findByLogin("test")).thenReturn(Optional.empty());

		final CreateUserRQConfirm request = new CreateUserRQConfirm();
		request.setLogin("test");
		request.setEmail("incorrect@domain");
		final ReportPortalException exception = assertThrows(ReportPortalException.class, () -> handler.createUser(request, "uuid"));
		assertEquals("Error in handled Request. Please, check specified parameters: 'email='incorrect@domain''", exception.getMessage());
	}

	@Test
	void createUserWithExistedEmail() {
		final UserCreationBid bid = new UserCreationBid();
		Project project = new Project();
		project.setName("test_project");
		bid.setDefaultProject(project);
		when(userCreationBidRepository.findById("uuid")).thenReturn(Optional.of(bid));
		when(userRepository.findByLogin("test")).thenReturn(Optional.empty());
		when(userRepository.findByEmail("email@domain.com")).thenReturn(Optional.of(new User()));

		final CreateUserRQConfirm request = new CreateUserRQConfirm();
		request.setLogin("test");
		request.setEmail("email@domain.com");
		final ReportPortalException exception = assertThrows(ReportPortalException.class, () -> handler.createUser(request, "uuid"));
		assertEquals("User with 'email='email@domain.com'' already exists. You couldn't create the duplicate.", exception.getMessage());
	}
}