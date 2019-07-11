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

import com.epam.ta.reportportal.dao.UserCreationBidRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.YesNoRS;
import com.epam.ta.reportportal.ws.model.user.UserBidRS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class GetUserHandlerImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserCreationBidRepository userCreationBidRepository;

	@InjectMocks
	private GetUserHandlerImpl handler;

	@Test
	void getNotExistedUserByUsername() {
		when(userRepository.findByLogin("not_exist")).thenReturn(Optional.empty());

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getUser("not_exist", getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L))
		);
		assertEquals("User 'not_exist' not found.", exception.getMessage());
	}

	@Test
	void getNotExistedUserByLoggedInUser() {
		when(userRepository.findByLogin("not_exist")).thenReturn(Optional.empty());

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getUser(getRpUser("not_exist", UserRole.USER, ProjectRole.MEMBER, 1L))
		);
		assertEquals("User 'not_exist' not found.", exception.getMessage());
	}

	@Test
	void getEmptyBidInfo() {
		String uuid = "uuid";

		when(userCreationBidRepository.findById(uuid)).thenReturn(Optional.empty());

		UserBidRS bidInformation = handler.getBidInformation(uuid);
		assertFalse(bidInformation.getIsActive());
		assertNull(bidInformation.getEmail());
		assertNull(bidInformation.getUuid());
	}

	@Test
	void validateInfoByNotExistUsername() {
		String username = "not_exist";
		when(userRepository.findByLogin(username)).thenReturn(Optional.empty());

		YesNoRS yesNoRS = handler.validateInfo(username, null);

		assertFalse(yesNoRS.getIs());
	}

	@Test
	void validateInfoByExistEmail() {
		String email = "exist@domain.com";
		when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

		YesNoRS yesNoRS = handler.validateInfo(null, email);

		assertTrue(yesNoRS.getIs());
	}

	@Test
	void validateInfoByNotExistEmail() {
		String email = "not_exist@domain.com";
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		YesNoRS yesNoRS = handler.validateInfo(null, email);

		assertFalse(yesNoRS.getIs());
	}

	@Test
	void validateInfoNullRequest() {
		assertFalse(handler.validateInfo(null, null).getIs());
	}
}