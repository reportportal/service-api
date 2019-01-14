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
package com.epam.ta.reportportal.auth.basic;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.auth.util.AuthUtils;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Spring's {@link UserDetailsService} implementation. Uses {@link User} entity
 * from ReportPortal database
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Service
public class DatabaseUserDetailsService implements UserDetailsService {

	private UserRepository userRepository;

	@Autowired
	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> user = userRepository.findByLogin(username);
		if (!user.isPresent()) {
			throw new UsernameNotFoundException("User not found");
		}

		String login = user.get().getLogin();
		String password = user.get().getPassword() == null ? "" : user.get().getPassword();

		org.springframework.security.core.userdetails.User u = new org.springframework.security.core.userdetails.User(login,
				password,
				true,
				true,
				true,
				true,
				AuthUtils.AS_AUTHORITIES.apply(user.get().getRole())
		);

		return new ReportPortalUser(u,
				user.get().getId(),
				user.get().getRole(),
				user.get()
						.getProjects()
						.stream()
						.collect(Collectors.toMap(p -> p.getProject().getName(),
								p -> new ReportPortalUser.ProjectDetails(p.getProject().getId(), p.getProjectRole())
						))
		);
	}

}