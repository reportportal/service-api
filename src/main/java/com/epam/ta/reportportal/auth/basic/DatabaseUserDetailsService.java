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
import com.epam.ta.reportportal.store.jooq.enums.JUserRoleEnum;
import com.epam.ta.reportportal.store.jooq.tables.pojos.JProject;
import com.epam.ta.reportportal.store.jooq.tables.pojos.JProjectUser;
import com.epam.ta.reportportal.store.jooq.tables.pojos.JUsers;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.store.jooq.tables.JProject.PROJECT;
import static com.epam.ta.reportportal.store.jooq.tables.JProjectUser.PROJECT_USER;
import static com.epam.ta.reportportal.store.jooq.tables.JUsers.USERS;

/**
 * Spring's {@link UserDetailsService} implementation. Uses {@link JUsers} entity
 * from ReportPortal database
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
public class DatabaseUserDetailsService implements UserDetailsService {

	@Autowired
	private DSLContext dsl;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Map<JUsers, List<FullProject>> userProjects = dsl.select()
				.from(USERS)
				.join(PROJECT_USER)
				.on(PROJECT_USER.USER_ID.eq(USERS.ID))
				.join(PROJECT)
				.on(PROJECT_USER.PROJECT_ID.eq(PROJECT.ID))
				.where(USERS.LOGIN.eq(username))
				.fetchGroups(
						// Map records first into the USER table and then into the key POJO type
						r -> r.into(JUsers.class),

						// Map records first into the ROLE table and then into the value POJO type
						r -> new FullProject(r.into(JProject.class), r.into(JProjectUser.class))
				);

		if (userProjects.isEmpty()) {
			throw new UsernameNotFoundException("User not found");
		}
		Map.Entry<JUsers, List<FullProject>> userProject = userProjects.entrySet().iterator().next();
		JUsers userEntity = userProject.getKey();

		String login = userEntity.getLogin();
		String password = userEntity.getPassword() == null ? "" : userEntity.getPassword();

		org.springframework.security.core.userdetails.User u = new org.springframework.security.core.userdetails.User(
				login,
				password,
				true,
				true,
				true,
				true,
				AuthUtils.AS_AUTHORITIES.apply(JUserRoleEnum.valueOf(userEntity.getRole().name().toUpperCase()))
		);
		return new ReportPortalUser(
				u,
				userProject.getValue().stream().collect(Collectors.toMap(p -> p.project.getName(), p -> p.usersProject.getProjectRole()))
		);
	}

	static class FullProject {
		JProjectUser usersProject;
		JProject project;

		FullProject(JProject project, JProjectUser usersProject) {
			this.usersProject = usersProject;
			this.project = project;
		}
	}
}