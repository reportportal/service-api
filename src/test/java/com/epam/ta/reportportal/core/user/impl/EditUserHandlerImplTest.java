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

import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.entity.user.UserType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.user.ChangePasswordRQ;
import com.epam.ta.reportportal.ws.model.user.EditUserRQ;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class EditUserHandlerImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private ProjectRepository projectRepository;

	@InjectMocks
	private EditUserHandlerImpl handler;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void uploadNotExistUserPhoto() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("User 'not_exists' not found.");

		when(userRepository.findByLogin("not_exists")).thenReturn(Optional.empty());

		handler.uploadPhoto("not_exists", new MockMultipartFile("photo", new byte[100]));
	}

	@Test
	public void uploadOversizePhoto() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Binary data cannot be saved. Image size should be less than 1 mb");

		when(userRepository.findByLogin("test")).thenReturn(Optional.of(new User()));

		handler.uploadPhoto("test", new MockMultipartFile("photo", new byte[1024 * 1024 + 10]));
	}

	@Test
	public void deleteNotExistUserPhoto() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("User 'not_exist' not found.");

		when(userRepository.findByLogin("not_exist")).thenReturn(Optional.empty());

		handler.deletePhoto("not_exist");
	}

	@Test
	public void deleteExternalUserPhoto() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("You do not have enough permissions. Unable to change photo for external user");

		User user = new User();
		user.setLogin("test");
		user.setUserType(UserType.UPSA);
		when(userRepository.findByLogin("test")).thenReturn(Optional.of(user));

		handler.deletePhoto("test");
	}

	@Test
	public void changeNotExistUserPassword() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("User 'not_exist' not found.");

		when(userRepository.findByLogin("not_exist")).thenReturn(Optional.empty());

		handler.changePassword(getRpUser("not_exist", UserRole.USER, ProjectRole.MEMBER, 1L), new ChangePasswordRQ());
	}

	@Test
	public void changeExternalUserPassword() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Forbidden operation. Impossible to change password for external users.");

		User user = new User();
		user.setLogin("test");
		user.setUserType(UserType.UPSA);
		when(userRepository.findByLogin("test")).thenReturn(Optional.of(user));

		handler.changePassword(getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L), new ChangePasswordRQ());
	}

	@Test
	public void changePasswordWithIncorrectOldPassword() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Forbidden operation. Old password not match with stored.");

		User user = new User();
		user.setLogin("test");
		user.setUserType(UserType.INTERNAL);
		user.setPassword("CBBA6D57536106F93CDEB6E426C2750E");
		when(userRepository.findByLogin("test")).thenReturn(Optional.of(user));

		final ChangePasswordRQ changePasswordRQ = new ChangePasswordRQ();
		changePasswordRQ.setOldPassword("wrongPass");
		handler.changePassword(getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L), changePasswordRQ);
	}

	@Test
	public void editNotExistUser() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("User 'not_exist' not found.");

		when(userRepository.findByLogin("not_exist")).thenReturn(Optional.empty());

		handler.editUser("not_exist", new EditUserRQ(), UserRole.USER);
	}

	@Test
	public void editUserWithIncorrectRole() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Error in handled Request. Please, check specified parameters: 'Incorrect specified Account Role parameter.'");

		when(userRepository.findByLogin("test")).thenReturn(Optional.of(new User()));
		final EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setRole("not_exist_role");

		handler.editUser("test", editUserRQ, UserRole.ADMINISTRATOR);
	}

	@Test
	public void editUserWithNotExistedDefaultProject() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Project 'not_exist_project' not found. Did you use correct project name?");

		when(userRepository.findByLogin("test")).thenReturn(Optional.of(new User()));
		when(projectRepository.findByName("not_exist_project")).thenReturn(Optional.empty());
		final EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setDefaultProject("not_exist_project");

		handler.editUser("test", editUserRQ, UserRole.ADMINISTRATOR);
	}

	@Test
	public void changeExternalUserEmail() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("You do not have enough permissions. Unable to change email for external user");

		User user = new User();
		user.setUserType(UserType.LDAP);
		when(userRepository.findByLogin("test")).thenReturn(Optional.of(user));
		final EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setEmail("newemail@domain.com");

		handler.editUser("test", editUserRQ, UserRole.USER);
	}

	@Test
	public void editUserWithIncorrectEmail() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Error in handled Request. Please, check specified parameters: ' wrong email: incorrect#domain.com'");

		User user = new User();
		user.setUserType(UserType.INTERNAL);
		when(userRepository.findByLogin("test")).thenReturn(Optional.of(user));
		final EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setEmail("incorrect#domain.com");

		handler.editUser("test", editUserRQ, UserRole.USER);
	}

	@Test
	public void editUserWithAlreadyExistedEmail() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("User with 'existed@domain.com' already exists. You couldn't create the duplicate.");

		User user = new User();
		user.setUserType(UserType.INTERNAL);
		when(userRepository.findByLogin("test")).thenReturn(Optional.of(user));
		when(userRepository.findByEmail("existed@domain.com")).thenReturn(Optional.of(new User()));
		final EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setEmail("existed@domain.com");

		handler.editUser("test", editUserRQ, UserRole.USER);
	}

	@Test
	public void editExternalUserFullName() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("You do not have enough permissions. Unable to change full name for external user");

		User user = new User();
		user.setUserType(UserType.GITHUB);
		when(userRepository.findByLogin("test")).thenReturn(Optional.of(user));
		final EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setFullName("full name");

		handler.editUser("test", editUserRQ, UserRole.USER);
	}
}