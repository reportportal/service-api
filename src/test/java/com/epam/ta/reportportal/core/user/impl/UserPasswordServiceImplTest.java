/*
 * Copyright 2020 EPAM Systems
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

package com.epam.ta.reportportal.core.user.impl;

import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.user.ChangePasswordRQ;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQFull;
import com.epam.ta.reportportal.ws.model.user.ResetPasswordRQ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class UserPasswordServiceImplTest {

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserPasswordServiceImpl userPasswordService;

	@Test
	void checkAndUpdatePositive() {
		String oldPass = "oldPass";
		String newPass = "newPass";
		String newPassEncrypted = "newPassEncrypted";

		User user = new User();
		user.setPassword(oldPass);

		ChangePasswordRQ request = new ChangePasswordRQ();
		request.setOldPassword(oldPass);
		request.setNewPassword(newPass);

		when(passwordEncoder.matches(oldPass, oldPass)).thenReturn(true);
		when(passwordEncoder.encode(newPass)).thenReturn(newPassEncrypted);

		userPasswordService.checkAndUpdate(user, request);

		assertEquals(newPassEncrypted, user.getPassword());
	}

	@Test
	void checkAndUpdateNegative() {
		String oldPass = "oldPass";
		String newPass = "newPass";
		String newPassEncrypted = "newPassEncrypted";

		User user = new User();
		user.setPassword("userPass");

		ChangePasswordRQ request = new ChangePasswordRQ();
		request.setOldPassword(oldPass);
		request.setNewPassword(newPass);

		when(passwordEncoder.matches(oldPass, user.getPassword())).thenReturn(false);

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> userPasswordService.checkAndUpdate(user, request)
		);
		assertEquals("Forbidden operation. Old password not match with stored.", exception.getMessage());
		verify(passwordEncoder, never()).encode(newPass);
	}

	@Test
	void encryptResetRqTest() {
		String unencrypted = "unencrypted";
		String encrypted = "encrypted";

		ResetPasswordRQ request = new ResetPasswordRQ();
		request.setPassword(unencrypted);

		when(passwordEncoder.encode(unencrypted)).thenReturn(encrypted);

		userPasswordService.encrypt(request);

		assertEquals(encrypted, request.getPassword());
	}

	@Test
	void encryptFullRqTest() {
		String unencrypted = "unencrypted";
		String encrypted = "encrypted";

		CreateUserRQFull request = new CreateUserRQFull();
		request.setPassword(unencrypted);

		when(passwordEncoder.encode(unencrypted)).thenReturn(encrypted);

		userPasswordService.encrypt(request);

		assertEquals(encrypted, request.getPassword());
	}
}