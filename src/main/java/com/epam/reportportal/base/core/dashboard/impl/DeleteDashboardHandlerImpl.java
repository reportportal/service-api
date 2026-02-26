/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.core.dashboard.impl;

import static com.epam.reportportal.base.ws.converter.converters.DashboardConverter.TO_ACTIVITY_RESOURCE;
import static java.util.stream.Collectors.toSet;

import com.epam.reportportal.base.core.dashboard.DeleteDashboardHandler;
import com.epam.reportportal.base.core.events.domain.DashboardDeletedEvent;
import com.epam.reportportal.base.core.widget.content.remover.WidgetContentRemover;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.DashboardRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.DashboardWidgetRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.WidgetRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.dashboard.Dashboard;
import com.epam.reportportal.base.infrastructure.persistence.entity.dashboard.DashboardWidget;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.reporting.OperationCompletionRS;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class DeleteDashboardHandlerImpl implements DeleteDashboardHandler {

  private final DashboardRepository dashboardRepository;
  private final DashboardWidgetRepository dashboardWidgetRepository;
  private final WidgetRepository widgetRepository;
  private final WidgetContentRemover widgetContentRemover;
  private final ApplicationEventPublisher eventPublisher;

  @Autowired
  public DeleteDashboardHandlerImpl(DashboardRepository dashboardRepository,
      DashboardWidgetRepository dashboardWidgetRepository,
      WidgetRepository widgetRepository,
      @Qualifier("delegatingStateContentRemover") WidgetContentRemover widgetContentRemover,
      ApplicationEventPublisher eventPublisher) {
    this.dashboardRepository = dashboardRepository;
    this.dashboardWidgetRepository = dashboardWidgetRepository;
    this.widgetRepository = widgetRepository;
    this.widgetContentRemover = widgetContentRemover;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public OperationCompletionRS deleteDashboard(Long dashboardId,
      MembershipDetails membershipDetails,
      ReportPortalUser user) {
    Dashboard dashboard = dashboardRepository.findByIdAndProjectId(dashboardId,
            membershipDetails.getProjectId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.DASHBOARD_NOT_FOUND_IN_PROJECT,
            dashboardId,
            membershipDetails.getProjectName()
        ));

    Set<DashboardWidget> dashboardWidgets = dashboard.getWidgets();
    List<Widget> widgets = dashboardWidgets.stream()
        .filter(DashboardWidget::isCreatedOn)
        .map(DashboardWidget::getWidget)
        .peek(widgetContentRemover::removeContent)
        .collect(Collectors.toList());
    dashboardWidgets.addAll(
        widgets.stream().flatMap(w -> w.getDashboardWidgets().stream()).collect(toSet()));

    dashboardWidgetRepository.deleteAll(dashboardWidgets);
    dashboardRepository.delete(dashboard);
    widgetRepository.deleteAll(widgets);

    eventPublisher.publishEvent(
        new DashboardDeletedEvent(TO_ACTIVITY_RESOURCE.apply(dashboard), user.getUserId(),
            user.getUsername(),
            membershipDetails.getOrgId()));
    return new OperationCompletionRS(
        "Dashboard with ID = '" + dashboardId + "' successfully deleted.");
  }
}
