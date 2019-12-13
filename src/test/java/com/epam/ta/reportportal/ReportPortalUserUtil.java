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

package com.epam.ta.reportportal;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.google.common.collect.Sets;
import org.assertj.core.util.Maps;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class ReportPortalUserUtil {

	public static final String TEST_PROJECT_NAME = "test_project";

	private ReportPortalUserUtil() {
		//static only
	}

	public static ReportPortalUser getRpUser(String login, UserRole userRole, ProjectRole projectRole, Long projectId) {
		return ReportPortalUser.userBuilder()
				.withUserName(login)
				.withPassword("test")
				.withAuthorities(Sets.newHashSet(new SimpleGrantedAuthority(userRole.getAuthority())))
				.withUserId(1L)
				.withEmail("test@email.com")
				.withUserRole(userRole)
				.withProjectDetails(Maps.newHashMap("test_project",
						new ReportPortalUser.ProjectDetails(projectId, TEST_PROJECT_NAME, projectRole)
				))
				.build();
	}
}