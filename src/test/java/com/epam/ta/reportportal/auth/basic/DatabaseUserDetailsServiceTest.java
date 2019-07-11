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

package com.epam.ta.reportportal.auth.basic;

import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.entity.user.UserType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class DatabaseUserDetailsServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private DatabaseUserDetailsService userDetailsService;

	@Test
	void userNotFoundTest() {
		when(userRepository.findByLogin("not_exist")).thenReturn(Optional.empty());

		UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
				() -> userDetailsService.loadUserByUsername("not_exist")
		);

		assertEquals("User not found", exception.getMessage());
	}

	@Test
	void loadUserWithEmptyPassword() {
		User user = new User();
		user.setLogin("user");
		user.setId(1L);
		user.setEmail("email@domain.com");
		user.setExpired(false);
		user.setUserType(UserType.INTERNAL);
		user.setRole(UserRole.USER);
		when(userRepository.findByLogin("user")).thenReturn(Optional.of(user));

		UserDetails userDetails = userDetailsService.loadUserByUsername("user");

		assertEquals(user.getLogin(), userDetails.getUsername());
		assertTrue(userDetails.getPassword().isEmpty());
		assertEquals(!user.isExpired(), userDetails.isAccountNonExpired());
	}
}