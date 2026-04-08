package com.epam.reportportal.base.core.project;

import com.epam.reportportal.base.core.events.domain.ProjectUsersUpdatedEvent;
import com.epam.reportportal.base.core.events.domain.UnassignUserEvent;
import com.epam.reportportal.base.core.user.UserService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.ws.converter.converters.UserConverter;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class ProjectUserService {

  private final ProjectUserRepository projectUserRepository;
  private final ProjectRepository projectRepository;
  private final UserService userService;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Single user unassign from projects (cascade from single org user removal). Publishes individual UnassignUserEvent
   * per project.
   */
  public void unassignUserFromProjectsByOrgId(Long orgId, Long userId, ReportPortalUser principal) {
    var user = userService.findById(userId);
    projectUserRepository.deleteProjectUserByProjectOrganizationId(orgId, userId)
        .forEach(projectId -> eventPublisher.publishEvent(
            new UnassignUserEvent(UserConverter.TO_ACTIVITY_RESOURCE.apply(user, projectId), principal.getUserId(),
                principal.getUsername(), orgId)));
  }

  /**
   * Bulk unassign users from projects (cascade from bulk org user removal). Publishes bulk ProjectUsersUpdatedEvent per
   * affected project.
   */
  public void unassignUsersFromProjectsByOrgId(Long orgId, List<Long> userIds) {
    if (CollectionUtils.isEmpty(userIds)) {
      return;
    }

    var userIdsByProjectId = projectUserRepository.findProjectIdAndUserIdByOrgId(orgId).stream()
        .collect(Collectors.groupingBy(
            row -> ((Number) row[0]).longValue(),
            Collectors.mapping(row -> ((Number) row[1]).longValue(), Collectors.toList())
        ));

    if (userIdsByProjectId.isEmpty()) {
      return;
    }

    projectUserRepository.deleteByOrgIdAndUserIds(orgId, userIds);

    var projectsById = projectRepository.findAllById(userIdsByProjectId.keySet()).stream()
        .collect(Collectors.toMap(Project::getId, Function.identity()));

    var userIdsSet = Set.copyOf(userIds);
    userIdsByProjectId.forEach((projectId, beforeUserIds) -> {
      var removedUserIds = beforeUserIds.stream()
          .filter(userIdsSet::contains)
          .toList();

      if (removedUserIds.isEmpty()) {
        return;
      }

      var afterUserIds = beforeUserIds.stream()
          .filter(id -> !userIdsSet.contains(id))
          .toList();

      var project = projectsById.get(projectId);
      if (project != null) {
        eventPublisher.publishEvent(
            new ProjectUsersUpdatedEvent(projectId, project.getName(), orgId, beforeUserIds, afterUserIds,
                EventAction.UNASSIGN));
      }
    });
  }
}
