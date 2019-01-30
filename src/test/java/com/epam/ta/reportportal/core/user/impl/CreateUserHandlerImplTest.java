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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserCreationBidRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserCreationBid;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQ;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQConfirm;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQFull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class CreateUserHandlerImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private UserCreationBidRepository userCreationBidRepository;

	@InjectMocks
	private CreateUserHandlerImpl handler;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void createByNotExistedAdmin() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("User 'admin' not found.");

		final ReportPortalUser rpUser = getRpUser("admin", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);
		when(userRepository.findByLogin("admin")).thenReturn(Optional.empty());

		handler.createUserByAdmin(new CreateUserRQFull(), rpUser, "url");
	}

	@Test
	public void createByNotAdmin() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("You do not have enough permissions. Only administrator can create new user. Your role is - USER");

		final ReportPortalUser rpUser = getRpUser("admin", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);
		User user = new User();
		user.setRole(UserRole.USER);
		when(userRepository.findByLogin("admin")).thenReturn(Optional.of(user));

		handler.createUserByAdmin(new CreateUserRQFull(), rpUser, "url");
	}

	@Test
	public void createByAdminUserAlreadyExists() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("User with 'login='new_user'' already exists. You couldn't create the duplicate.");

		final ReportPortalUser rpUser = getRpUser("admin", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);
		User creator = new User();
		creator.setRole(UserRole.ADMINISTRATOR);
		when(userRepository.findByLogin("admin")).thenReturn(Optional.of(creator));
		when(userRepository.findByLogin("new_user")).thenReturn(Optional.of(new User()));

		final CreateUserRQFull request = new CreateUserRQFull();
		request.setLogin("new_user");
		handler.createUserByAdmin(request, rpUser, "url");
	}

	@Test
	public void createByAdminWithIncorrectName() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Incorrect Request. Username '#$$/' consists only of special characters");

		final ReportPortalUser rpUser = getRpUser("admin", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);
		User creator = new User();
		creator.setRole(UserRole.ADMINISTRATOR);
		when(userRepository.findByLogin("admin")).thenReturn(Optional.of(creator));
		when(userRepository.findByLogin("#$$/")).thenReturn(Optional.empty());

		final CreateUserRQFull request = new CreateUserRQFull();
		request.setLogin("#$$/");
		handler.createUserByAdmin(request, rpUser, "url");
	}

	@Test
	public void createByAdminWithIncorrectEmail() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Error in handled Request. Please, check specified parameters: 'email = 'incorrect@email''");

		final ReportPortalUser rpUser = getRpUser("admin", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);
		User creator = new User();
		creator.setRole(UserRole.ADMINISTRATOR);
		when(userRepository.findByLogin("admin")).thenReturn(Optional.of(creator));
		when(userRepository.findByLogin("new_user")).thenReturn(Optional.empty());

		final CreateUserRQFull request = new CreateUserRQFull();
		request.setLogin("new_user");
		request.setEmail("incorrect@email");
		handler.createUserByAdmin(request, rpUser, "url");
	}

	@Test
	public void createByAdminWithExistedEmail() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("User with 'email = 'correct@domain.com'' already exists. You couldn't create the duplicate.");

		final ReportPortalUser rpUser = getRpUser("admin", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);
		User creator = new User();
		creator.setRole(UserRole.ADMINISTRATOR);
		when(userRepository.findByLogin("admin")).thenReturn(Optional.of(creator));
		when(userRepository.findByLogin("new_user")).thenReturn(Optional.empty());
		when(userRepository.findByEmail("correct@domain.com")).thenReturn(Optional.of(new User()));

		final CreateUserRQFull request = new CreateUserRQFull();
		request.setLogin("new_user");
		request.setEmail("correct@domain.com");
		handler.createUserByAdmin(request, rpUser, "url");
	}

	@Test
	public void CreateUserBidOnNotExistedProject() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Project 'not_exists' not found. Did you use correct project name?");

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		when(projectRepository.findByName("not_exists")).thenReturn(Optional.empty());

		CreateUserRQ request = new CreateUserRQ();
		request.setDefaultProject("not_exists");
		handler.createUserBid(request, rpUser, "emailUrl");
	}

	@Test
	public void createUserWithoutBid() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Incorrect Request. Impossible to register user. UUID expired or already registered.");

		when(userCreationBidRepository.findById("uuid")).thenReturn(Optional.empty());

		handler.createUser(new CreateUserRQConfirm(), "uuid");
	}

	@Test
	public void createAlreadyExistedUser() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("User with 'login='test'' already exists. You couldn't create the duplicate.");

		when(userCreationBidRepository.findById("uuid")).thenReturn(Optional.of(new UserCreationBid()));
		when(userRepository.findByLogin("test")).thenReturn(Optional.of(new User()));

		final CreateUserRQConfirm request = new CreateUserRQConfirm();
		request.setLogin("test");
		handler.createUser(request, "uuid");
	}

	@Test
	public void createUserWithIncorrectLogin() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Incorrect Request. Username '##$%/' consists only of special characters");

		when(userCreationBidRepository.findById("uuid")).thenReturn(Optional.of(new UserCreationBid()));
		when(userRepository.findByLogin("##$%/")).thenReturn(Optional.empty());

		final CreateUserRQConfirm request = new CreateUserRQConfirm();
		request.setLogin("##$%/");
		handler.createUser(request, "uuid");
	}

	@Test
	public void CreateUserWithIncorrectDefaultProject() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Project 'not_existed' not found. Did you use correct project name?");

		final UserCreationBid bid = new UserCreationBid();
		Project project = new Project();
		project.setName("not_existed");
		bid.setDefaultProject(project);
		when(userCreationBidRepository.findById("uuid")).thenReturn(Optional.of(bid));
		when(userRepository.findByLogin("test")).thenReturn(Optional.empty());
		when(projectRepository.findByName("not_existed")).thenReturn(Optional.empty());

		final CreateUserRQConfirm request = new CreateUserRQConfirm();
		request.setLogin("test");
		handler.createUser(request, "uuid");
	}

	@Test
	public void createUserWithIncorrectEmail() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Error in handled Request. Please, check specified parameters: 'incorrect@domain'");

		final UserCreationBid bid = new UserCreationBid();
		Project project = new Project();
		project.setName("test_project");
		bid.setDefaultProject(project);
		when(userCreationBidRepository.findById("uuid")).thenReturn(Optional.of(bid));
		when(userRepository.findByLogin("test")).thenReturn(Optional.empty());
		when(projectRepository.findByName("test_project")).thenReturn(Optional.of(project));

		final CreateUserRQConfirm request = new CreateUserRQConfirm();
		request.setLogin("test");
		request.setEmail("incorrect@domain");
		handler.createUser(request, "uuid");
	}

	@Test
	public void createUserWithExistedEmail() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("User with 'email='email@domain.com'' already exists. You couldn't create the duplicate.");

		final UserCreationBid bid = new UserCreationBid();
		Project project = new Project();
		project.setName("test_project");
		bid.setDefaultProject(project);
		when(userCreationBidRepository.findById("uuid")).thenReturn(Optional.of(bid));
		when(userRepository.findByLogin("test")).thenReturn(Optional.empty());
		when(projectRepository.findByName("test_project")).thenReturn(Optional.of(project));
		when(userRepository.findByEmail("email@domain.com")).thenReturn(Optional.of(new User()));

		final CreateUserRQConfirm request = new CreateUserRQConfirm();
		request.setLogin("test");
		request.setEmail("email@domain.com");
		handler.createUser(request, "uuid");
	}
}