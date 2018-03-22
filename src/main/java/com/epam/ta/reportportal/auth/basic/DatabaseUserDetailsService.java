/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-authorization
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.auth.basic;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.auth.util.AuthUtils;
import com.epam.ta.reportportal.store.database.dao.UserRepository;
import com.epam.ta.reportportal.store.database.entity.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Spring's {@link UserDetailsService} implementation. Uses {@link User} entity
 * from ReportPortal database
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
public class DatabaseUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> user = userRepository.findByLogin(username);
		if (!user.isPresent()) {
			throw new UsernameNotFoundException("User not found");
		}

		String login = user.get().getLogin();
		String password = user.get().getPassword() == null ? "" : user.get().getPassword();

		org.springframework.security.core.userdetails.User u = new org.springframework.security.core.userdetails.User(login, password, true,
				true, true, true, AuthUtils.AS_AUTHORITIES.apply(user.get().getRole())
		);

		return new ReportPortalUser(u, user.get().getId(), user.get().getRole(), user.get()
				.getProjects()
				.stream()
				.collect(Collectors.toMap(p -> p.getProject().getName(),
						p -> new ReportPortalUser.ProjectDetails(p.getProject().getId(), p.getRole())
				)));
	}

}