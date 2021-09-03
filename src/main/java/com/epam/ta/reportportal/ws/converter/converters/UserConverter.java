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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.commons.MoreCollectors;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserType;
import com.epam.ta.reportportal.ws.model.activity.UserActivityResource;
import com.epam.ta.reportportal.ws.model.user.SearchUserResource;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import com.google.common.collect.Lists;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Converts user from database to resource
 *
 * @author Pavel Bortnik
 */
public final class UserConverter {

	private UserConverter() {
		//static only
	}

	public static final Function<User, UserResource> TO_RESOURCE = user -> {
		UserResource resource = new UserResource();
		resource.setId(user.getId());
		resource.setUserId(user.getLogin());
		resource.setEmail(user.getEmail());
		resource.setPhotoId(user.getAttachment());
		resource.setFullName(user.getFullName());
		resource.setAccountType(user.getUserType().toString());
		resource.setUserRole(user.getRole().toString());
		resource.setIsLoaded(UserType.UPSA != user.getUserType());
		resource.setMetadata(user.getMetadata().getMetadata());

		if (null != user.getProjects()) {
			List<ProjectUser> projects = Lists.newArrayList(user.getProjects());
			projects.sort(Comparator.comparing(compare -> compare.getProject().getName()));
			Map<String, UserResource.AssignedProject> userProjects = user.getProjects()
					.stream()
					.collect(MoreCollectors.toLinkedMap(p -> p.getProject().getName(), p -> {
						UserResource.AssignedProject assignedProject = new UserResource.AssignedProject();
						assignedProject.setEntryType(p.getProject().getProjectType().name());
						assignedProject.setProjectRole(p.getProjectRole().toString());
						return assignedProject;
					}));
			resource.setAssignedProjects(userProjects);
		}
		return resource;
	};

	public static final Function<User, SearchUserResource> TO_SEARCH_RESOURCE = user -> {
		final SearchUserResource resource = new SearchUserResource();
		resource.setId(user.getId());
		resource.setLogin(user.getLogin());
		resource.setEmail(user.getEmail());
		resource.setFullName(user.getFullName());
		return resource;
	};

	public static final BiFunction<User, Long, UserActivityResource> TO_ACTIVITY_RESOURCE = (user, projectId) -> {
		UserActivityResource resource = new UserActivityResource();
		resource.setId(user.getId());
		resource.setDefaultProjectId(projectId);
		resource.setFullName(user.getFullName());
		return resource;
	};

}
