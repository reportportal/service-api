package com.epam.reportportal.base.core.events.domain;

import com.epam.reportportal.base.model.activity.DashboardActivityResource;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Andrei Varabyeu
 */
@Getter
@NoArgsConstructor
public class DashboardUpdatedStateEvent extends AbstractEvent<DashboardActivityResource> {

  /**
   * Constructs a DashboardUpdatedStateEvent.
   *
   * @param before    The dashboard state before the update
   * @param after     The dashboard state after the update
   * @param userId    The ID of the user who updated the dashboard
   * @param userLogin The login of the user who updated the dashboard
   */
  public DashboardUpdatedStateEvent(DashboardActivityResource before, DashboardActivityResource after, Long userId,
      String userLogin) {
    super(userId, userLogin, before, after);
  }
}
