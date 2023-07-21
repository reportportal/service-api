package com.epam.ta.reportportal.core.project.impl;

import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.AssignUserEvent;
import com.epam.ta.reportportal.core.project.ProjectUserHandler;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.ws.model.activity.UserActivityResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectUserHandlerImpl implements ProjectUserHandler {

  private final MessageBus messageBus;
  private final ProjectUserRepository projectUserRepository;

  @Autowired
  public ProjectUserHandlerImpl(MessageBus messageBus,
      ProjectUserRepository projectUserRepository) {
    this.projectUserRepository = projectUserRepository;
    this.messageBus = messageBus;
  }

  @Override
  public ProjectUser assign(User user, Project project, ProjectRole projectRole, User creator) {
    final ProjectUser projectUser = new ProjectUser().withProjectRole(projectRole)
        .withUser(user)
        .withProject(project);
    projectUserRepository.save(projectUser);

    AssignUserEvent assignUserEvent = new AssignUserEvent(getUserActivityResource(user, project),
        creator.getId(), creator.getLogin());
    messageBus.publishActivity(assignUserEvent);

    return projectUser;
  }

  private UserActivityResource getUserActivityResource(User user, Project project) {
    UserActivityResource userActivityResource = new UserActivityResource();
    userActivityResource.setId(user.getId());
    userActivityResource.setDefaultProjectId(project.getId());
    userActivityResource.setFullName(user.getLogin());
    return userActivityResource;
  }
}
