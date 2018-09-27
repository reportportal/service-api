package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserType;
import com.epam.ta.reportportal.ws.model.user.UserResource;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class UserConverter {

	private UserConverter() {
		//static only
	}

	public static final Function<User, UserResource> TO_RESOURCE = user -> {
		UserResource resource = new UserResource();
		resource.setUserId(user.getId().toString());
		resource.setEmail(user.getEmail());
		resource.setPhotoId(user.getAttachment());
		resource.setFullName(user.getFullName());
		resource.setAccountType(user.getUserType().toString());
		resource.setUserRole(user.getRole().toString());
		resource.setIsLoaded(UserType.UPSA != user.getUserType());

		if (null != user.getProjects()) {
			Map<String, UserResource.AssignedProject> userProjects = user.getProjects()
					.stream()
					.collect(Collectors.toMap(p -> p.getProject().getName(), p -> {
						UserResource.AssignedProject assignedProject = new UserResource.AssignedProject();
						assignedProject.setProjectRole(p.getRole().toString());
						return assignedProject;
					}));
			resource.setDefaultProject(user.getDefaultProject().getName());
			resource.setAssignedProjects(userProjects);
		}
		return resource;
	};

}
