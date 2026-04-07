package com.epam.reportportal.base.core.project;

import com.epam.reportportal.base.core.events.domain.UnassignUserEvent;
import com.epam.reportportal.base.core.user.UserService;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.base.model.activity.UserActivityResource;
import com.epam.reportportal.base.ws.converter.converters.UserConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectUserService {

  private final ProjectUserRepository projectUserRepository;

  private final ApplicationEventPublisher applicationEventPublisher;
  private final UserService userService;


  public void unassignUserFromProjectsByOrgId(long orgId, long userId) {
    var unassignedUser = userService.findById(userId);
    projectUserRepository.deleteProjectUserByProjectOrganizationId(orgId, userId)
        .forEach(projectId -> {
          UserActivityResource userActivityResource = UserConverter.TO_ACTIVITY_RESOURCE
              .apply(unassignedUser, projectId);
          applicationEventPublisher.publishEvent(new UnassignUserEvent(userActivityResource, orgId));
        });
  }

}
