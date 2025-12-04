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

package com.epam.reportportal.core.dashboard.impl;

import static com.epam.reportportal.ws.converter.converters.DashboardConverter.TO_ACTIVITY_RESOURCE;

import com.epam.reportportal.core.dashboard.UpdateDashboardHandler;
import com.epam.reportportal.core.events.domain.DashboardUpdatedEvent;
import com.epam.reportportal.core.events.domain.WidgetDeletedEvent;
import com.epam.reportportal.core.widget.content.remover.WidgetContentRemover;
import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.persistence.dao.DashboardRepository;
import com.epam.reportportal.infrastructure.persistence.dao.DashboardWidgetRepository;
import com.epam.reportportal.infrastructure.persistence.dao.WidgetRepository;
import com.epam.reportportal.infrastructure.persistence.entity.dashboard.Dashboard;
import com.epam.reportportal.infrastructure.persistence.entity.dashboard.DashboardWidget;
import com.epam.reportportal.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.model.activity.DashboardActivityResource;
import com.epam.reportportal.model.dashboard.AddWidgetRq;
import com.epam.reportportal.model.dashboard.UpdateDashboardRQ;
import com.epam.reportportal.reporting.OperationCompletionRS;
import com.epam.reportportal.ws.converter.builders.DashboardBuilder;
import com.epam.reportportal.ws.converter.converters.WidgetConverter;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * @author Pavel Bortnik
 */
@Service
public class UpdateDashboardHandlerImpl implements UpdateDashboardHandler {

  private static final int DELETE_WIDGET_COUNT_THRESHOLD = 1;

  private final DashboardWidgetRepository dashboardWidgetRepository;
  private final DashboardRepository dashboardRepository;
  private final WidgetContentRemover widgetContentRemover;
  private final WidgetRepository widgetRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Autowired
  public UpdateDashboardHandlerImpl(DashboardRepository dashboardRepository,
      @Qualifier("delegatingStateContentRemover") WidgetContentRemover widgetContentRemover,
      ApplicationEventPublisher eventPublisher,
      DashboardWidgetRepository dashboardWidgetRepository, WidgetRepository widgetRepository) {
    this.dashboardRepository = dashboardRepository;
    this.widgetContentRemover = widgetContentRemover;
    this.eventPublisher = eventPublisher;
    this.dashboardWidgetRepository = dashboardWidgetRepository;
    this.widgetRepository = widgetRepository;
  }

  @Override
  public OperationCompletionRS updateDashboard(MembershipDetails membershipDetails,
      UpdateDashboardRQ rq, Long dashboardId,
      ReportPortalUser user) {
    Dashboard dashboard = dashboardRepository.findByIdAndProjectId(dashboardId,
            membershipDetails.getProjectId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.DASHBOARD_NOT_FOUND_IN_PROJECT,
            dashboardId,
            membershipDetails.getProjectName()
        ));
    DashboardActivityResource before = TO_ACTIVITY_RESOURCE.apply(dashboard);

    if (!dashboard.getName().equals(rq.getName())) {
      BusinessRule.expect(dashboardRepository.existsByNameAndProjectId(rq.getName(),
              membershipDetails.getProjectId()
          ), BooleanUtils::isFalse)
          .verify(ErrorType.RESOURCE_ALREADY_EXISTS, rq.getName());
    }

    dashboard = new DashboardBuilder(dashboard).addUpdateRq(rq).get();
    dashboardRepository.save(dashboard);

    eventPublisher.publishEvent(new DashboardUpdatedEvent(before,
        TO_ACTIVITY_RESOURCE.apply(dashboard),
        user.getUserId(),
        user.getUsername(), membershipDetails.getOrgId()
    ));
    return new OperationCompletionRS(
        "Dashboard with ID = '" + dashboard.getId() + "' successfully updated");
  }

  @Override
  public OperationCompletionRS addWidget(Long dashboardId,
      MembershipDetails membershipDetails, AddWidgetRq rq,
      ReportPortalUser user) {
    final Dashboard dashboard = dashboardRepository.findByIdAndProjectId(dashboardId,
            membershipDetails.getProjectId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.DASHBOARD_NOT_FOUND_IN_PROJECT,
            dashboardId,
            membershipDetails.getProjectName()
        ));
    Set<DashboardWidget> dashboardWidgets = dashboard.getWidgets();

    validateWidgetBeforeAddingToDashboard(rq, dashboard, dashboardWidgets);

    Widget widget = widgetRepository.findByIdAndProjectId(rq.getAddWidget().getWidgetId(),
            membershipDetails.getProjectId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.WIDGET_NOT_FOUND_IN_PROJECT,
            rq.getAddWidget().getWidgetId(),
            membershipDetails.getProjectName()
        ));
    boolean isCreatedOnDashboard = CollectionUtils.isEmpty(widget.getDashboardWidgets());
    DashboardWidget dashboardWidget = WidgetConverter.toDashboardWidget(rq.getAddWidget(),
        dashboard, widget, isCreatedOnDashboard);
    dashboardWidgetRepository.save(dashboardWidget);
    return new OperationCompletionRS(
        "Widget with ID = '" + widget.getId()
            + "' was successfully added to the dashboard with ID = '" + dashboard.getId() + "'");

  }

  private void validateWidgetBeforeAddingToDashboard(AddWidgetRq rq, Dashboard dashboard,
      Set<DashboardWidget> dashboardWidgets) {
    BusinessRule.expect(dashboardWidgets.stream()
            .anyMatch(dashboardWidget -> StringUtils.equals(dashboardWidget.getWidgetName(),
                rq.getAddWidget().getName())), BooleanUtils::isFalse)
        .verify(ErrorType.DASHBOARD_UPDATE_ERROR, Suppliers.formattedSupplier(
            "Widget with name = '{}' is already added to the dashboard with name = '{}'",
            rq.getAddWidget().getName(),
            dashboard.getName()));

    BusinessRule.expect(dashboardWidgets.stream()
                .map(dw -> dw.getId().getWidgetId())
                .anyMatch(widgetId -> widgetId.equals(rq.getAddWidget().getWidgetId())),
            BooleanUtils::isFalse)
        .verify(ErrorType.DASHBOARD_UPDATE_ERROR, Suppliers.formattedSupplier(
            "Widget with ID = '{}' is already added to the dashboard with ID = '{}'",
            rq.getAddWidget().getWidgetId(),
            dashboard.getId()
        ));
  }

  @Override
  public OperationCompletionRS removeWidget(Long widgetId, Long dashboardId,
      MembershipDetails membershipDetails,
      ReportPortalUser user) {
    Dashboard dashboard = dashboardRepository.findByIdAndProjectId(dashboardId,
            membershipDetails.getProjectId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.DASHBOARD_NOT_FOUND_IN_PROJECT,
            dashboardId,
            membershipDetails.getProjectName()
        ));
    Widget widget = widgetRepository.findByIdAndProjectId(widgetId,
            membershipDetails.getProjectId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.WIDGET_NOT_FOUND_IN_PROJECT,
            widgetId,
            membershipDetails.getProjectName()
        ));

    if (shouldDelete(widget)) {
      OperationCompletionRS result = deleteWidget(widget);
      eventPublisher.publishEvent(
          new WidgetDeletedEvent(WidgetConverter.TO_ACTIVITY_RESOURCE.apply(widget),
              user.getUserId(),
              user.getUsername(), membershipDetails.getOrgId()
          ));
      return result;
    }

    DashboardWidget toRemove = dashboard.getWidgets()
        .stream()
        .filter(dashboardWidget -> widget.getId().equals(dashboardWidget.getId().getWidgetId()))
        .findFirst()
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.WIDGET_NOT_FOUND_IN_DASHBOARD, widgetId,
                dashboardId));

    dashboardWidgetRepository.delete(toRemove);

    return new OperationCompletionRS(
        "Widget with ID = '" + widget.getId()
            + "' was successfully removed from the dashboard with ID = '" + dashboard.getId()
            + "'");
  }

  private boolean shouldDelete(Widget widget) {
    return dashboardWidgetRepository.countAllByWidgetId(widget.getId())
        <= DELETE_WIDGET_COUNT_THRESHOLD;
  }

  /**
   * Totally remove the widget from all dashboards
   *
   * @param widget Widget
   * @return OperationCompletionRS
   */
  private OperationCompletionRS deleteWidget(Widget widget) {
    widgetContentRemover.removeContent(widget);
    dashboardWidgetRepository.deleteAll(widget.getDashboardWidgets());
    widgetRepository.delete(widget);
    return new OperationCompletionRS(
        "Widget with ID = '" + widget.getId() + "' was successfully deleted from the system.");
  }

}
