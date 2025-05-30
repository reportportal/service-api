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
package com.epam.ta.reportportal.core.dashboard.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.dashboard.CreateDashboardHandler;
import com.epam.ta.reportportal.core.dashboard.UpdateDashboardHandler;
import com.epam.ta.reportportal.core.filter.UpdateUserFilterHandler;
import com.epam.ta.reportportal.core.widget.CreateWidgetHandler;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.model.EntryCreatedRS;
import com.epam.ta.reportportal.model.dashboard.AddWidgetRq;
import com.epam.ta.reportportal.model.dashboard.DashboardPreconfiguredRq;
import com.epam.ta.reportportal.model.filter.UpdateUserFilterRQ;
import com.epam.ta.reportportal.model.filter.UserFilterResource;
import com.epam.ta.reportportal.model.widget.WidgetConfigResource;
import com.epam.ta.reportportal.model.widget.WidgetRQ;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Pavel Bortnik
 */
@Service
@RequiredArgsConstructor
public class DashboardPreconfiguredService {

  private final CreateDashboardHandler createDashboardHandler;
  private final UpdateDashboardHandler updateDashboardHandler;
  private final CreateWidgetHandler createWidgetHandler;
  private final UpdateUserFilterHandler userFilterHandler;

  public EntryCreatedRS createDashboard(MembershipDetails membershipDetails,
      DashboardPreconfiguredRq rq, ReportPortalUser user) {
    var dashboard = createDashboardHandler.createDashboard(membershipDetails, rq, user);
    createAndAddWidgets(dashboard.getId(), rq.getDashboardConfig().getWidgetsConfig(), membershipDetails, user);
    return dashboard;
  }

  private void createAndAddWidgets(Long dashboardId, List<WidgetConfigResource> widgetsConfigs,
      MembershipDetails membershipDetails, ReportPortalUser user) {
    var filterIdMapping = createUniqueFilters(widgetsConfigs, membershipDetails, user);
    widgetsConfigs.forEach(widgetConfig -> {
      var widget = createWidgetByConfig(membershipDetails, user,
          getNewFilterIds(filterIdMapping, widgetConfig), widgetConfig);
      widgetConfig.getWidgetObject().setWidgetId(widget.getId());
      updateDashboardHandler.addWidget(dashboardId, membershipDetails,
          new AddWidgetRq(widgetConfig.getWidgetObject()), user);
    });
  }

  private EntryCreatedRS createWidgetByConfig(MembershipDetails projectDetails, ReportPortalUser user,
      List<Long> filterIds, WidgetConfigResource config) {
    var widgetRQ = new WidgetRQ();
    widgetRQ.setName(config.getWidgetResource().getName());
    widgetRQ.setDescription(config.getWidgetResource().getDescription());
    widgetRQ.setWidgetType(config.getWidgetResource().getWidgetType());
    widgetRQ.setContentParameters(config.getWidgetResource().getContentParameters());
    widgetRQ.setFilterIds(filterIds);
    return createWidgetHandler.createWidget(widgetRQ, projectDetails, user);
  }

  private List<Long> getNewFilterIds(HashMap<Long, Long> filterIdMapping,
      WidgetConfigResource config) {
    return config.getWidgetResource().getAppliedFilters().stream()
        .map(it -> filterIdMapping.get(it.getFilterId())).collect(
            Collectors.toList());
  }

  private HashMap<Long, Long> createUniqueFilters(List<WidgetConfigResource> widgetsConfigs,
      MembershipDetails membershipDetails, ReportPortalUser user) {

    var filtersMapping = new HashMap<Long, Long>();

    var uniqueFilters = widgetsConfigs.stream()
        .flatMap(it -> it.getWidgetResource().getAppliedFilters().stream())
        .collect(Collectors.toMap(UserFilterResource::getFilterId, f -> f,
            (existing, current) -> existing));

    uniqueFilters.forEach((id, filter) -> {
      var updateUserFilterRQ = new UpdateUserFilterRQ();
      updateUserFilterRQ.setName(filter.getName());
      updateUserFilterRQ.setConditions(filter.getConditions());
      updateUserFilterRQ.setOrders(filter.getOrders());
      updateUserFilterRQ.setObjectType(filter.getObjectType());
      updateUserFilterRQ.setDescription(filter.getDescription());
      var createdFilter = userFilterHandler.createFilterCopyOnDuplicate(updateUserFilterRQ,
          membershipDetails.getProjectKey(), user);
      filtersMapping.put(id, createdFilter.getId());
    });
    return filtersMapping;
  }


}
