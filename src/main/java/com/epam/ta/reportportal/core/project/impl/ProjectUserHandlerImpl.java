package com.epam.ta.reportportal.core.project.impl;

import com.epam.ta.reportportal.core.project.ProjectUserHandler;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectUserHandlerImpl implements ProjectUserHandler {

	private final ProjectUserRepository projectUserRepository;

	@Autowired
	public ProjectUserHandlerImpl(ProjectUserRepository projectUserRepository) {
		this.projectUserRepository = projectUserRepository;
	}

	@Override
	public ProjectUser assign(User user, Project project, ProjectRole projectRole) {
		final ProjectUser projectUser = new ProjectUser().withProjectRole(projectRole)
				.withUser(user)
				.withProject(project);
		projectUserRepository.save(projectUser);

		return projectUser;
	}
}
