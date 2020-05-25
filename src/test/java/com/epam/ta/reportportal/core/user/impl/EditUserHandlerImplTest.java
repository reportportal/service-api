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

import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.entity.user.UserType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.user.ChangePasswordRQ;
import com.epam.ta.reportportal.ws.model.user.EditUserRQ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class EditUserHandlerImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private EditUserHandlerImpl handler;

	@Test
	void uploadNotExistUserPhoto() {
		when(userRepository.findByLogin("not_exists")).thenReturn(Optional.empty());

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.uploadPhoto("not_exists", new MockMultipartFile("photo", new byte[100]))
		);
		assertEquals("User 'not_exists' not found.", exception.getMessage());
	}

	@Test
	void uploadOversizePhoto() {
		when(userRepository.findByLogin("test")).thenReturn(Optional.of(new User()));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.uploadPhoto("test", new MockMultipartFile("photo", new byte[1024 * 1024 + 10]))
		);
		assertEquals("Binary data cannot be saved. Image size should be less than 1 mb", exception.getMessage());
	}

	@Test
	void deleteNotExistUserPhoto() {
		when(userRepository.findByLogin("not_exist")).thenReturn(Optional.empty());

		final ReportPortalException exception = assertThrows(ReportPortalException.class, () -> handler.deletePhoto("not_exist"));
		assertEquals("User 'not_exist' not found.", exception.getMessage());
	}

	@Test
	void deleteExternalUserPhoto() {
		User user = new User();
		user.setLogin("test");
		user.setUserType(UserType.UPSA);
		when(userRepository.findByLogin("test")).thenReturn(Optional.of(user));

		final ReportPortalException exception = assertThrows(ReportPortalException.class, () -> handler.deletePhoto("test"));
		assertEquals("You do not have enough permissions. Unable to change photo for external user", exception.getMessage());
	}

	@Test
	void changeNotExistUserPassword() {
		when(userRepository.findByLogin("not_exist")).thenReturn(Optional.empty());

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.changePassword(getRpUser("not_exist", UserRole.USER, ProjectRole.MEMBER, 1L), new ChangePasswordRQ())
		);
		assertEquals("User 'not_exist' not found.", exception.getMessage());
	}

	@Test
	void changeExternalUserPassword() {
		User user = new User();
		user.setLogin("test");
		user.setUserType(UserType.UPSA);
		when(userRepository.findByLogin("test")).thenReturn(Optional.of(user));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.changePassword(getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L), new ChangePasswordRQ())
		);
		assertEquals("Forbidden operation. Impossible to change password for external users.", exception.getMessage());
	}

	@Test
	void changePasswordWithIncorrectOldPassword() {
		User user = new User();
		user.setLogin("test");
		user.setUserType(UserType.INTERNAL);
		user.setPassword("CBBA6D57536106F93CDEB6E426C2750E");
		when(userRepository.findByLogin("test")).thenReturn(Optional.of(user));

		final ChangePasswordRQ changePasswordRQ = new ChangePasswordRQ();
		changePasswordRQ.setOldPassword("wrongPass");

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.changePassword(getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L), changePasswordRQ)
		);
		assertEquals("Forbidden operation. Old password not match with stored.", exception.getMessage());
	}

	@Test
	void editNotExistUser() {
		when(userRepository.findByLogin("not_exist")).thenReturn(Optional.empty());

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.editUser("not_exist", new EditUserRQ(), getRpUser("not_exist", UserRole.USER, ProjectRole.MEMBER, 1L))
		);
		assertEquals("User 'not_exist' not found.", exception.getMessage());
	}

	@Test
	void editUserWithIncorrectRole() {
		User user = new User();
		user.setLogin("test");
		when(userRepository.findByLogin("test")).thenReturn(Optional.of(user));
		final EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setRole("not_exist_role");

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.editUser("test", editUserRQ, getRpUser("not_exist", UserRole.ADMINISTRATOR, ProjectRole.MEMBER, 1L))
		);
		assertEquals("Error in handled Request. Please, check specified parameters: 'Incorrect specified Account Role parameter.'",
				exception.getMessage()
		);
	}

	@Test
	void changeExternalUserEmail() {
		User user = new User();
		user.setUserType(UserType.LDAP);
		when(userRepository.findByLogin("test")).thenReturn(Optional.of(user));
		final EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setEmail("newemail@domain.com");

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.editUser("test", editUserRQ, getRpUser("not_exist", UserRole.USER, ProjectRole.MEMBER, 1L))
		);
		assertEquals("You do not have enough permissions. Unable to change email for external user", exception.getMessage());
	}

	@Test
	void editUserWithIncorrectEmail() {
		User user = new User();
		user.setUserType(UserType.INTERNAL);
		when(userRepository.findByLogin("test")).thenReturn(Optional.of(user));
		final EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setEmail("incorrect#domain.com");

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.editUser("test", editUserRQ, getRpUser("not_exist", UserRole.USER, ProjectRole.MEMBER, 1L))
		);
		assertEquals("Error in handled Request. Please, check specified parameters: ' wrong email: incorrect#domain.com'",
				exception.getMessage()
		);
	}

	@Test
	void editUserWithAlreadyExistedEmail() {
		User user = new User();
		user.setUserType(UserType.INTERNAL);
		when(userRepository.findByLogin("test")).thenReturn(Optional.of(user));
		when(userRepository.findByEmail("existed@domain.com")).thenReturn(Optional.of(new User()));
		final EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setEmail("existed@domain.com");

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.editUser("test", editUserRQ, getRpUser("not_exist", UserRole.USER, ProjectRole.MEMBER, 1L))
		);
		assertEquals("User with 'existed@domain.com' already exists. You couldn't create the duplicate.", exception.getMessage());
	}

	@Test
	void editExternalUserFullName() {
		User user = new User();
		user.setUserType(UserType.GITHUB);
		when(userRepository.findByLogin("test")).thenReturn(Optional.of(user));
		final EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setFullName("full name");

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.editUser("test", editUserRQ, getRpUser("not_exist", UserRole.USER, ProjectRole.MEMBER, 1L))
		);
		assertEquals("You do not have enough permissions. Unable to change full name for external user", exception.getMessage());
	}
}