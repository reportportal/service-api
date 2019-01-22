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

import com.epam.ta.reportportal.dao.UserCreationBidRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
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
public class GetUserHandlerImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserCreationBidRepository userCreationBidRepository;

	@InjectMocks
	private GetUserHandlerImpl handler;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getNotExistedUserByUsername() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("User 'not_exist' not found.");

		when(userRepository.findByLogin("not_exist")).thenReturn(Optional.empty());

		handler.getUser("not_exist", getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L));
	}

	@Test
	public void getNotExistedUserByLoggedInUser() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("User 'not_exist' not found.");

		when(userRepository.findByLogin("not_exist")).thenReturn(Optional.empty());

		handler.getUser(getRpUser("not_exist", UserRole.USER, ProjectRole.MEMBER, 1L));
	}

	@Test
	public void name() {

	}
}