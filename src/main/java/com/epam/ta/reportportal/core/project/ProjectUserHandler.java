package com.epam.ta.reportportal.core.project;

import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;

public interface ProjectUserHandler {

	ProjectUser assign(User user, Project project, ProjectRole projectRole);
}
