package com.epam.reportportal.base.core.events.domain;

import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event published when organization users are updated in bulk.
 */
@Getter
@NoArgsConstructor
public class OrganizationUsersUpdatedEvent extends AbstractEvent<List<Long>> {

  private String organizationName;
  private EventAction action;

  /**
   * Constructor for user-initiated events.
   */
  public OrganizationUsersUpdatedEvent(Long userId, String userLogin, Long organizationId, String organizationName,
      List<Long> beforeUserIds, List<Long> afterUserIds, EventAction action) {
    super(userId, userLogin, beforeUserIds, afterUserIds);
    this.organizationId = organizationId;
    this.organizationName = organizationName;
    this.action = action;
  }

  /**
   * Constructor for system-initiated events (no user context).
   */
  public OrganizationUsersUpdatedEvent(Long organizationId, String organizationName,
      List<Long> beforeUserIds, List<Long> afterUserIds, EventAction action) {
    super();
    this.organizationId = organizationId;
    this.organizationName = organizationName;
    this.before = beforeUserIds;
    this.after = afterUserIds;
    this.action = action;
  }
}
