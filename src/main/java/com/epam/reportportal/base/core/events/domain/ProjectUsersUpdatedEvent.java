package com.epam.reportportal.base.core.events.domain;

import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event published when project users are updated in bulk.
 */
@Getter
@NoArgsConstructor
public class ProjectUsersUpdatedEvent extends AbstractEvent<List<Long>> {

  private String projectName;
  private EventAction action;

  /**
   * Constructor for user-initiated events.
   */
  public ProjectUsersUpdatedEvent(Long userId, String userLogin, Long projectId, String projectName,
      Long organizationId, List<Long> beforeUserIds, List<Long> afterUserIds, EventAction action) {
    super(userId, userLogin, beforeUserIds, afterUserIds);
    this.projectId = projectId;
    this.projectName = projectName;
    this.organizationId = organizationId;
    this.action = action;
  }

  /**
   * Constructor for system/cascade events.
   */
  public ProjectUsersUpdatedEvent(Long projectId, String projectName, Long organizationId, List<Long> beforeUserIds,
      List<Long> afterUserIds, EventAction action) {
    super();
    this.before = beforeUserIds;
    this.after = afterUserIds;
    this.projectId = projectId;
    this.projectName = projectName;
    this.organizationId = organizationId;
    this.action = action;
  }
}
