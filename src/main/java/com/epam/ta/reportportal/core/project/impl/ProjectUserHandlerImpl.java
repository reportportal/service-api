package com.epam.ta.reportportal.core.project.impl;

import com.epam.ta.reportportal.auth.acl.ShareableObjectsHandler;
import com.epam.ta.reportportal.core.project.ProjectUserHandler;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.stereotype.Service;

@Service
public class ProjectUserHandlerImpl implements ProjectUserHandler {

	private final ProjectUserRepository projectUserRepository;
	private final ShareableObjectsHandler aclHandler;

	@Autowired
	public ProjectUserHandlerImpl(ProjectUserRepository projectUserRepository, ShareableObjectsHandler aclHandler) {
		this.projectUserRepository = projectUserRepository;
		this.aclHandler = aclHandler;
	}

	@Override
	public ProjectUser assign(User user, Project project, ProjectRole projectRole) {
		final ProjectUser projectUser = new ProjectUser().withProjectRole(projectRole)
				.withUser(user)
				.withProject(project);
		projectUserRepository.save(projectUser);

		if (projectRole.sameOrHigherThan(ProjectRole.PROJECT_MANAGER)) {
			aclHandler.permitSharedObjects(project.getId(), user.getLogin(), BasePermission.ADMINISTRATION);
		} else {
			aclHandler.permitSharedObjects(project.getId(), user.getLogin(), BasePermission.READ);
		}

		return projectUser;
	}
}
